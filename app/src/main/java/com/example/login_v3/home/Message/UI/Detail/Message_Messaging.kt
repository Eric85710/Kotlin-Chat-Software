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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.Reaction
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.ReactionUsersState
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

    // 💡 1. 取得 ViewModel 裡的 Reaction 狀態
    val reactionState by viewModel.reactionUsersState.collectAsStateWithLifecycle()
    // 控制 BottomSheet 顯示的變數
    var showReactionBottomSheet by remember { mutableStateOf(false) }

    // 當狀態變成 Success 且不是空名單時，或者正在 Loading，就開啟 BottomSheet
    LaunchedEffect(reactionState) {
        if (reactionState is ReactionUsersState.Success || reactionState is ReactionUsersState.Loading) {
            showReactionBottomSheet = true
        }
    }
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

    // 💡 2. 渲染 BottomSheet 視窗
    if (showReactionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showReactionBottomSheet = false
                viewModel.resetReactionUsersState() // 關閉時記得清空狀態，避免下次閃出舊資料
            },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "按讚的名單",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (val state = reactionState) {
                    is ReactionUsersState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                    is ReactionUsersState.Success -> {
                        if (state.users.isEmpty()) {
                            Text("目前沒有人點擊", color = Color.Gray)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.users) { userName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        // 這裡可以用一個預設頭像，或是單純顯示文字
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(36.dp).padding(end = 8.dp)
                                        )
                                        Text(
                                            text = userName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is ReactionUsersState.Error -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }
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
                            onReplyClick = { message -> viewModel.setReplyingMessage(message) },
                            onReactionClick = { message, emoji ->
                                viewModel.loadMessageReactionUsers(
                                    roomId = roomId,
                                    messageId = message.id,
                                    emoji = emoji
                                )
                            },
                            onAddReaction = { message, emoji ->
                                viewModel.addMessageReaction(
                                    roomId = roomId,
                                    messageId = message.id,
                                    emoji = emoji
                                )
                            }
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
    onReactionClick: (Message, String) -> Unit,
    onAddReaction: (Message, String) -> Unit,
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
                onReplyClick = onReplyClick,
                onReactionClick = onReactionClick,
                onAddReaction = onAddReaction
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
    onReplyClick: (Message) -> Unit,
    onReactionClick: (Message, String) -> Unit,
    onAddReaction: (Message, String) -> Unit
) {
    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
    val contentLowerCase = (message.content ?: "").lowercase()
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
    val isLegacyImage = contentLowerCase.startsWith("http") && imageExtensions.any { contentLowerCase.contains(it) }
    val isImage = isAttachmentImage || isLegacyImage

    val rawContent = message.content ?: ""
    val finalImageModel = if (rawContent.startsWith("/")) "http://192.168.0.217$rawContent" else rawContent

    //emoji value
    var showEmojiPanel by remember { mutableStateOf(false) }
    val commonEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 💡 讓使用者長按整條訊息就能觸發回覆
            .combinedClickable(
                onLongClick = { onReplyClick(message) },
                onClick = { showEmojiPanel = true }     // 💡 點一下開啟 Emoji 面板
            ),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            UserAvatar(
                avatarUrl = partnerAvatarUrl,
                modifier = Modifier.padding(end = 8.dp).size(32.dp)
            )
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
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

                DropdownMenu(
                    expanded = showEmojiPanel,
                    onDismissRequest = { showEmojiPanel = false },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        commonEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onAddReaction(message, emoji) // 觸發新增 Reaction API
                                        showEmojiPanel = false        // 關閉面板
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // 💡 ✨ 新增：如果這條訊息有 Reaction，就顯示在氣泡的正下方
            if (!message.reactions.isNullOrEmpty()) {
                ReactionRow(
                    reactions = message.reactions,
                    onReactionClick = { emoji -> onReactionClick(message, emoji) }
                )
            }
        }
    }
}


@Composable
fun MessageInputBar(
    isLoading: Boolean,
    replyingMessage: Message?,
    onCancelReply: () -> Unit,
    onSendClick: (String) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    var textState by remember { mutableStateOf("") }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {
                onImageSelected(it)
            }
        }

    // 💡 使用打包器，確保軟鍵盤彈出時系統會正確計算高度
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        color = Color.White // 確保底色不透明，不會透出後方的聊天訊息
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. 回覆預覽列
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

            // 2. 💡 實際的輸入工具列 (確保有 padding 且元件垂直置中)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 圖片選擇按鈕 (範例，可換成你的實作)
                // ✅ 圖片按鈕
                IconButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "選擇圖片",
                        tint = Color.Gray
                    )
                }

                // 輸入文字框
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    placeholder = { Text("請輸入訊息...") },
                    maxLines = 4, // 避免使用者打太多字時無限長高
                    shape = RoundedCornerShape(20.dp)
                )

                // 送出按鈕
                IconButton(
                    onClick = {
                        if (textState.isNotBlank() && !isLoading) {
                            onSendClick(textState)
                            textState = "" // 送出後清空輸入框
                        }
                    },
                    enabled = textState.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "送出", tint = Color(0xFFDA7029))
                    }
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