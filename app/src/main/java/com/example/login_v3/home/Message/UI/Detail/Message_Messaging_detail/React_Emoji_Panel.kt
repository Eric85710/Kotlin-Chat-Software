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

object EmojiProvider {
    fun getSmileysAndPeople(): List<String> {
        val emojis = mutableListOf<String>()

        // 1. 經典表情區間 (Smiley Faces): 😀 到 🪟
        for (i in 0x1F600..0x1F64F) {
            if (Character.isValidCodePoint(i)) {
                emojis.add(String(Character.toChars(i)))
            }
        }

        // 2. 常用手勢與身體部位 (Body & Gestures): 👍 👏 等
        for (i in 0x1F440..0x1F49F) {
            if (Character.isValidCodePoint(i)) {
                emojis.add(String(Character.toChars(i)))
            }
        }

        return emojis
    }
}

@Composable
fun MessageEmojiBar(
    onEmojiClick: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // emoji pack
    val emojiList = remember { EmojiProvider.getSmileysAndPeople() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        color = Color(0xFFF5F5F5),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 頂部功能列 (標題與叉叉)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "傳送回應", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Box(
                    modifier = Modifier.clip(CircleShape).clickable { onCancel() }.padding(8.dp)
                ) {
                    Text(text = "✕", color = Color.Gray, fontSize = 16.sp)
                }
            }

            // 網格滾動區：塞了幾百個也能流暢滑動
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(emojiList) { emoji ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .clickable { onEmojiClick(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    }
}