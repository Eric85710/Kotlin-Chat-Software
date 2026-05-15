package com.example.login_v3.data.api.api_class

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