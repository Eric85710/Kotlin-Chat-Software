package com.example.login_v3.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.login_v3.data.api.api_class.Reaction

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
    val reactions: List<Reaction>?
)
