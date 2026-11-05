package c.app.sociamediabackend.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var apiService: ApiService? = null
    private var currentBaseUrl: String? = null

    fun getService(context: Context): ApiService {
        val sessionManager = SessionManager(context)
        val baseUrl = sessionManager.getBaseUrl()

        if (apiService == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val token = sessionManager.getAuthToken()

                    val requestBuilder = original.newBuilder()
                    if (!token.isNullOrEmpty()) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }

                    val response = chain.proceed(requestBuilder.build())
                    if (response.code == 401) {
                        sessionManager.clearSession()
                    }
                    response
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        return apiService!!
    }

    fun reset() {
        apiService = null
        currentBaseUrl = null
    }
}
