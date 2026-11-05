package c.app.sociamediabackend.data.api

import retrofit2.Response

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(response.code(), "Empty response body")
            }
        } else {
            val errorMsg = response.errorBody()?.string().orEmpty()
            NetworkResult.Error(
                response.code(),
                if (errorMsg.isNotBlank()) errorMsg else "HTTP ${response.code()} Error"
            )
        }
    } catch (e: Exception) {
        NetworkResult.Exception(e)
    }
}
