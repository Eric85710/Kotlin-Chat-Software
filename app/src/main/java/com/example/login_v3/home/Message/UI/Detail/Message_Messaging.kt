package com.example.login_v3.home.Message.UI.Detail

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.UI.Detail.Message_Component.MessageActionMenuRow
import com.example.login_v3.home.Message.UI.Detail.Message_Component.MessageEmojiBar
import com.example.login_v3.home.Message.UI.Detail.Message_Component.MessageInputBar
import com.example.login_v3.home.Message.UI.Detail.Message_Component.ReactionRow
import com.example.login_v3.home.Message.UI.Detail.Message_Component.UserAvatar
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.DeleteMessageState
import com.example.login_v3.home.Message.ViewModel.Detail.MessageStatus
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.SendMessageState
import com.example.login_v3.navigation.BottomBarViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars // 如果要使用 statusBars 也需要這個
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import com.example.login_v3.data.api.api_class.CallLogInfo
import com.example.login_v3.home.Message.UI.Detail.Message_Component.CallLogBubble
import com.example.login_v3.home.Message.UI.Detail.Message_Component.ImageLightbox
import com.example.login_v3.home.Message.UI.Detail.Message_Component.UserStatusDot


//bottom bar state
sealed interface BottomBarState {
    object Input : BottomBarState                                // 預設：輸入框
    data class ActionMenu(val message: Message) : BottomBarState // 長按：動作選單
    data class EmojiMenu(val message: Message) : BottomBarState  // 單擊：Emoji 工具列
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageMessaging(
    roomId: String,
    navController: NavController,
    onBackClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ChatViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //send message
    val sendStatus by viewModel.sendMessageState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //reply function
    val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()

    //message that selected
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    var emojiTargetMessage by remember { mutableStateOf<Message?>(null) }

    //inspect image
    var lightboxImageUrl by remember { mutableStateOf<String?>(null) }

    // 🌟 核心：使用 derivedStateOf 統一控管狀態權限（長按優先權通常大於單擊）
    val bottomBarState by remember {
        derivedStateOf {
            when {
                actionMessage != null -> BottomBarState.ActionMenu(actionMessage!!)
                emojiTargetMessage != null -> BottomBarState.EmojiMenu(emojiTargetMessage!!)
                else -> BottomBarState.Input
            }
        }
    }

    //進入時重整
    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    //bottom bar on and off
    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose { bottomBarViewModel.setVisible(true) }
    }

    LaunchedEffect(sendStatus) {
        when (sendStatus) {
            is SendMessageState.Error -> {
                Toast.makeText(context, "發送失敗: ...", Toast.LENGTH_SHORT).show()
                viewModel.resetSendMessageState()
            }
            is SendMessageState.Success -> {
                // 收到 Success() 訊號，輸入框清空文字，回歸正常可用狀態
                viewModel.resetSendMessageState()
            }
            else -> {}
        }
    }

