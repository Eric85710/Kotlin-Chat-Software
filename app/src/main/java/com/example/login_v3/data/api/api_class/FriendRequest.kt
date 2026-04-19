package com.example.login_v3.data.api.api_class

data class FriendRequest(
    val friendId: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)