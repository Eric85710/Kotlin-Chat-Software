package com.example.login_v3.home.Message.UI.Detail

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

    //進入時重整
    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose { bottomBarViewModel.setVisible(true) }
    }

    // ✨ 監聽發送狀態，如果失敗了就彈出提示並重置狀態
    // 修改後的 LaunchedEffect
    LaunchedEffect(sendStatus) {
        if (sendStatus is SendMessageState.Error) {
            val errorMsg = (sendStatus as SendMessageState.Error).message
            Toast.makeText(context, "發送失敗: $errorMsg", Toast.LENGTH_SHORT).show()
            viewModel.resetSendMessageState()
        } else if (sendStatus is SendMessageState.Success) {
            // ✨ 發送成功後，只重置狀態。讓 ViewModel 的 uiState 自動推播新訊息給 UI 即可！
            viewModel.resetSendMessageState()
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
            MessageInputBar(
                isLoading = sendStatus is SendMessageState.Loading,
                onSendClick = { text ->
                    viewModel.sendMessage(roomId = roomId, content = text)
                },
                onImageSelected = { uri ->
                    // ✨ 這裡呼叫你先前在 ViewModel 寫好的上傳圖片功能
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
                            partnerDisplayName = state.roomTitle
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
                currentUserId = currentUserId
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
    currentUserId: String
) {
    // 1. ✨ 使用新的 attachment 欄位精準判斷是否為圖片，或是舊格式的網址判斷（相容舊資料）
    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true

    // 保留你原本的舊資料防禦邏輯，避免舊的訊息爆掉
    val contentLowerCase = (message.content ?: "").lowercase()
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
    val isLegacyImage = contentLowerCase.startsWith("http") &&
            imageExtensions.any { contentLowerCase.contains(it) }

    val isImage = isAttachmentImage || isLegacyImage

    // 2. 補全圖片網址
    // 2. 補全圖片網址
    val rawContent = message.content ?: ""
    val finalImageModel = if (rawContent.startsWith("/")) {
        "http://192.168.0.217$rawContent"
    } else {
        rawContent
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            UserAvatar(
                avatarUrl = partnerAvatarUrl,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMe) Color(0xFFDCF8C6) else Color(0xFFECEFF1),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
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
                                .clickable { /* 點擊放大 */ }
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
    onSendClick: (String) -> Unit,
    onImageSelected: (Uri) -> Unit, // ✨ 新增：選取圖片後的回呼
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    Surface(
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ✨ 新增：相簿按鈕
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle, // 你也可以改用 Icons.Default.Image
                    contentDescription = "選擇圖片",
                    tint = Color(0xFFDA7029)
                )
            }

            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("輸入訊息...") },
                modifier = Modifier.weight(1.0f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank() && !isLoading) {
                        onSendClick(textInput)
                        textInput = ""
                    }
                },
                enabled = textInput.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "發送訊息",
                        tint = if (textInput.isNotBlank()) Color(0xFFDA7029) else Color.Gray
                    )
                }
            }
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