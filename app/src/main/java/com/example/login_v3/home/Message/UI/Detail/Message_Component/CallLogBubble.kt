package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.data.api.api_class.CallLogInfo

@Composable
fun CallLogBubble(
    callLog: CallLogInfo,
    isMe: Boolean, // 這一則訊息是不是「我」發送的
    currentUserId: String,
    partnerDisplayName: String,
    modifier: Modifier = Modifier
) {
    // 🎯 改造 1：配合方案 A 的變數名稱修正 (initiator_id -> initiatorId)
    val isIInitiated = callLog.initiatorId == currentUserId

    // 根據通話狀態決定圖示、顏色與顯示主標題
    val (icon, iconColor, titleText) = when (callLog.type) {
        "call_missed" -> {
            Triple(
                Icons.Default.CallMissed,
                Color(0xFFEC1C24), // 未接來電用紅色
                if (isIInitiated) "對方未接聽" else "未接來電"
            )
        }
        else -> {
            // 🎯 改造 2：配合方案 A 的變數名稱修正 (has_video -> hasVideo)
            val callTypeStr = if (callLog.hasVideo) "視訊通話" else "語音通話"
            Triple(
                if (callLog.hasVideo) Icons.Default.VideoCall else Icons.Default.Call,
                if (isMe) Color(0xFF4CAF50) else Color(0xFF1976D2),
                if (isIInitiated) "撥出$callTypeStr" else "已接聽$callTypeStr"
            )
        }
    }

    // 來電顯示副標題邏輯
    val callTypeLabel = if (callLog.hasVideo) "視訊" else "語音"
    val subText = when {
        isIInitiated -> "$callTypeLabel • 撥給 $partnerDisplayName"
        else -> "$callTypeLabel • 來自 $partnerDisplayName"
    }

    // 🎯 改造 3：外層加上氣泡背景與剪裁，讓它看起來更像聊天氣泡
    Surface(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // 給予淡淡的灰色氣泡背景
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 左側：圓形背景與狀態圖示
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = titleText,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 右側：通話狀態與來電顯示文字
            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}