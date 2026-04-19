package com.example.login_v3.data.api.api_class

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String
)