package com.example.login_v3.home.Message.UI.Detail.Message_Component

import android.R.attr.onClick
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.login_v3.data.api.api_class.Message

@Composable
fun MessageInputBar(
    isLoading: Boolean,
    replyingMessage: Message?,
    onCancelReply: () -> Unit,
    onSendClick: (String) -> Unit,
    onAttachmentClick: () -> Unit
) {
    var textState by remember { mutableStateOf("") }

    // 💡 外層包裹一層 Box，並加上 padding 與 navigationBarsPadding，確保懸浮且不被系統列擋住
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 自動適應 Android 系統導覽列高度
            .padding(horizontal = 16.dp, vertical = 8.dp) // 懸浮的外邊距
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

        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. 回覆預覽列 (加入頂部圓角修飾)
            AnimatedVisibility(visible = replyingMessage != null) {
                if (replyingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA)) // 更輕盈的底色
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = replyingMessage.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "取消回覆",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // 2. 實際的輸入工具列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 檔案上傳
                IconButton(onClick = { onAttachmentClick() }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "attachment bar",
                        tint = Color.White
                    )
                }

                // 輸入文字框 (改為內嵌樣式，去掉自帶的邊框，讓整體更有一體感)
                BasicTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    decorationBox = { innerTextField ->
                        if (textState.isEmpty()) {
                            Text("請輸入訊息...", color = Color.LightGray)
                        }
                        innerTextField()
                    }
                )

                // 送出按鈕
                IconButton(
                    onClick = {
                        if (textState.isNotBlank() && !isLoading) {
                            onSendClick(textState)
                            textState = ""
                        }
                    },
                    enabled = textState.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        // 加上小背景讓送出按鈕更有重點
                        val iconColor = if (textState.isNotBlank()) Color(0xFFDA7029) else Color.LightGray
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "送出",
                            tint = iconColor
                        )
                    }
                }
            }
        }
    }
}