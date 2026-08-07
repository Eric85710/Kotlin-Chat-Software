package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Edit // 🎯 新增
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.login_v3.home.Message.UI.Detail.MessageUiModel

@Composable
fun MessageActionMenuRow(
    message: MessageUiModel,
    onCancel: () -> Unit,
    onReplyClick: (MessageUiModel) -> Unit, // 👈 修正：這裡只需要定義型態
    onDeleteClick: (MessageUiModel) -> Unit,
    onEditClick: (MessageUiModel) -> Unit, // 🎯 新增
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    // ... (rest of the function)
    // 💡 取得系統剪貼簿，用來實作「複製文字」功能
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 自動適應 Android 系統導覽列（手勢列）高度
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {


        //glass effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(10.dp)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左側：取消關閉選單按鈕
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消選取",
                    tint = Color.Gray
                )
            }

            // 中間：各個功能按鈕
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 回覆
                ChatMenuButton(
                    imageVector = Icons.Default.Reply,
                    label = "回覆",
                    onClick = { onReplyClick(message) }
                )

                // 2. 複製文字
                val isImage = message.isImage
                ChatMenuButton(
                    imageVector = Icons.Default.ContentCopy,
                    label = "複製",
                    enabled = !isImage,
                    onClick = {
                        val text = message.content
                        clipboardManager.setText(AnnotatedString(text))
                        onCancel()
                    }
                )

                // 3. 編輯：只有在自己的文字訊息時顯示 🎯
                if (isOwnMessage && !isImage) {
                    ChatMenuButton(
                        imageVector = Icons.Default.Edit,
                        label = "編輯",
                        onClick = { onEditClick(message) }
                    )
                }

                // 4. 刪除：只有在自己的訊息時才顯示 👈
                if (isOwnMessage) {
                    ChatMenuButton(
                        imageVector = Icons.Default.Delete,
                        label = "刪除",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { onDeleteClick(message) }
                    )
                }
            }

            // 為了讓中間的按鈕群置中，右邊放一個等寬的 Spacer
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

// 💡 記得一併附上這個按鈕元件，它也是放在同一個檔案底部
@Composable
fun ChatMenuButton(
    imageVector: ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: Color = Color.White, // 💡 將這裡的 Color(0xFFDA7029) 改成 Color.White
    onClick: () -> Unit
) {
    val contentColor = if (enabled) tint else Color.LightGray.copy(alpha = 0.5f)
    // 💡 既然圖標變白了，文字建議也同步改成白色（或帶點透明度的白），在深色背景上才看得清楚
    val textColor = if (enabled) Color.White else Color.LightGray.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}