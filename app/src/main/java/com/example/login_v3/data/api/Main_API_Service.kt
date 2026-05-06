package com.example.login_v3.data.api

import com.example.login_v3.data.api.api_class.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface TecnologiaApi {

    @GET("api/health")
    suspend fun checkHealth(): Response<Unit>

    // 註冊：建議路徑 api/auth/register
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    // 登入：將回傳值包裹在 Response 中
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // 取得個人資料：通常需要 Token
    @GET("api/me")
    suspend fun getUserProfile(): Response<UserProfile>

    @PATCH("api/me")
    suspend fun updateProfile(
        @Body request: UserProfileUpdateRequest
    ): Response<Unit>

    @Multipart
    @POST("api/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @Multipart
    @POST("api/me/banner")
    suspend fun uploadBanner(
        @Part file: MultipartBody.Part
    ): Response<Unit>

    // contact
    @GET("api/friends")
    suspend fun getFriends(): FriendListResponse
}