    val deleteState by viewModel.deleteMessageState.collectAsState()

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteMessageState.Error) {
            // 當背景 API 刪除失敗、訊息彈回來時，給使用者一個提示
            val errorMsg = (deleteState as DeleteMessageState.Error).message
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // 根據目前的 uiState 來動態決定 TopBar 要顯示什麼文字
    val topBarTitle = when (val state = uiState) {
        is MessagesUiState.Success -> state.roomTitle
        is MessagesUiState.Error -> "載入失敗"
        is MessagesUiState.Loading -> ""
    }


    with(sharedTransitionScope){
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
                Column() {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                    val topBarShape = RoundedCornerShape(16.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                        ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))

                        //name and avatar
                        Box(
                            modifier = Modifier
                                .weight(0.68f)
                                .sharedElement(
                                    rememberSharedContentState(key = "container_$roomId"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .clip(topBarShape)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = topBarShape
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    shape = topBarShape
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp) // 58dp 就會是純內容的高度，內容就會完美垂直置中了！
                                    .padding(horizontal = 12.dp)
                            ) {
                                val currentState = uiState
                                if (currentState is MessagesUiState.Success) {
                                    UserAvatar(
                                        avatarUrl = currentState.partnerAvatarUrl,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = topBarTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )


                                if (currentState is MessagesUiState.Success) {
                                    // 拿掉原本只判斷 ONLINE 的 if，讓所有狀態（除了 UNKNOWN）都能顯示
                                    UserStatusDot(
                                        status = currentState.partnerStatus,
                                        size = 14.dp // 建議統一成 14.dp，包含白色外圈的視覺效果最好
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        //voice call button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(0.16f)
                                .height(58.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = topBarShape
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    shape = topBarShape
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    // 這裡觸發語音通話的邏輯，例如：viewModel.startVoiceCall(roomId)
                                },
                                modifier = Modifier
                                    .size(48.dp) // 給予標準的點擊區域大小
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call, // 👈 這裡使用 Material Design 的預設電話圖標，也可以換成 Icons.Rounded.Phone
                                    contentDescription = "語音通話",
                                    tint = Color.White, // 👈 顏色可以根據你的漸層背景調整，如果是亮色背景可改用 MaterialTheme.colorScheme.onBackground
                                    modifier = Modifier.size(28.dp) // Icon 實際的大小
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        //more option button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(0.16f)
                                .height(58.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = topBarShape
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    shape = topBarShape
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    // 這裡觸發語音通話的邏輯，例如：viewModel.startVoiceCall(roomId)
                                },
                                modifier = Modifier
                                    .size(48.dp) // 給予標準的點擊區域大小
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert, // 👈 這裡使用 Material Design 的預設電話圖標，也可以換成 Icons.Rounded.Phone
                                    contentDescription = "語音通話",
                                    tint = Color.White, // 👈 顏色可以根據你的漸層背景調整，如果是亮色背景可改用 MaterialTheme.colorScheme.onBackground
                                    modifier = Modifier.size(28.dp) // Icon 實際的大小
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            ,
            bottomBar = {
                // 👈 使用我們定義的 sealed interface 狀態機
                AnimatedContent(
                    targetState = bottomBarState,
                    label = "BottomBarSwitchAnimation"
                ) { currentState ->
                    when (currentState) {
                        is BottomBarState.Input -> {
                            MessageInputBar(
                                isLoading = sendStatus is SendMessageState.Loading,
                                replyingMessage = replyingMessage,
                                onCancelReply = { viewModel.setReplyingMessage(null) },
                                onSendClick = { content -> viewModel.sendMessage(roomId, content) },
                                onImageSelected = { uri -> viewModel.uploadAttachment(roomId, uri) },
                            )
                        }

                        is BottomBarState.ActionMenu -> {
                            val currentActionMessage = currentState.message
                            val currentUserId = (uiState as? MessagesUiState.Success)?.currentUserId
                            val isOwnMessage = currentActionMessage.senderId == currentUserId

                            MessageActionMenuRow(
                                message = currentActionMessage,
                                isOwnMessage = isOwnMessage,
                                onCancel = { viewModel.clearActionMessage() },
                                onReplyClick = { msg ->
                                    viewModel.setReplyingMessage(msg)
                                    viewModel.clearActionMessage()
                                },
                                onDeleteClick = { msg ->
                                    // 🌟 核心防呆：如果訊息還在發送中(SENDING)，禁止點擊刪除，避免 tempId 送給後端造成 404
                                    if (msg.status == MessageStatus.SENDING) {
                                        // 可以選擇在這裡彈出一個短暫的 Toast 提示使用者
                                        Toast.makeText(context, "訊息發送中，請稍後再操作", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 正常訊息，呼叫樂觀更新刪除
                                        viewModel.deleteMessage(roomId, msg.id)
                                    }
                                }
                            )
                        }

                        is BottomBarState.EmojiMenu -> {
                            // 👈 渲染全新設計的獨立 Emoji Bar
                            val currentEmojiMessage = currentState.message
                            MessageEmojiBar(
                                // 🌟 移除了 commonEmojis 參數，變得非常清爽！
                                onEmojiClick = { emoji ->
                                    val targetReaction = currentEmojiMessage.reactions?.find { it.emoji == emoji }
                                    if (targetReaction?.meReacted == true) {
                                        viewModel.removeMessageReaction(roomId, currentEmojiMessage.id, emoji)
                                    } else {
                                        viewModel.addMessageReaction(roomId, currentEmojiMessage.id, emoji)
                                    }
                                    emojiTargetMessage = null // 點完後自動關閉
                                },
                                onCancel = {
                                    emojiTargetMessage = null // 點擊關閉
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                                activeEmojiMessageId = emojiTargetMessage?.id,   // ✨ 新增短按目標 ID
                                activeActionMessageId = actionMessage?.id,
                                onReplyClick = { message ->
                                    emojiTargetMessage = null
                                    viewModel.setActionMessage(message)
                                },
                                // 💡 單擊事件：開啟 Emoji 選單，同時清空長按選單
                                onRowClick = { message ->
                                    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
                                    val contentLowerCase = (message.content ?: "").lowercase()
                                    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
                                    val isLegacyImage = imageExtensions.any { contentLowerCase.contains(it) }

                                    if (isAttachmentImage || isLegacyImage) {
                                        // 🎯 A 狀態：這是圖片，計算圖片完整 URL 並塞入 lightbox 狀態觸發放大
                                        val rawPath = if (isAttachmentImage && !message.attachment?.filename.isNullOrBlank()) {
                                            message.attachment!!.filename
                                        } else {
                                            message.content ?: ""
                                        }
                                        val baseUrl = "https://tg.technologia-tw.com"
                                        val finalUrl = when {
                                            rawPath.isBlank() -> ""
                                            rawPath.startsWith("http") -> rawPath
                                            else -> {
                                                val cleanPath = rawPath.removePrefix("/").removePrefix("uploads/")
                                                if (cleanPath.startsWith("attachment/")) {
                                                    "$baseUrl/uploads/$cleanPath"
                                                } else {
                                                    "$baseUrl/uploads/attachment/$cleanPath"
                                                }
                                            }
                                        }

                                        // 關閉可能開啟的選單，並打開大圖
                                        viewModel.clearActionMessage()
                                        emojiTargetMessage = null
                                        lightboxImageUrl = finalUrl
                                    } else {
                                        // 🎯 B 狀態：一般文字或通話，維持原本點擊叫出 Emoji Bar 的邏輯
                                        viewModel.clearActionMessage()
                                        emojiTargetMessage = message
                                    }
                                },
                                onReactionClick = { message, emoji ->
                                    // 這是訊息氣泡下方既有 Reaction 小標籤的點擊事件，保持原樣即可
                                    val targetReaction =
                                        message.reactions?.find { it.emoji == emoji }
                                    if (targetReaction?.meReacted == true) {
                                        viewModel.removeMessageReaction(roomId, message.id, emoji)
                                    } else {
                                        viewModel.addMessageReaction(roomId, message.id, emoji)
                                    }
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

    lightboxImageUrl?.let { url ->
        ImageLightbox(
            imageUrl = url,
            onDismiss = { lightboxImageUrl = null }
        )
    }
}

@Composable
fun MessageList(
    currentUserId: String,
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    messages: List<Message>,
    activeEmojiMessageId: String?,
    activeActionMessageId: String?,
    onReplyClick: (Message) -> Unit,
    onReactionClick: (Message, String) -> Unit,
    onRowClick: (Message) -> Unit,
    modifier: Modifier = Modifier
) {
    // 🌟 核心修正：過濾掉所有已被刪除的訊息，讓它們直接不參與渲染
    val visibleMessages = remember(messages) {
        messages.filter { !it.isDeleted }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        reverseLayout = true // ✨ 保持 true，讓畫面底部作為起點，並預設滾動到最下方
    ) {
        // 💡 改用過濾後的全新列表 visibleMessages
        items(visibleMessages) { message ->
            val isMe = message.senderId == currentUserId
            MessageRow(
                message = message,
                partnerAvatarUrl = partnerAvatarUrl,
                partnerDisplayName = partnerDisplayName,
                isMe = isMe,
                isHighlight = message.id == activeEmojiMessageId || message.id == activeActionMessageId,
                currentUserId = currentUserId,
                onReplyClick = onReplyClick,
                onReactionClick = onReactionClick,
                onRowClick = onRowClick
            )
        }
    }
}

private fun String?.isNullTabOrBlank(): Boolean = this == null || this.trim().isBlank()
@Composable
fun MessageRow(
    message: Message,
    isMe: Boolean,
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    isHighlight: Boolean,
    currentUserId: String,
    onReplyClick: (Message) -> Unit,
    onReactionClick: (Message, String) -> Unit,
    onRowClick: (Message) -> Unit,
) {
    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
    val contentLowerCase = (message.content ?: "").lowercase()
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")

    val isLegacyImage = imageExtensions.any { contentLowerCase.contains(it) }
    val isImage = isAttachmentImage || isLegacyImage
    val rawContent = message.content ?: ""

    val callLog = message.callLogInfo

    val finalImageModel = remember(message) {
        val rawPath = if (isAttachmentImage && !message.attachment?.filename.isNullOrBlank()) {
            message.attachment!!.filename
        } else {
            message.content ?: ""
        }
        val baseUrl = "https://tg.technologia-tw.com"
        when {
            rawPath.isBlank() -> ""
            rawPath.startsWith("http") -> rawPath
            else -> {
                val cleanPath = rawPath.removePrefix("/").removePrefix("uploads/")
                if (cleanPath.startsWith("attachment/")) {
                    "$baseUrl/uploads/$cleanPath"
                } else {
                    "$baseUrl/uploads/attachment/$cleanPath"
                }
            }
        }
    }

    val finalBubbleColor = remember(message.status, isHighlight, callLog) {
        val baseColor = when {
            callLog != null -> {
                if (callLog.type == "call_missed") Color(0xFFFCE8E6) else Color(0xFFF1F3F4)
            }
            isMe && isHighlight -> Color(0xFFB4E197)
            isMe -> Color(0xFFDCF8C6)
            !isMe && isHighlight -> Color(0xFFCFD8DC)
            else -> Color(0xFFECEFF1)
        }
        if (message.status == MessageStatus.SENDING) baseColor.copy(alpha = 0.75f) else baseColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isHighlight) Modifier.background(Color(0x33FF9800), RoundedCornerShape(12.dp)) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            if (!isMe) {
                UserAvatar(
                    avatarUrl = partnerAvatarUrl,
                    modifier = Modifier.padding(end = 8.dp).size(32.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isMe && message.status == MessageStatus.FAILED) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = "發送失敗",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp).clickable { /* 重發邏輯 */ }
                    )
                }

                //three type of message
                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {

                    // 核心分流：如果是圖片，不穿氣泡外衣，直接渲染
                    if (isImage) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 240.dp) // 限制圖片最大寬度
                                .combinedClickable(
                                    onLongClick = { onReplyClick(message) },
                                    onClick = { onRowClick(message) }
                                ),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            // 1. 如果有被回覆的訊息，依然顯示在圖片上方（可選，如果不需要也可以移進去文字氣泡）
                            message.repliedMessage?.let { replied ->
                                RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                            }

                            // 2. 獨立的圖片元件（完全沒有外層氣泡背景色與 Padding）
                            AsyncImage(
                                model = finalImageModel,
                                contentDescription = "聊天圖片",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .sizeIn(maxWidth = 240.dp, maxHeight = 300.dp)
                                    .clip(RoundedCornerShape(12.dp)) // 圖片自帶圓角，更美觀
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(Color(0xFFF5F5F5)),
                                onError = { errorState ->
                                    android.util.Log.e("ChatImageError", "原因: ${errorState.result.throwable}")
                                }
                            )
                        }
                    } else if (callLog != null) {
                        // 🌟 🎯 改造 2：通話紀錄渲染優化
                        // 移除原本包裹在外層、帶有背景與 Padding 的 Box，改由長按/點擊事件的容器包住 CallLogBubble
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                // 讓通話紀錄一樣能響應點擊與長按（如刪除、回覆）
                                .combinedClickable(
                                    onLongClick = { onReplyClick(message) },
                                    onClick = { onRowClick(message) }
                                )
                        ) {
                            CallLogBubble(
                                callLog = callLog,
                                isMe = isMe,
                                currentUserId = currentUserId,
                                partnerDisplayName = partnerDisplayName
                            )
                        }
                    } else {
                        // 🌟 文字訊息：維持原本的 Box 氣泡樣式
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp, topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                    )
                                )
                                .combinedClickable(
                                    onLongClick = { onReplyClick(message) },
                                    onClick = { onRowClick(message) }
                                )
                                .background(color = finalBubbleColor)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                message.repliedMessage?.let { replied ->
                                    RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                                }

                                Text(
                                    text = rawContent,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                                )
                            }
                        }
                    }

                    // 3. 按讚/反應功能（無論文字或圖片都靠下對齊顯示）
                    if (!message.reactions.isNullOrEmpty()) {
                        ReactionRow(
                            reactions = message.reactions,
                            onReactionClick = { emoji -> onReactionClick(message, emoji) }
                        )
                    }
                }
            }
        }
    }
}

// 💡 提取出的「被回覆訊息預覽」組件，避免重複代碼並維持排版乾淨
@Composable
private fun RepliedMessagePreview(
    replied: Message,
    isMe: Boolean,
    partnerDisplayName: String,
    currentUserId: String
) {
    Row(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x1A000000))
            .padding(start = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(22.dp)
                .background(if (isMe) Color(0xFF4CAF50) else Color(0xFF78909C))
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = if (replied.attachment?.mimeType?.startsWith("image/") == true) "[圖片]" else replied.content ?: "",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}