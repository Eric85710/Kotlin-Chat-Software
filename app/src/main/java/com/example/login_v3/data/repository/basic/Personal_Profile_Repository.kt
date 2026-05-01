package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.UserProfile
import javax.inject.Inject

class Personal_Profile_Repository @Inject constructor(
    private val api: TecnologiaApi
) {
    suspend fun getMyProfile(): UserProfile {
        val response = api.getUserProfile()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }
}