package com.example.login_v3.data.api.api_class

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