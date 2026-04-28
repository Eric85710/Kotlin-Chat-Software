package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ApiResponse
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.LoginResponse
import com.example.login_v3.data.api.api_class.RegisterRequest
import com.example.login_v3.data.api.api_class.RegisterResponse
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: TecnologiaApi
) {
    /**
     * 處理用戶註冊
     * 回傳 Response<ApiResponse>，讓 ViewModel 可以根據 isSuccessful 判斷結果
     */
    suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        return api.register(request)
    }

    // 登入功能預留
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return api.login(request)
    }
}