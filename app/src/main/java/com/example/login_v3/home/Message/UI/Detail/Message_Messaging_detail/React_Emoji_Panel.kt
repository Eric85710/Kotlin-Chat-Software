package com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageEmojiBar(
    onEmojiClick: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 這裡可以無限塞入幾百個 Emoji 都沒問題了！
    val emojiList = remember {
        listOf(
            "👍", "❤️", "😂", "😮", "😢", "🙏",
            "🔥", "🎉", "👏", "👀", "✨", "💯",
            "🤔", "🥳", "😎", "🚀", "💔", "💩",
            "🤩", "🤯", "🙄", "🤫", "😴", "🤤",
            "😭", "😡", "🤡", "👻", "👽", "🤖",
            "👑", "🦄", "🐾", "🍕", "🍺", "☕️"
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp), // 保持你設定的 300.dp 固定高度
        color = Color(0xFFF5F5F5),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding() // 確保不被系統海苔條遮擋
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 【頂部功能列】：左邊放個小提示，右邊放關閉按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "傳送回應",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // 關閉按鈕
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onCancel() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.Gray, fontSize = 16.sp)
                }
            }

            // 【核心改動】：使用網格佈局，塞再多都不怕！
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(emojiList) { emoji ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f) // 讓每個格子都是正方形，比較好對齊
                            .clip(CircleShape)
                            .clickable { onEmojiClick(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 28.sp // 稍微放大一點點，比較好點擊
                        )
                    }
                }
            }
        }
    }
}