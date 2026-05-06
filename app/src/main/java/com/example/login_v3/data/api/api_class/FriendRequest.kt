package com.example.login_v3.data.api.api_class

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendListResponse(
    val friends: List<Friend>
)
@JsonClass(generateAdapter = true)
data class Friend(
    @Json(name = "friend_id")
    val friendId: String,

    val username: String,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "avatar_url")
    val avatarUrl: String?, // 建議給可空性，避免後端沒傳頭像時崩潰

    val status: String,

    @Json(name = "accepted_at")
    val acceptedAt: String
)