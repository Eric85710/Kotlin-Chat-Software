package com.example.login_v3.data.api.api_class

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass



//login data
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val token: String,
    val user: UserInfo
)
@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: String,
    val username: String,
    val display_name: String,
    val email: String,
    val avatar_url: String?,
    val banner_url: String?,
    val bio: String?,
    val status: String,
    val is_verified: Boolean,
    val created_at: String
)




//register data
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

    // 💡 使用 field: 前綴消除警告，並保持 String? 避免解析崩潰
    @field:Json(name = "display_name") val displayName: String?,

    @field:Json(name = "email") val email: String?,

    @field:Json(name = "avatar_url") val avatarUrl: String? = null,
    @field:Json(name = "banner_url") val bannerUrl: String? = null,
    @field:Json(name = "bio") val bio: String? = null,

    @field:Json(name = "is_verified") val isVerified: Boolean? = false,
    @field:Json(name = "status") val status: String? = "active",
    @field:Json(name = "created_at") val createdAt: String? = ""
)