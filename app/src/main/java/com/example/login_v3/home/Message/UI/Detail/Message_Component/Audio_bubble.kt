package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import okhttp3.internal.concurrent.formatDuration

@Composable
fun AudioMessageBubble(
    message: Message,
    audioUrl: String,
    isMe: Boolean,
    bubbleColor: Color,
    viewModel: ChatViewModel // 🎯 傳入 ViewModel
) {
    // 訂閱全域的播放狀態
    val playbackState by viewModel.audioState.collectAsState()

    // 判斷當前這顆氣泡是不是正在被操作的音訊
    val isCurrentAudio = playbackState.currentPlayingMessageId == message.id
    val isPlaying = isCurrentAudio && playbackState.isPlaying

    // 計算當前進度 (0.0 ~ 1.0)
    val currentPos = if (isCurrentAudio) playbackState.currentPosition else 0L
    val totalDuration = if (isCurrentAudio) playbackState.duration else 0L
    val sliderProgress = if (totalDuration > 0) currentPos.toFloat() / totalDuration.toFloat() else 0f

    Row(modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp, topEnd = 12.dp,
                    bottomStart = if (isMe) 12.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 12.dp
                )
            )
            .background(bubbleColor)
            .padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // 播放 / 暫停按鈕
        IconButton(
            onClick = { viewModel.toggleAudioPlayback(message.id, audioUrl) }, // 🎯 觸發全域控制
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暫停" else "播放",
                tint = if (isMe) Color.White else Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // 進度條
        Slider(
            value = sliderProgress,
            onValueChange = { ratio ->
                if (isCurrentAudio) viewModel.seekAudioTo(ratio) // 🎯 允許使用者拖拉進度
            },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                activeTrackColor = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                inactiveTrackColor = (if (isMe) Color.White else Color.Gray).copy(alpha = 0.3f)
            )
        )

        // 時間格式化 (00:00)
        val displayTime = remember(currentPos, totalDuration) {
            val timeToFormat = if (currentPos > 0) currentPos else totalDuration
            formatDuration(timeToFormat)
        }

        Text(
            text = displayTime,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}