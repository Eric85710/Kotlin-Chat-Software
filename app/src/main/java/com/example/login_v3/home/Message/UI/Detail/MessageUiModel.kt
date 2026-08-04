package com.example.login_v3.home.Message.UI.Detail

import androidx.compose.runtime.Immutable
import com.example.login_v3.data.api.api_class.Attachment
import com.example.login_v3.data.api.api_class.CallLogInfo
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.Reaction
import com.example.login_v3.data.local.entities.MessageStatus

@Immutable
data class MessageUiModel(
    val id: String,
    val senderId: String,
    val content: String,
    val type: String,
    val createdAt: String,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val replyToId: String?,
    val repliedMessage: MessageUiModel?,
    val attachment: Attachment?,
    val reactions: List<Reaction>,
    val status: MessageStatus,
    val callLogInfo: CallLogInfo?,
    val isImage: Boolean,
    val isGif: Boolean,
    val isVideo: Boolean,
    val isAudio: Boolean,
    val isFile: Boolean,
    val mediaUrl: String,
    val thumbnailUrl: String? = null,
    val aspectRatio: Float? = null
)

fun Message.toUiModel(): MessageUiModel {
    val finalAspectRatio = aspectRatio ?: attachment?.let {
        if (it.width > 0 && it.height > 0) it.width.toFloat() / it.height.toFloat() else null
    }

    val baseUrl = "https://tg.technologia-tw.com"
    val finalThumbnailUrl = attachment?.thumbnailUrl?.let { raw ->
        if (raw.startsWith("http")) raw
        else "$baseUrl${if (raw.startsWith("/")) "" else "/"}$raw"
    }

    return MessageUiModel(
        id = id,
        senderId = senderId,
        content = content,
        type = type,
        createdAt = createdAt,
        isEdited = isEdited,
        isDeleted = isDeleted,
        replyToId = replyToId,
        repliedMessage = repliedMessage?.toUiModel(),
        attachment = attachment,
        reactions = reactions ?: emptyList(),
        status = status,
        callLogInfo = callLogInfo,
        isImage = isImage,
        isGif = isGif,
        isVideo = isVideo,
        isAudio = isAudio,
        isFile = isFile,
        mediaUrl = mediaUrl,
        thumbnailUrl = finalThumbnailUrl,
        aspectRatio = finalAspectRatio
    )
}
