package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import javax.inject.Inject

class NetworkRepository @Inject constructor(
    private val apiService: TecnologiaApi
) {
    suspend fun performHealthCheck(): Result<Boolean> {
        return try {
            val response = apiService.checkHealth()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}