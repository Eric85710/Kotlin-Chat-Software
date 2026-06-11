package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.data.api.api_class.Reaction
import kotlin.collections.forEach

@Composable
fun ReactionRow(
    reactions: List<Reaction>,
    onReactionClick: (String) -> Unit
) {
    // 使用 FlowRow，如果貼圖太多會自動折行
    OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { reaction ->
            if (reaction.count > 0) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            // 如果我自己有按讚，背景變稍微深色/藍色點綴，否則為淡灰色
                            if (reaction.meReacted) Color(0xFFBBDEFB) else Color(0xFFEEEEEE)
                        )
                        .clickable { onReactionClick(reaction.emoji) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = reaction.emoji, fontSize = 14.sp)
                    Text(
                        text = reaction.count.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (reaction.meReacted) Color(0xFF1976D2) else Color.DarkGray
                    )
                }
            }
        }
    }
}