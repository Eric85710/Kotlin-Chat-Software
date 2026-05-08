package com.example.login_v3.data.api.api_class

import com.example.login_v3.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendListResponse(
    // 這樣寫最安全：即便 JSON 裡少了這個欄位，也會變成 emptyList
    val friends: List<Friend> = emptyList()
)
@JsonClass(generateAdapter = true)
data class Friend(
    @field:Json(name = "friend_id")
    val friendId: String?, // 改為 String?

    val username: String?, // 建議 username 也改可空

    @Json(name = "display_name") // 移除 field: 試試看
    val displayName: String?,

    @Json(name = "avatar_url")
    val avatarUrl: String?,

    val status: String?,

    @field:Json(name = "accepted_at")
    val acceptedAt: String?
)
val Friend.fullFriendsAvatarUrl: Any
    get() {
        val url = avatarUrl ?: ""
        return when {
            url.isBlank() -> R.drawable.avatar_v1
            url.startsWith("http") -> url
            else -> "http://192.168.0.217$url"
        }
    }



//friends pending request
@JsonClass(generateAdapter = true)
data class PendingFriendsResponse(
    @Json(name = "pending") // 移除 field:
    val pendingRequests: List<PendingFriendApiModel>
)

@JsonClass(generateAdapter = true)
data class PendingFriendApiModel(
    @Json(name = "friendship_id")
    val friendshipId: String?,

    val username: String?,

    @Json(name = "display_name")
    val displayName: String?, // 建議改為可空，比較保險

    @Json(name = "avatar_url")
    val pendingAvatarUrl: String?,

    @Json(name = "from_user_id")
    val fromUserId: String?,

    @Json(name = "created_at")
    val createdAt: String?
)

val PendingFriendApiModel.fullPendingAvatarUrl: Any
    get() = if (pendingAvatarUrl.isNullOrBlank()) {
        R.drawable.avatar_v1
    } else if (pendingAvatarUrl.startsWith("http")) {
        pendingAvatarUrl
    } else {
        "http://192.168.0.217$pendingAvatarUrl"
    }
