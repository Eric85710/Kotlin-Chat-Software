package com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageEmojiBar(
    onEmojiClick: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {

    val emojiList = remember {
        listOf(
            "👍", "❤️", "😂", "😮", "😢", "🙏",
            "🔥", "🎉", "👏", "👀", "✨", "💯",
            "🤔", "🥳", "😎", "🚀", "💔", "💩"
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF5F5F5), // 稍微與純白底色做點區隔，可自行調整
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding() // 避免被手機底部的導覽橫條遮擋
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左側：Emoji 點擊區
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                emojiList.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 26.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onEmojiClick(emoji) }
                            .padding(4.dp)
                    )
                }
            }

            // 右側：關閉按鈕 (Icon 可以換成你專案內現有的)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onCancel() }
                    .padding(4.dp)
            ) {
                Text(text = "✕", color = Color.Gray, fontSize = 18.sp)
            }
        }
    }
}