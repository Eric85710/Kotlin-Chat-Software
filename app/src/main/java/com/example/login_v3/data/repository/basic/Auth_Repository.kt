package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ApiResponse
import com.example.login_v3.data.api.api_class.RegisterRequest
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: TecnologiaApi
) {
    /**
     * 處理用戶註冊
     * 回傳 Response<ApiResponse>，讓 ViewModel 可以根據 isSuccessful 判斷結果
     */
    suspend fun register(request: RegisterRequest): Response<ApiResponse> {
        return api.register(request)
    }

    // 你也可以在這裡加入其他的 Auth 相關功能，例如登入
    // suspend fun login(request: LoginRequest) = api.login(request)
}