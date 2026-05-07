package com.example.login_v3.data.api.api_class

import com.example.login_v3.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendListResponse(
    val friends: List<Friend>
)
@JsonClass(generateAdapter = true)
data class Friend(
    @field:Json(name = "friend_id")
    val friendId: String,

    val username: String,

    @field:Json(name = "display_name")
    val displayName: String,

    @field:Json(name = "avatar_url")
    val avatarUrl: String?, // 建議給可空性，避免後端沒傳頭像時崩潰

    val status: String,

    @field:Json(name = "accepted_at")
    val acceptedAt: String
)
val Friend.fullFriendsAvatarUrl: Any
    get() = if (avatarUrl.isNullOrBlank()) {
        R.drawable.avatar_v1
    } else if (avatarUrl.startsWith("http")) {
        avatarUrl
    } else {
        "http://192.168.0.217$avatarUrl"
    }



//friends pending request
@JsonClass(generateAdapter = true)
data class PendingFriendsResponse(
    @field:Json(name = "pending")
    val pendingRequests: List<PendingFriendApiModel>
)

@JsonClass(generateAdapter = true)
data class PendingFriendApiModel(
    @field:Json(name = "friendship_id")
    val friendshipId: String,

    val username: String,

    @field:Json(name = "display_name")
    val displayName: String,

    @field:Json(name = "avatar_url")
    val PendingAvatarUrl: String?,

    @field:Json(name = "from_user_id")
    val fromUserId: String,

    @field:Json(name = "created_at")
    val createdAt: String
)

val PendingFriendApiModel.fullPendingAvatarUrl: Any
    get() = if (PendingAvatarUrl.isNullOrBlank()) {
        R.drawable.avatar_v1
    } else if (PendingAvatarUrl.startsWith("http")) {
        PendingAvatarUrl
    } else {
        "http://192.168.0.217$PendingAvatarUrl"
    }
