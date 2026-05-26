package com.example.login_v3.home.Message.UI.Detail

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.SendMessageState
import com.example.login_v3.home.Message.ViewModel.UserStatus
import com.example.login_v3.navigation.BottomBarViewModel
import com.example.login_v3.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageMessaging(
    roomId: String,
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //send message
    val sendStatus by viewModel.sendMessageState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //reply function
    val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()

    //進入時重整
    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose { bottomBarViewModel.setVisible(true) }
    }

    // ✨ 監聽發送狀態，如果失敗了就彈出提示並重置狀態
    LaunchedEffect(sendStatus) {
        if (sendStatus is SendMessageState.Error) {
            val errorMsg = (sendStatus as SendMessageState.Error).message
            Toast.makeText(context, "發送失敗: $errorMsg", Toast.LENGTH_SHORT).show()
            viewModel.resetSendMessageState()
        } else if (sendStatus is SendMessageState.Success) {
            // 發送成功後，重置狀態以便下一次發送
            viewModel.resetSendMessageState()

            // 【優化體驗】：你可以在這裡重新呼叫 viewModel.loadMessages(roomId) 刷新列表
            // 或者如果你的 ViewModel 已經會自動把新訊息塞進 uiState，這裡就什麼都不用做。
            viewModel.loadMessages(roomId)
        }
    }

    // 根據目前的 uiState 來動態決定 TopBar 要顯示什麼文字
    val topBarTitle = when (val state = uiState) {
        is MessagesUiState.Success -> state.roomTitle
        is MessagesUiState.Error -> "載入失敗"
        is MessagesUiState.Loading -> "載入中..."
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf
                        (Color(0xFFDA7029),
                        Color(0xFF777777),
                        Color(0xFFB34800))
                )
            ),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //avatar
                        if (uiState is MessagesUiState.Success) {
                            val successState = uiState as MessagesUiState.Success
                            UserAvatar(
                                avatarUrl = successState.partnerAvatarUrl,
                                modifier = Modifier.size(36.dp) // 適中的 TopBar 頭像大小
                            )
                        }

                        Text(text = topBarTitle)

                        // ✨ 如果是 Success 狀態，且對方是在線狀態（ONLINE），就顯示綠色小圓點
                        if (uiState is MessagesUiState.Success) {
                            val successState = uiState as MessagesUiState.Success
                            if (successState.partnerStatus == UserStatus.ONLINE) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = UserStatus.ONLINE.color,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            // 💡 這裡把 replyingMessage 與 取消回覆的動作 傳進去 (等等修改 MessageInputBar)
            MessageInputBar(
                isLoading = sendStatus is SendMessageState.Loading,
                replyingMessage = replyingMessage,
                onCancelReply = { viewModel.setReplyingMessage(null) },
                onSendClick = { text ->
                    viewModel.sendMessage(roomId = roomId, content = text)
                },
                onImageSelected = { uri ->
                    viewModel.uploadAttachment(roomId = roomId, fileUri = uri)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ,
            contentAlignment = Alignment.Center
        ) {
            // 根據不同的狀態渲染主畫面內容
            when (val state = uiState) {
                is MessagesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is MessagesUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        Text("目前沒有新訊息")
                    } else {
                        MessageList(
                            currentUserId = state.currentUserId,
                            messages = state.messages,
                            partnerAvatarUrl = state.partnerAvatarUrl,
                            partnerDisplayName = state.roomTitle,
                            onReplyClick = { message -> viewModel.setReplyingMessage(message) }
                        )
                    }
                }
                is MessagesUiState.Error -> {
                    Text(
                        text = "載入失敗：${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MessageList(
    currentUserId: String,
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    messages: List<Message>,
    onReplyClick: (Message) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true // ✨ 保持 true，讓畫面底部作為起點，並預設滾動到最下方
    ) {
        items(messages) { message -> // ✨ 直接帶入原始的 messages
            val isMe = message.senderId == currentUserId
            MessageRow(
                message = message,
                partnerAvatarUrl = partnerAvatarUrl,
                partnerDisplayName = partnerDisplayName,
                isMe = isMe,
                currentUserId = currentUserId,
                onReplyClick = onReplyClick
            )
        }
    }
}

@Composable
fun MessageRow(
    message: Message,
    isMe: Boolean,
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    currentUserId: String,
    onReplyClick: (Message) -> Unit // 💡 新增參數
) {
    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
    val contentLowerCase = (message.content ?: "").lowercase()
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
    val isLegacyImage = contentLowerCase.startsWith("http") && imageExtensions.any { contentLowerCase.contains(it) }
    val isImage = isAttachmentImage || isLegacyImage

    val rawContent = message.content ?: ""
    val finalImageModel = if (rawContent.startsWith("/")) "http://192.168.0.217$rawContent" else rawContent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 💡 讓使用者長按整條訊息就能觸發回覆
            .combinedClickable(
                onLongClick = { onReplyClick(message) },
                onClick = { /* 可做其他事或留空 */ }
            ),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            UserAvatar(
                avatarUrl = partnerAvatarUrl,
                modifier = Modifier.padding(end = 8.dp).size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMe) Color(0xFFDCF8C6) else Color(0xFFECEFF1),
                    shape = RoundedCornerShape(
                        topStart = 12.dp, topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // 💡 【核心：渲染被回覆的訊息內容】
                message.repliedMessage?.let { replied ->
                    val repliedSenderName = if (replied.senderId == currentUserId) "你" else partnerDisplayName

                    // 被回覆的小氣泡外框
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            // 💡 修正這裡：直接根據 isMe 決定傳入哪一個 Color 物件即可
                            .background(if (isMe) Color(0xFFC7EBB2) else Color(0xFFE0E0E0))
                            .drawBehind {
                                // 在左側畫一條精緻的提示線 (類似 LINE / Telegram)
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 6f
                                )
                            }
                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = repliedSenderName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E88E5),
                                maxLines = 1
                            )
                            Text(
                                // 如果被回覆的是圖片，顯示 [圖片]，否則顯示文字內容
                                text = if (replied.attachment?.mimeType?.startsWith("image/") == true) "[圖片]" else replied.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                maxLines = 1, // 最多一行，避免太長
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. 原本的訊息主體內容（文字或圖片）
                if (isImage) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .widthIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = finalImageModel,
                            contentDescription = "聊天圖片",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        )
                    }
                } else {
                    Text(
                        text = rawContent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun MessageInputBar(
    isLoading: Boolean,
    replyingMessage: Message?,         // 💡 新增
    onCancelReply: () -> Unit,         // 💡 新增
    onSendClick: (String) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    var textState by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // 或者是你的背景色
    ) {
        // 💡 如果目前處於回覆狀態，就在輸入框上方橫向塞一個預覽 UI
        AnimatedVisibility(visible = replyingMessage != null) {
            if (replyingMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "正在回覆訊息",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFDA7029)
                        )
                        Text(
                            text = replyingMessage.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // 取消回覆的 X 按鈕
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

        // 下方維持你原本既有的輸入框 Row 排版
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 你原本的 TextField, 選擇圖片按鈕, 送出按鈕...
            // 送出時呼叫：
            // onSendClick(textState)
            // textState = ""
        }
    }
}


@Composable
fun UserAvatar(
    avatarUrl: Any?, // ✨ 改成 Any?
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = avatarUrl ?: R.drawable.avatar_v1, // 如果為 null，就用預設圖
        contentDescription = "用戶頭像",
        modifier = modifier
            .background(Color.LightGray, shape = CircleShape)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}