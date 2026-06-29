package com.example.login_v3.data.api.api_class

import com.example.login_v3.R
import com.example.login_v3.home.Message.ViewModel.Detail.MessageStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


//Get DM list
@JsonClass(generateAdapter = true)
data class RoomListResponse(
    @Json(name = "rooms") val rooms: List<ChatRoom>
)
@JsonClass(generateAdapter = true)
data class ChatRoom(
    @Json(name = "room_id") val roomId: String,
    @Json(name = "room_name") val roomName: String?,
    @Json(name = "room_type") val roomType: String? = null, // 🎯 修正這裡：改成 String? 並加上預設值，解決閃退問題！
    @Json(name = "room_icon_url") val roomIconUrl: String?,
    @Json(name = "is_muted") val isMuted: Boolean = false,   // 💡 安全防護：若後端漏傳，預設為 false
    @Json(name = "is_pinned") val isPinned: Boolean = false, // 💡 安全防護：若後端漏傳，預設為 false
    @Json(name = "unread_count") val unreadCount: Int = 0,   // 💡 安全防護：若後端漏傳，預設為 0
    @Json(name = "mention_count") val mentionCount: Int = 0, // 💡 安全防護：若後端漏傳，預設為 0
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

val Partner.fullContactAvatarUrl: Any
    get() = if (avatarUrl.isNullOrBlank()) {
        R.drawable.avatar_v1 // 如果沒網址，直接回傳預設圖片資源
    } else if (avatarUrl.startsWith("http")) {
        avatarUrl // 已經是完整網址就直接用
    } else {
        "http://192.168.0.217$avatarUrl" // 否則補上 Base URL
    }



//Message DM
// 抽到最外層，讓 ChatRoom、LastMessage、Message 共同使用
@JsonClass(generateAdapter = true)
data class Attachment(
    @Json(name = "filename") val filename: String,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "size") val size: Long
)

@JsonClass(generateAdapter = true)
data class Reaction(
    @Json(name = "emoji") val emoji: String = "",
    @Json(name = "count") val count: Int = 0,
    @Json(name = "me_reacted") val meReacted: Boolean = false
)

// --- Message DM 相關 ---
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
    @kotlin.jvm.Transient
    var repliedMessage: Message? = null,
    @Json(name = "forwarded_from_id") val forwardedFromId: String? = null,
    @Json(name = "forwarded_from_room_id") val forwardedFromRoomId: String? = null,
    @Json(name = "attachment") val attachment: Attachment? = null,
    @Json(name = "reactions") val reactions: List<Reaction>? = emptyList(),

    // 🌟 核心新增：讓 UI 可以辨識訊息發送狀態！
    @kotlin.jvm.Transient // 👈 告訴 Moshi 這個欄位不需要解析 JSON，這是我們本地維護的
    val status: MessageStatus = MessageStatus.SUCCESS,

    @kotlin.jvm.Transient
    var callLogInfo: CallLogInfo? = null

){
    init {
        if (type == "system" || type.startsWith("call")) {
            callLogInfo = CallLogInfo.parseContent(content)
        }
    }
}

@JsonClass(generateAdapter = true)
data class WebSocketEventResponse(
    @Json(name = "type") val type: String,
    @Json(name = "payload") val payload: WebSocketMessagePayload? // 🎯 直接強型別對齊
)

// 這是專門給 Socket payload 用的臨時結構，用來解決 room_id 和 chat_room_id 欄位不一致的問題
@JsonClass(generateAdapter = true)
data class WebSocketMessagePayload(
    @Json(name = "id") val id: String,
    @Json(name = "room_id") val roomId: String, // 🎯 完美吃下後端的 room_id
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "content") val content: String,
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "is_edited") val isEdited: Boolean,
    @Json(name = "is_deleted") val isDeleted: Boolean,
    @Json(name = "reply_to_id") val replyToId: String? = null,
    @Json(name = "reactions") val reactions: List<Reaction>? = emptyList()
)

//send message
@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "content") val content: String,
    @Json(name = "reply_to_id") val replyToId: String? = null
)

//message reaction
@JsonClass(generateAdapter = true)
data class MessageReactionUsersResponse(
    @Json(name = "users") val users: List<String>
)




//voice call
@JsonClass(generateAdapter = true)
data class CallLogInfo(
    @Json(name = "type") val type: String, // 例如: call_missed, call_rejected
    @Json(name = "initiator_id") val initiatorId: String, // 順便改成 Kotlin 的駝峰命名規範
    @Json(name = "has_video") val hasVideo: Boolean
) {
    companion object {
        private val moshi = Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        private val adapter = moshi.adapter(CallLogInfo::class.java)

        // 🌟 改成只單純解析 String
        fun parseContent(content: String?): CallLogInfo? {
            val json = content?.trim() ?: return null
            if (!json.startsWith("{") || !json.endsWith("}")) return null

            return try {
                adapter.fromJson(json)
            } catch (e: Exception) {
                android.util.Log.e("CallLogInfo", "解析通話 JSON 失敗: ${e.message}")
                null
            }
        }
    }
}