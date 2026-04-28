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
    val display_name: String,
    val email: String,
    val password: String,
    val username: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "display_name") val displayName: String,
    @field:Json(name = "reg_email") val regEmail: String,
    @field:Json(name = "current_email") val currentEmail: String?,
    @field:Json(name = "avatar_url") val avatarUrl: String?,
    @field:Json(name = "bio") val bio: String?,
    @field:Json(name = "is_active") val isActive: Boolean,
    @field:Json(name = "is_verified") val isVerified: Boolean,
    @field:Json(name = "status") val status: String,
    @field:Json(name = "reg_date") val regDate: String,
    @field:Json(name = "last_login") val lastLogin: String?,
    @field:Json(name = "updated_at") val updatedAt: String?
)