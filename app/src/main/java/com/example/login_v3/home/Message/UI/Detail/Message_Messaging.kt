package com.example.login_v3.home.Message.UI.Detail

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageActionMenuRow
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageEmojiBar
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageInputBar
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.ReactionRow
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.UserAvatar
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.DeleteMessageState
import com.example.login_v3.home.Message.ViewModel.Detail.MessageStatus
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.SendMessageState
import com.example.login_v3.home.Message.ViewModel.UserStatus
import com.example.login_v3.navigation.BottomBarViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars // 如果要使用 statusBars 也需要這個
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack


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

                    Spacer(modifier = Modifier.height(20.dp))

                    //topbar content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedElement(
                                rememberSharedContentState(key = "container_$roomId"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(0.dp)
                            )
                    ) {
                        // 2. 改用 Column 將「狀態欄空間」與「實際內容」垂直排列
                        Column(modifier = Modifier.fillMaxWidth()) {

                            // 🌟 這一行負責把手機最上方的狀態欄（Status Bar）高度撐開
                            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                            // 3. 真正的頂部導覽列內容
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp) // 👈 固定標準的 TopAppBar 高度，不會再扁扁一條了！
                                    .padding(horizontal = 12.dp) // 側邊留點呼吸空間
                            ) {
                                // 如果你想加返回按鈕，可以加在這裡

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
                                    fontSize = 24.sp, // 👈 直接在這裡設定你想要的 sp 大小
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                if (currentState is MessagesUiState.Success) {
                                    if (currentState.partnerStatus == UserStatus.ONLINE) {
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
                    }
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
                                    viewModel.clearActionMessage()
                                    emojiTargetMessage = message
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
    val isLegacyImage = contentLowerCase.startsWith("http") && imageExtensions.any { contentLowerCase.contains(it) }
    val isImage = isAttachmentImage || isLegacyImage


    val rawContent = message.content ?: ""
    // 💡 核心修正：利用 remember 根據 message 狀態動態計算正確的圖片 URL
    val finalImageModel = remember(message) {
        // 1. 取得原始路徑（優先使用 attachment 的 filename，次之用 content）
        val rawPath = if (isAttachmentImage && !message.attachment?.filename.isNullOrBlank()) {
            message.attachment!!.filename
        } else {
            message.content ?: ""
        }

        when {
            rawPath.isBlank() -> ""
            // 如果後端已經貼心地給了完整網址（包含 http 或 https），就直接使用
            rawPath.startsWith("http") -> rawPath

            // 如果後端給的路徑已經包含 "/uploads/"（例如 "/uploads/attachment/dfec7f...jpg"）
            rawPath.startsWith("/uploads/") -> "https://192.168.0.217$rawPath"
            rawPath.contains("uploads/") -> "https://192.168.0.217/$rawPath"

            // 💡 關鍵處理：如果後端只給了純檔名（例如 "dfec7f...jpg"）或是 "attachment/dfec7f...jpg"
            // 我們要在前方幫它手動補上標準的存放路徑 "/uploads/"
            else -> {
                val cleanPath = rawPath.removePrefix("/") // 移除可能重複的開頭斜線
                if (cleanPath.startsWith("attachment/")) {
                    "https://192.168.0.217/uploads/$cleanPath"
                } else {
                    "https://192.168.0.217/uploads/attachment/$cleanPath"
                }
            }
        }
    }




    val finalBubbleColor = remember(message.status, isHighlight) {
        val baseColor = when {
            isMe && isHighlight -> Color(0xFFB4E197)
            isMe -> Color(0xFFDCF8C6)
            !isMe && isHighlight -> Color(0xFFCFD8DC)
            else -> Color(0xFFECEFF1)
        }

        // ⏳ 如果是發送中，直接讓顏色本身帶 alpha（例如 0.75f，防止被背景吃掉）
        if (message.status == MessageStatus.SENDING) {
            baseColor.copy(alpha = 0.75f)
        } else {
            baseColor // 成功或失敗時維持實色
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isHighlight) Modifier.background(Color(0x33FF9800), RoundedCornerShape(12.dp)) else Modifier)
    ) {
        //message container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
            ,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            if (!isMe) {
                UserAvatar(
                    avatarUrl = partnerAvatarUrl,
                    modifier = Modifier.padding(end = 8.dp).size(32.dp)
                )
            }

            // 💡 核心優化：如果是「我發的」且「發送失敗」，我們要把驚嘆號跟氣泡橫向並排
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                // 🛑 如果是我發的訊息，且發送失敗（FAILED），就把紅色驚嘆號顯示在氣泡「左邊」
                if (isMe && message.status == MessageStatus.FAILED) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning, // 需確保有 import
                        contentDescription = "發送失敗",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                // 💡 加分功能：未來可以在這裡綁定「點擊重發」的 viewModel.sendMessage(...) 邏輯
                            }
                    )
                }

                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
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

                            // 🌟 核心修復：如果存在被回覆的訊息，在這裡將它渲染出來！
                            message.repliedMessage?.let { replied ->
                                Row(
                                    modifier = Modifier
                                        .padding(bottom = 6.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x1A000000)) // 給予一層淡淡的黑色半透明背景做為區隔
                                        .padding(start = 6.dp) // 留空間給左側邊條
                                ) {
                                    // 左側的垂直裝飾邊條
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(32.dp)
                                            .background(if (isMe) Color(0xFF4CAF50) else Color(0xFF78909C)) // 根據是誰發的決定邊條顏色
                                    )

                                    // 被回覆訊息的內容預覽
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (replied.senderId == currentUserId) "你" else partnerDisplayName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (replied.attachment?.mimeType?.startsWith("image/") == true) "[圖片]" else replied.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1, // 限制一行，避免回覆訊息太長塞爆畫面
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // 訊息主體內容（文字或圖片）
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
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                                        onError = { errorState ->
                                            android.util.Log.e("ChatImageError", "原因: ${errorState.result.throwable}")
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    text = rawContent,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }
                    if (!message.reactions.isNullOrEmpty()) {
                        ReactionRow(
                            reactions = message.reactions,
                            onReactionClick = { emoji -> onReactionClick(message, emoji) }
                        )
                    }
                }

                // 🛑 如果是對方發的訊息（雖然對方理論上不會有 FAILED 狀態，安全起見做對稱），或是想放右邊的提示
                // 可以依此類推。通常聊天軟體只會處理自己發送失敗的提示。
            }
        }
    }
}