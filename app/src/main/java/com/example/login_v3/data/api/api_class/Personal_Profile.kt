package com.example.login_v3.data.api.api_class

import com.example.login_v3.R
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

val UserProfile.fullAvatarUrl: Any
    get() = if (avatar_url.isNullOrBlank()) {
        R.drawable.avatar_v1 // 如果沒網址，直接回傳預設圖片資源
    } else if (avatar_url.startsWith("http")) {
        avatar_url // 已經是完整網址就直接用
    } else {
        "https://tg.technologia-tw.com$avatar_url" // 否則補上 Base URL
    }

val UserProfile.fullBannerUrl: Any
    get() = if (banner_url.isNullOrBlank()) {
        R.drawable.thumbnail_v1 // 如果沒網址，直接回傳預設圖片資源
    } else if (banner_url.startsWith("http")) {
        banner_url // 已經是完整網址就直接用
    } else {
        "https://tg.technologia-tw.com$banner_url" // 否則補上 Base URL
    }