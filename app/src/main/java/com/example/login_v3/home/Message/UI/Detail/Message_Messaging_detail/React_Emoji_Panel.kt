package com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object EmojiProvider {
    fun getFullEmojiPack(): List<String> {
        val emojis = mutableListOf<String>()

        // 1. 經典表情與人類行為 (Smiley Faces & People)
        // 包含：😀 🤣 😘 🤠 🧑‍⚕️ 🏃 等
        for (i in 0x1F600..0x1F64F) {
            addCodePointIfValid(i, emojis)
        }

        // 2. 身體部位、手勢與愛心 (Body, Gestures & Hearts)
        // 包含：👋 👍 👏 💪 👁️ ❤️ 💔 等
        for (i in 0x1F440..0x1F49F) {
            addCodePointIfValid(i, emojis)
        }

        // 3. 額外追加的表情與角色 (Extended Smileys & Symbols)
        // 包含：🥰 🤩 🥳 🥵 🥶 🥺 等較新版表情
        for (i in 0x1F900..0x1F9FF) {
            addCodePointIfValid(i, emojis)
        }

        // 4. 動物與大自然 (Animals & Nature)
        // 包含：🐶 🐱 🦁 🦊 🐵 🦅 🌲 🌸 🌞 等
        for (i in 0x1F400..0x1F43F) { // 常見動物頭像
            addCodePointIfValid(i, emojis)
        }
        for (i in 0x1F1E6..0x1F1FF) { // 補充大自然元素
            addCodePointIfValid(i, emojis)
        }

        // 5. 食物與飲料 (Food & Drink)
        // 包含：🍏 🍓 🍔 🍕 🍣 🍦 🍺 ☕️ 等
        for (i in 0x1F354..0x1F37F) {
            addCodePointIfValid(i, emojis)
        }

        // 6. 運動、娛樂與活動 (Activities & Sports)
        // 包含：⚽️ 🏀 🏈 🏆 🎮 🎤 🎬 🎨 等
        for (i in 0x1F3A0..0x1F3C4) {
            addCodePointIfValid(i, emojis)
        }

        // 7. 旅遊、交通工具與建築 (Travel & Places)
        // 包含：🚗 ✈️ 🚀 🌋 🏖️ 🏥 🇨🇳 等
        for (i in 0x1F680..0x1F6FF) {
            addCodePointIfValid(i, emojis)
        }

        // 8. 物品、工具與雜項 (Objects & Tools)
        // 包含：💻 📱 💡 🔑 🔨 📚 📦 💵 等
        for (i in 0x1F4A0..0x1F4FF) {
            addCodePointIfValid(i, emojis)
        }

        return emojis
    }

    /**
     * 輔助函式：檢查該 Unicode 編碼在當前系統是否有效，若有效則轉成 String 存入
     */
    private fun addCodePointIfValid(codePoint: Int, list: MutableList<String>) {
        if (Character.isValidCodePoint(codePoint)) {
            list.add(String(Character.toChars(codePoint)))
        }
    }
}

@Composable
fun MessageEmojiBar(
    onEmojiClick: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 💡 修正 1：改為呼叫你實際定義的 getFullEmojiPack()
    val emojiList = remember { EmojiProvider.getFullEmojiPack() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(horizontal = 6.dp, vertical = 18.dp)
    ){

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

            // 網格滾動區
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 💡 修正 2：明確指定 emoji 的型態為 String
                // 這樣可以強迫編譯器去認對應的 items 擴充函式，徹底解決 Text(text = emoji) 報 Int 的錯誤
                items(emojiList) { emoji: String ->
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