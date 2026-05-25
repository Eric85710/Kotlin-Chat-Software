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
import retrofit2.http.Path
import retrofit2.http.Query

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



    // friends
    @GET("api/friends")
    suspend fun getFriends(): FriendListResponse
    @GET("api/friends/pending")
    suspend fun getFriendsPending(): PendingFriendsResponse

    //accept and reject
    // accept and reject
    @POST("api/friends/{fromUserId}/accept")
    suspend fun acceptFriendRequest(
        @Path("fromUserId") fromUserId: String
    ): Response<Unit>

    @POST("api/friends/{fromUserId}/reject")
    suspend fun rejectFriendRequest(
        @Path("fromUserId") fromUserId: String
    ): Response<Unit>

    // add friends
    @POST("api/friends/request")
    suspend fun sendFriendRequest(
        @Body request: AddFriendRequest
    ): Response<Unit>

    // 搜尋使用者
    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<UserSearchResponse>


    // Get dm list
    @GET("api/dm/rooms")
    suspend fun getChatRooms(): Response<RoomListResponse>

    // Get dm message
    @GET("api/dm/{room_id}/messages")
    suspend fun getChatMessages(
        @Path("room_id") roomId: String
    ): Response<MessageResponse>

    // 標記聊天室已讀
    @POST("api/dm/{room_id}/read")
    suspend fun markAsRead(
        @Path("room_id") roomId: String
    ): Response<Unit>

    //send DM message
    @POST("api/dm/{room_id}/messages")
    suspend fun sendMessage(
        @Path("room_id") roomId: String,
        @Body request: SendMessageRequest
    ): Response<Message>

    //send image
    @Multipart
    @POST("api/dm/{room_id}/attachment")
    suspend fun uploadAttachment(
        @Path("room_id") roomId: String,
        @Part file: MultipartBody.Part
    ): Response<Message>
}