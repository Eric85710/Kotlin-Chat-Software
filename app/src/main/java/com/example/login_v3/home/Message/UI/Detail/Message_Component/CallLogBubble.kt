package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    isMe: Boolean,
    currentUserId: String,
    partnerDisplayName: String,
    modifier: Modifier = Modifier
) {
    // 判斷這通電話是不是「我」撥打的
    val isIInitiated = callLog.initiator_id == currentUserId

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
            val callTypeStr = if (callLog.has_video) "視訊通話" else "語音通話"
            Triple(
                if (callLog.has_video) Icons.Default.VideoCall else Icons.Default.Call,
                if (isMe) Color(0xFF4CAF50) else Color(0xFF1976D2),
                if (isIInitiated) "撥出$callTypeStr" else "已接聽$callTypeStr"
            )
        }
    }

    // 🌟 新增：來電顯示副標題邏輯
    val callTypeLabel = if (callLog.has_video) "視訊" else "語音"
    val subText = when {
        // 我撥出去的 -> 顯示「撥給 某某」
        isIInitiated -> "$callTypeLabel • 撥給 $partnerDisplayName"
        // 對方撥進來的 -> 顯示「來自 某某」
        else -> "$callTypeLabel • 來自 $partnerDisplayName"
    }

    Row(
        modifier = modifier.padding(vertical = 2.dp),
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
                // 🌟 改用動態組合的來電顯示文字
                text = subText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            )
        }
    }
}