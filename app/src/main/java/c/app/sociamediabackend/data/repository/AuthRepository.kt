package c.app.sociamediabackend.data.repository

import c.app.sociamediabackend.data.api.ApiService
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.api.safeApiCall
import c.app.sociamediabackend.data.model.AuthResponse
import c.app.sociamediabackend.data.model.LoginRequest
import c.app.sociamediabackend.data.model.RegisterRequest

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.login(loginRequest) }
    }

    suspend fun register(registerRequest: RegisterRequest): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.register(registerRequest) }
    }

    suspend fun getMe(): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.getMe() }
    }
}
