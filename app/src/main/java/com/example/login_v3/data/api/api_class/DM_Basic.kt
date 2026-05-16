package com.example.login_v3.data.api.api_class

import com.example.login_v3.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass



//Get DM list
@JsonClass(generateAdapter = true)
data class RoomListResponse(
    @Json(name = "rooms") val rooms: List<ChatRoom>
)
@JsonClass(generateAdapter = true)
data class ChatRoom(
    @Json(name = "room_id") val roomId: String,
    @Json(name = "room_name") val roomName: String?,
    @Json(name = "room_type") val roomType: String,
    @Json(name = "room_icon_url") val roomIconUrl: String?,
    @Json(name = "is_muted") val isMuted: Boolean,
    @Json(name = "is_pinned") val isPinned: Boolean,
    @Json(name = "unread_count") val unreadCount: Int,
    @Json(name = "mention_count") val mentionCount: Int,
    @Json(name = "partner") val partner: Partner?,
    @Json(name = "last_message") val lastMessage: LastMessage?
)
@JsonClass(generateAdapter = true)
data class Partner(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "status") val status: String
)
@JsonClass(generateAdapter = true)
data class LastMessage(
    @Json(name = "id") val id: String,
    @Json(name = "chat_room_id") val chatRoomId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "content") val content: String,
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "deleted_at") val deletedAt: String?,
    @Json(name = "edited_at") val editedAt: String?,
    @Json(name = "is_deleted") val isDeleted: Boolean,
    @Json(name = "is_edited") val isEdited: Boolean,
    @Json(name = "attachment") val attachment: Attachment?,
    @Json(name = "reactions") val reactions: List<Reaction>?,
    @Json(name = "reply_to_id") val replyToId: String?,
    @Json(name = "forwarded_from_id") val forwardedFromId: String?,
    @Json(name = "forwarded_from_room_id") val forwardedFromRoomId: String?
)
@JsonClass(generateAdapter = true)
data class Attachment(
    @Json(name = "filename") val filename: String,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "size") val size: Long
)
@JsonClass(generateAdapter = true)
data class Reaction(
    @Json(name = "emoji") val emoji: String,
    @Json(name = "count") val count: Int,
    @Json(name = "me_reacted") val meReacted: Boolean
)

val Partner.fullContactAvatarUrl: Any
    get() = if (avatarUrl.isNullOrBlank()) {
        R.drawable.avatar_v1 // 如果沒網址，直接回傳預設圖片資源
    } else if (avatarUrl.startsWith("http")) {
        avatarUrl // 已經是完整網址就直接用
    } else {
        "http://192.168.0.217$avatarUrl" // 否則補上 Base URL
    }



//Message DM
@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "has_more") val hasMore: Boolean,
    @Json(name = "limit") val limit: Int,
    @Json(name = "messages") val messages: List<Message>,
    @Json(name = "next_cursor") val nextCursor: String? = null
)

@JsonClass(generateAdapter = true)
data class Message(
    @Json(name = "id") val id: String,
    @Json(name = "chat_room_id") val chatRoomId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "content") val content: String,
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "edited_at") val editedAt: String? = null,
    @Json(name = "deleted_at") val deletedAt: String? = null,
    @Json(name = "is_edited") val isEdited: Boolean,
    @Json(name = "is_deleted") val isDeleted: Boolean,
    @Json(name = "reply_to_id") val replyToId: String? = null,
    @Json(name = "forwarded_from_id") val forwardedFromId: String? = null,
    @Json(name = "forwarded_from_room_id") val forwardedFromRoomId: String? = null,
    @Json(name = "attachment") val attachment: Attachment? = null,
    @Json(name = "reactions") val reactions: List<Reaction> = emptyList() // 若 API 沒給此欄位，給予空列表
){
    @JsonClass(generateAdapter = true)
    data class Attachment(
        @Json(name = "filename") val filename: String,
        @Json(name = "mime_type") val mimeType: String,
        @Json(name = "size") val size: Long
    )
    @JsonClass(generateAdapter = true)
    data class Reaction(
        @Json(name = "emoji") val emoji: String,
        @Json(name = "count") val count: Int,
        @Json(name = "me_reacted") val meReacted: Boolean
    )
}