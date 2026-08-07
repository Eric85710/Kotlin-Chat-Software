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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // 🎯 新增
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.login_v3.home.Message.UI.Detail.MessageUiModel

@Composable
fun MessageInputBar(
    isLoading: Boolean,
    replyingMessage: MessageUiModel?,
    editingMessage: MessageUiModel? = null, // 🎯 新增
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit = {},          // 🎯 新增
    onSendClick: (String) -> Unit,
    onEditSaveClick: (String) -> Unit = {}, // 🎯 新增
    onAttachmentClick: () -> Unit
) {
    var textState by remember { mutableStateOf("") }

    // 🎯 當進入編輯模式時，自動填入原本的內容
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            textState = editingMessage.content
        } else {
            // 如果從編輯模式退出且不是因為發送成功，可能需要清空？
            // 但通常 clearEditingMessage 會觸發這裡
        }
    }

    // 💡 外層包裹一層 Box，並加上 padding 與 navigationBarsPadding，確保懸浮且不被系統列擋住
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 自動適應 Android 系統導覽列高度
            .padding(horizontal = 16.dp, vertical = 8.dp) // 懸浮的外邊距

            //color
            .background(
                color = MaterialTheme.colorScheme.primary,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
    ) {


        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. 回覆預覽列 (現代化樣式 + 圓角與間距)
            AnimatedVisibility(visible = replyingMessage != null) {
                if (replyingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp) // 🎯 與邊框保持間距
                            .background(
                                color = Color.White.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(12.dp) // 🎯 圓角
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左側綠色裝飾條
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

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
                                tint = Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 🎯 1.5 編輯預覽列 (現代化樣式 + 圓角與間距)
            AnimatedVisibility(visible = editingMessage != null) {
                if (editingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp) // 🎯 與邊框保持間距
                            .background(
                                color = Color.White.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(12.dp) // 🎯 圓角
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左側橘色裝飾條，提升現代感
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(Color(0xFFDA7029), RoundedCornerShape(2.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFFDA7029),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "正在編輯訊息",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDA7029)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                onCancelEdit()
                                textState = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "取消編輯",
                                tint = Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp)) // 🎯 提供預覽列與輸入框之間的間距

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
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    decorationBox = { innerTextField ->
                        if (textState.isEmpty()) {
                            Text(
                                text = if (editingMessage != null) "編輯訊息..." else "請輸入訊息...",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )

                // 送出按鈕
                IconButton(
                    onClick = {
                        if (textState.isNotBlank() && !isLoading) {
                            if (editingMessage != null) {
                                onEditSaveClick(textState)
                            } else {
                                onSendClick(textState)
                            }
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
                        val icon = if (editingMessage != null) Icons.Default.Check else Icons.Default.Send
                        Icon(
                            imageVector = icon,
                            contentDescription = if (editingMessage != null) "儲存編輯" else "送出",
                            tint = iconColor
                        )
                    }
                }
            }
        }
    }
}