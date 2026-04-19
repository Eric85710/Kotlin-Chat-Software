package com.example.login_v3.data.api

import com.example.login_v3.data.api.api_class.ApiResponse
import com.example.login_v3.data.api.api_class.FriendRequest
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.LoginResponse
import retrofit2.Response
import com.example.login_v3.data.api.api_class.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TecnologiaApi {

    @GET("api/health")
    // 必須包裹在 Response<> 裡面，才能呼叫 .isSuccessful
    suspend fun checkHealth(): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/me")
    suspend fun getMe(@Header("Authorization") token: String): User

    @POST("api/friends/request")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body body: FriendRequest
    ): ApiResponse

    // 其他 endpoint 一樣加
}