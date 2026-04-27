package com.example.login_v3.data.api.api_class

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val userId: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "display_name") val displayName: String, // 將 JSON 的底線對應到 Kotlin 的駝峰
    val email: String,
    val password: String,
    val username: String
)