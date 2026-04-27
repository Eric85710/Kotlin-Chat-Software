package com.example.login_v3.data.api.api_class

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendRequest(
    val friendId: String
)
@JsonClass(generateAdapter = true)
data class ApiResponse(
    val success: Boolean,
    val message: String
)