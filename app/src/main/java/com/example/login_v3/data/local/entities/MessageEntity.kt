package com.example.login_v3.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.login_v3.data.api.api_class.Reaction
import com.example.login_v3.data.api.api_class.Attachment

enum class MessageStatus { SENDING, SUCCESS, FAILED }

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatRoomId: String,
    val senderId: String,
    val content: String,
    val type: String,
    val createdAt: String,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val replyToId: String?,
    val status: MessageStatus,
    val reactions: List<Reaction>?,
    val attachment: Attachment? = null,
    val aspectRatio: Float? = null,
    val isDownloaded: Boolean = false, // 👈 新增：標記媒體/訊息是否已下載到本地
    val localPath: String? = null      // 👈 新增：儲存本地檔案路徑
)
