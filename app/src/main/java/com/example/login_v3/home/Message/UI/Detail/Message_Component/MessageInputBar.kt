package com.example.login_v3.home.Message.UI.Detail.Message_Component

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.content.consume
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.login_v3.home.Message.UI.Detail.MessageUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageInputBar(
    isLoading: Boolean,
    replyingMessage: MessageUiModel?,
    editingMessage: MessageUiModel? = null,
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit = {},
    onSendClick: (String) -> Unit,
    onEditSaveClick: (String) -> Unit = {},
    onAttachmentClick: () -> Unit,
    onMediaReceived: (Uri) -> Unit = {}
) {
    val textState = rememberTextFieldState()

    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            textState.setTextAndPlaceCursorAtEnd(editingMessage.content)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
            AnimatedVisibility(visible = replyingMessage != null) {
                if (replyingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

            AnimatedVisibility(visible = editingMessage != null) {
                if (editingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                textState.clearText()
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAttachmentClick() }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "attachment bar",
                        tint = Color.White
                    )
                }

                BasicTextField(
                    state = textState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .contentReceiver { transferableContent ->
                            if (transferableContent.hasMediaType(MediaType.Image)) {
                                transferableContent.consume { item ->
                                    val uri = item.uri
                                    if (uri != null) {
                                        onMediaReceived(uri)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            } else {
                                transferableContent
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrectEnabled = true,
                        imeAction = ImeAction.Default
                    ),
                    decorator = { innerTextField ->
                        if (textState.text.isEmpty()) {
                            Text(
                                text = if (editingMessage != null) "編輯訊息..." else "請輸入訊息...",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )

                IconButton(
                    onClick = {
                        val content = textState.text.toString()
                        if (content.isNotBlank() && !isLoading) {
                            if (editingMessage != null) {
                                onEditSaveClick(content)
                            } else {
                                onSendClick(content)
                            }
                            textState.clearText()
                        }
                    },
                    enabled = textState.text.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        val iconColor = if (textState.text.isNotBlank()) Color(0xFFDA7029) else Color.LightGray
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
