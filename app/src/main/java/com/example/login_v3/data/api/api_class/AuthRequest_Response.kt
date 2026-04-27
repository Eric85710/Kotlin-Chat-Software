package com.example.login_v3.data.api.api_class

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String
)

data class RegisterRequest(
    val display_name: String,
    val email: String,
    val password: String,
    val username: String
)