package com.example.login_v3.home.Message.UI.Detail

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageActionMenuRow
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageEmojiBar
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.MessageInputBar
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.ReactionRow
import com.example.login_v3.home.Message.UI.Detail.Message_Messaging_detail.UserAvatar
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.SendMessageState
import com.example.login_v3.home.Message.ViewModel.UserStatus
import com.example.login_v3.navigation.BottomBarViewModel




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
                            onImageSelected = { uri -> viewModel.uploadAttachment(roomId, uri) }
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
                                viewModel.deleteMessage(roomId, msg.id)
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
                                val targetReaction = message.reactions?.find { it.emoji == emoji }
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
                // 💡 判斷：不論是正在選 Emoji 還是正在開啟動作選單，只要命中就判定為高亮
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



    // 💡 根據是否高亮，動態決定氣泡背景色（或是你想要的邊框效果）
    val bubbleColor = when {
        isMe && isHighlight -> Color(0xFFB4E197)   // 我發的：高亮時變成較深的草綠色
        isMe -> Color(0xFFDCF8C6)                  // 我發的：平常的淺綠色
        !isMe && isHighlight -> Color(0xFFCFD8DC)  // 對方發的：高亮時變成較深的灰色
        else -> Color(0xFFECEFF1)                  // 對方發的：平常的淺灰色
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 💡 讓使用者長按整條訊息就能觸發回覆
            .combinedClickable(
                onLongClick = { onReplyClick(message) }, // 長按 -> 喚起 ActionMenu
                onClick = { onRowClick(message) }       // 單擊 -> 喚起 EmojiMenu
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
                        color = bubbleColor, // 💡 使用動態計算的背景色
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
                                    .heightIn(max = 300.dp),
                                // 🛠️ 移除了 unresolved 的 debugPlaceholder
                                onError = { errorState ->
                                    // 這樣就能在 Logcat 裡過濾 "ChatImageError" 關鍵字，看看到底是哪個網址拼錯了！
                                    android.util.Log.e(
                                        "ChatImageError",
                                        "圖片載入失敗, 網址為: $finalImageModel, 原因: ${errorState.result.throwable}"
                                    )
                                }
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