package com.example.login_v3.data.api.api_class

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val user_id: String?,
    val username: String?,
    val display_name: String?,
    val email: String?,
    val avatar_url: String?,
    val banner_url: String?,
    val bio: String?,
    val status: String?,
    val is_active: Boolean?,
    val is_verified: Boolean?,
    val last_login: String?,
    val created_at: String?
)

@JsonClass(generateAdapter = true)
data class UserProfileUpdateRequest(
    val avatar_url: String? = null,
    val banner_url: String? = null,
    val bio: String? = null,
    val display_name: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    @field:Json(name = "user_id") val userId: String,
    val username: String,
    val email: String,
    @field:Json(name = "display_name") val displayName: String,
    @field:Json(name = "avatar_url") val avatarUrl: String?,
    @field:Json(name = "banner_url") val bannerUrl: String?,
    val bio: String?,
    val status: String?,
    @field:Json(name = "is_active") val isActive: Boolean,
    @field:Json(name = "is_verified") val isVerified: Boolean,
    @field:Json(name = "created_at") val createdAt: String,
    @field:Json(name = "last_login") val lastLogin: String?
)