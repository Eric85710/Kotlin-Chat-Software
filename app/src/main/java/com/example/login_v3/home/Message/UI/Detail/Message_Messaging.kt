package com.example.login_v3.home.Message.UI.Detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.login_v3.data.local.entities.MessageStatus
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.Detail.SendMessageState
import com.example.login_v3.navigation.BottomBarViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars // 如果要使用 statusBars 也需要這個
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import com.example.login_v3.data.api.api_class.CallLogInfo
import com.example.login_v3.home.Message.UI.Detail.Message_Component.AudioMessageBubble
import com.example.login_v3.home.Message.UI.Detail.Message_Component.CallLogBubble
import com.example.login_v3.home.Message.UI.Detail.Message_Component.ImageLightbox
import com.example.login_v3.home.Message.UI.Detail.Message_Component.MessageAttachmentBar
import com.example.login_v3.home.Message.UI.Detail.Message_Component.UserStatusDot
import com.example.login_v3.home.Message.UI.Detail.Message_Component.VideoLightbox
import com.example.login_v3.home.Message.ViewModel.Detail.DownloadStatus


//bottom bar state
sealed interface BottomBarState {
    object Input : BottomBarState                                // 預設：輸入框
    data class ActionMenu(val message: Message) : BottomBarState // 長按：動作選單
    data class EmojiMenu(val message: Message) : BottomBarState  // 單擊：Emoji 工具列
    object AttachmentMenu : BottomBarState                       // 🎯 新增：附件選單
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

    //inspect video
    var lightboxVideoUrl by remember { mutableStateOf<String?>(null) }

    // 🌟 核心：使用 derivedStateOf 統一控管狀態權限（長按優先權通常大於單擊）
    // 🎯 新增：用來控管非訊息觸發的底部狀態（例如：AttachmentMenu）
    var localBottomBarState by remember { mutableStateOf<BottomBarState?>(null) }
    val bottomBarState by remember {
        derivedStateOf {
            when {
                actionMessage != null -> BottomBarState.ActionMenu(actionMessage!!)
                emojiTargetMessage != null -> BottomBarState.EmojiMenu(emojiTargetMessage!!)
                localBottomBarState != null -> localBottomBarState!! // 🎯 如果有本地狀態，則採用它
                else -> BottomBarState.Input
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAttachment(roomId, it) }
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

    // 1. 定義需要向系統「申請」的權限陣列（維持不變）
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // 2. 🎯 核心修正：判斷「目前是否能夠進入選單」的邏輯
    fun checkHasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // 【Android 14+】
            // 音訊必須要過
            val audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            // 圖片不管是「全過(READ_MEDIA_IMAGES)」還是「部分過(READ_MEDIA_VISUAL_USER_SELECTED)」都可以算過！
            val imageGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val partialImageGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED

            audioGranted && (imageGranted || partialImageGranted)
        } else {
            // 【Android 13 及以下】
            requiredPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        }
    }

// 3. Launcher 接收結果時的判斷
    val mainPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // 🎯 不要直接用傳回來的 Map，而是用我們上面寫好的自訂檢查
        if (checkHasRequiredPermissions()) {
            localBottomBarState = BottomBarState.AttachmentMenu
        } else {
            Toast.makeText(context, "Need Access Permission", Toast.LENGTH_SHORT).show()
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
                                // 🎯 修正這部分的權限檢查邏輯
                                onAttachmentClick = {
                                    // 1. 使用我們剛剛封裝好的多重權限檢查函式
                                    if (checkHasRequiredPermissions()) {
                                        // 已經有權限了，直接開啟附件選單
                                        localBottomBarState = BottomBarState.AttachmentMenu
                                    } else {
                                        // 2. 沒權限，叫起多重權限請求視窗（傳入 requiredPermissions 陣列）
                                        mainPermissionLauncher.launch(requiredPermissions)
                                    }
                                }
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

                        is BottomBarState.AttachmentMenu -> {
                            MessageAttachmentBar(
                                onImageSelected = { uri ->
                                    // 🎯 使用者在預覽列中點選了某張圖片，直接觸發上傳
                                    viewModel.uploadAttachment(roomId, uri)
                                    // 上傳後自動退回輸入框
                                    localBottomBarState = null
                                },
                                onAudioSelected = { uri ->
                                    // 處理音訊上傳的邏輯（通常跟圖片上傳類似，或是呼叫你專門處理音訊的函式）
                                    viewModel.uploadAttachment(roomId, uri) // 假設你的 ViewModel 上傳函式是通用的
                                    // 上傳後自動退回輸入框
                                    localBottomBarState = null
                                },
                                onCancel = {
                                    // 點擊關閉退回輸入框
                                    localBottomBarState = null
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
                                roomId = roomId,
                                currentUserId = state.currentUserId,
                                messages = state.messages,
                                partnerAvatarUrl = state.partnerAvatarUrl,
                                partnerDisplayName = state.roomTitle,
                                activeEmojiMessageId = emojiTargetMessage?.id,   // ✨ 新增短按目標 ID
                                activeActionMessageId = actionMessage?.id,
                                viewModel = viewModel,
                                onReplyClick = { message ->
                                    emojiTargetMessage = null
                                    viewModel.setActionMessage(message)
                                },
                                // 💡 單擊事件：開啟 Emoji 選單，同時清空長按選單
                                onRowClick = { message ->
                                    val contentLowerCase = (message.content ?: "").lowercase()

                                    //is image
                                    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
                                    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
                                    val isImage = isAttachmentImage || imageExtensions.any { contentLowerCase.contains(it) }

                                    //is video
                                    val isAttachmentVideo = message.attachment?.mimeType?.startsWith("video/") == true
                                    val videoExtensions = listOf(".mp4", ".mov", ".mkv", ".avi", ".3gp", ".webm")
                                    val isVideo = isAttachmentVideo || videoExtensions.any { contentLowerCase.contains(it) }

                                    if (isImage || isVideo) {
                                        // 計算乾淨的 URL 網址
                                        val rawPath = when {
                                            isAttachmentImage && !message.attachment?.filename.isNullOrBlank() -> message.attachment!!.filename
                                            isAttachmentVideo && !message.attachment?.filename.isNullOrBlank() -> message.attachment!!.filename
                                            else -> message.content ?: ""
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

                                        // 關閉所有底部的 Emoji 或動作選單
                                        viewModel.clearActionMessage()
                                        emojiTargetMessage = null

                                        // 依據類型，分流塞入對應的狀態
                                        if (isImage) {
                                            lightboxImageUrl = finalUrl
                                        } else {
                                            lightboxVideoUrl = finalUrl // 🎯 觸發影片全螢幕
                                        }
                                    } else {
                                        // 🎯 一般文字或通話，叫出 Emoji Bar
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

    //image inspect
    lightboxImageUrl?.let { url ->
        ImageLightbox(
            imageUrl = url,
            onDismiss = { lightboxImageUrl = null }
        )
    }

    //video inspect
    lightboxVideoUrl?.let { url ->
        VideoLightbox(
            videoUrl = url,
            onDismiss = { lightboxVideoUrl = null }
        )
    }
}

@Composable
fun MessageList(
    roomId: String,
    currentUserId: String,
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    messages: List<Message>,
    activeEmojiMessageId: String?,
    activeActionMessageId: String?,
    viewModel: ChatViewModel,
    onReplyClick: (Message) -> Unit,
    onReactionClick: (Message, String) -> Unit,
    onRowClick: (Message) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. 建立 LazyColumn 的滾動狀態監聽
    val listState = rememberLazyListState()

    val visibleMessages = remember(messages) {
        messages.filter { !it.isDeleted }
    }

    // 🎯 2. 核心分頁監聽：監聽滾動，快滑到最老訊息時默默去遠端載入更多
    LaunchedEffect(listState, visibleMessages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    // 因為設定了 reverseLayout = true，最新訊息在 index = 0 (最底部)
                    // 最老的訊息在列表的最後一項（最頂部）。
                    val lastVisibleItem = visibleItems.last()

                    // 當畫面上顯示的老訊息距離總資料量不到 5 條時，提前預加載歷史紀錄
                    val threshold = 5
                    if (lastVisibleItem.index >= visibleMessages.size - threshold) {
                        Log.d("MessageList", "接近頂端老訊息，自動觸發載入更多")
                        viewModel.loadMoreMessages(roomId)
                    }
                }
            }
    }

    LazyColumn(
        state = listState, // 🎯 記得綁定 state
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        reverseLayout = true
    ) {
        items(
            items = visibleMessages,
            key = { it.id } // 💡 加上 key 可以大幅提升 LazyColumn 刷新時的效能與動畫平滑度
        ) { message ->
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
                onRowClick = onRowClick,
                viewModel = viewModel,
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
    viewModel: ChatViewModel,
    onReplyClick: (Message) -> Unit,
    onReactionClick: (Message, String) -> Unit,
    onRowClick: (Message) -> Unit,
) {
    //圖片判斷
    val isAttachmentImage = message.attachment?.mimeType?.startsWith("image/") == true
    val contentLowerCase = (message.content ?: "").lowercase()
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif", ".gif")

    val isLegacyImage = imageExtensions.any { contentLowerCase.contains(it) }
    val isImage = isAttachmentImage || isLegacyImage
    val rawContent = message.content ?: ""

// 🌟 新增一個專門用來判斷是否為 GIF 的布林值，UI 載入時會用到
    val isGif = message.attachment?.mimeType == "image/gif" || contentLowerCase.endsWith(".gif")

    // 🌟 2. 影片判斷
    val isAttachmentVideo = message.attachment?.mimeType?.startsWith("video/") == true
    val videoExtensions = listOf(".mp4", ".mov", ".mkv", ".avi", ".3gp", ".webm")
    val isLegacyVideo = videoExtensions.any { contentLowerCase.contains(it) }
    val isVideo = isAttachmentVideo || isLegacyVideo

    //音訊判斷
    val isTypeAudio = message.type == "audio"
    val isAttachmentAudio = message.attachment?.mimeType?.startsWith("audio/") == true
    val audioExtensions = listOf(".mp3", ".wav", ".m4a", ".aac", ".ogg", ".opus", ".amr")
    val isLegacyAudio = audioExtensions.any { contentLowerCase.contains(it) }
    val isAudio = isTypeAudio || isAttachmentAudio || isLegacyAudio

    // 🌟 新增：檔案判斷 (包含 zip、rar、pdf、doc 等常規檔案)
    val isAttachmentFile = message.attachment?.mimeType?.startsWith("application/") == true ||
            message.attachment?.mimeType == "text/plain"
    val fileExtensions = listOf(".zip", ".rar", ".7z", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt")
    val isLegacyFile = fileExtensions.any { contentLowerCase.contains(it) }
    val isFile = isAttachmentFile || isLegacyFile

    val callLog = message.callLogInfo

    // 計算媒體 URL
    val finalMediaUrl = remember(message, isImage, isVideo, isAudio, isFile) {
        val rawPath = when {
            isAttachmentImage && !message.attachment?.filename.isNullOrBlank() -> message.attachment!!.filename
            isAttachmentVideo && !message.attachment?.filename.isNullOrBlank() -> message.attachment!!.filename
            isAttachmentAudio && !message.attachment?.filename.isNullOrBlank() -> message.attachment!!.filename
            // 🌟 新增：檔案附件路徑抓取
            isFile && message.attachment?.filename?.isNotBlank() == true -> message.attachment!!.filename
            else -> message.content ?: ""
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

    // 取得檔案的顯示名稱（如果是 zip 這種長檔名，可以從 URL 或 attachment 抓取）
    val displayFileName = remember(message, finalMediaUrl) {
        if (!message.attachment?.filename.isNullOrBlank()) {
            // 如果後端有給原始檔名（例如：資料.zip），優先顯示
            message.attachment.filename.substringAfterLast("/")
        } else {
            // 如果只有 URL，抓出最後一段當作檔名
            finalMediaUrl.substringAfterLast("/")
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

                //All type of message
                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    when {
                        // 核心分流 1：圖片處理
                        isImage -> {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .combinedClickable(
                                        onLongClick = { onReplyClick(message) },
                                        onClick = { onRowClick(message) }
                                    ),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                message.repliedMessage?.let { replied ->
                                    RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                                }

                                // 🌟 根據是否為 GIF，動態配置 ImageLoader
                                val context = LocalContext.current
                                val gifImageLoader = remember(isGif) {
                                    if (isGif) {
                                        ImageLoader.Builder(context)
                                            .components {
                                                // 依據 Android 版本自動選擇最佳的 GIF 解碼器
                                                if (android.os.Build.VERSION.SDK_INT >= 28) {
                                                    add(ImageDecoderDecoder.Factory())
                                                } else {
                                                    add(GifDecoder.Factory())
                                                }
                                            }
                                            .build()
                                    } else {
                                        null // 普通圖片用系統預設的即可，省記憶體
                                    }
                                }

                                AsyncImage(
                                    model = finalMediaUrl,
                                    contentDescription = if (isGif) "動態 GIF" else "聊天圖片",
                                    imageLoader = gifImageLoader ?: LocalImageLoader.current, // 🎯 如果是 GIF 就用專屬解碼器
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .sizeIn(maxWidth = 240.dp, maxHeight = 300.dp)
                                        .clip(RoundedCornerShape(12.dp))
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
                        }

                        // 🌟 核心分流 2：影片處理 (新增)
                        isVideo -> {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .combinedClickable(
                                        onLongClick = { onReplyClick(message) },
                                        onClick = { onRowClick(message) } // 點擊時可以在此觸發開啟影片播放器
                                    ),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                message.repliedMessage?.let { replied ->
                                    RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                                }

                                // 影片佈局：使用 Box 將「播放圖示」疊加在預覽圖（或黑色背景）上方
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(width = 240.dp, height = 160.dp) // 給予影片固定的預覽比例
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black) // 預防沒載入圖時顯示黑色底
                                        .border(
                                            width = 0.5.dp,
                                            color = Color.LightGray.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 💡 注意：這裡 model 給 finalMediaUrl。
                                    // 如果後端支援，通常會另外傳一個 video_thumbnail_url；
                                    // 如果直接給影片 URL，部分圖片載入庫（如 Coil 搭配 VideoFrameDecoder）可以直接擷取第一幀。
                                    AsyncImage(
                                        model = finalMediaUrl,
                                        contentDescription = "影片預覽",
                                        // 🌟 注入一個支援影片解碼的 ImageLoader
                                        imageLoader = ImageLoader.Builder(LocalContext.current)
                                            .components {
                                                add(VideoFrameDecoder.Factory())
                                            }
                                            .build(),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        alpha = 0.8f,
                                        onError = { errorState ->
                                            android.util.Log.e("ChatVideoThumbError", "原因: ${errorState.result.throwable}")
                                        }
                                    )

                                    // 播放圖示容器
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                                            contentDescription = "播放影片",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        //isAudio
                        isAudio -> {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 250.dp) // 音訊氣泡設定稍窄，更像一般通訊軟體
                                    .combinedClickable(
                                        onLongClick = { onReplyClick(message) },
                                        onClick = { /* 整塊氣泡點擊邏輯，可留空 */ }
                                    ),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                // 如果有回覆別人的訊息，一樣顯示回覆預覽
                                message.repliedMessage?.let { replied ->
                                    RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                                }

                                // 呼叫你的音訊播放器元件
                                AudioMessageBubble(
                                    message = message,
                                    audioUrl = finalMediaUrl,
                                    isMe = isMe,
                                    bubbleColor = finalBubbleColor,
                                    viewModel = viewModel
                                )
                            }
                        }

                        //file message
                        isFile -> {
                            // 1. 🎯 從 ViewModel 取得當前訊息的下載狀態
                            val downloadStatus = viewModel.getDownloadStatus(message.id)

                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp, topEnd = 12.dp,
                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                        )
                                    )
                                    .combinedClickable(
                                        onLongClick = { onReplyClick(message) },
                                        onClick = {
                                            // 點擊整個氣泡：如果是已完成就直接開啟，否則可維持你原本的行為
                                            if (downloadStatus is DownloadStatus.Completed) {
                                                onRowClick(message)
                                            } else {
                                                // 也可以在這裡同樣觸發下載
                                                viewModel.downloadFile(message, displayFileName)
                                            }
                                        }
                                    )
                                    .background(color = finalBubbleColor)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    message.repliedMessage?.let { replied ->
                                        RepliedMessagePreview(replied, isMe, partnerDisplayName, currentUserId)
                                    }

                                    // 檔案的 UI 佈局 (左邊按鈕，右邊文字)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // 檔案下載按鈕 / 進度顯示
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                                                .clip(CircleShape)
                                                .clickable {
                                                    // 2. 🎯 點擊按鈕觸發下載或開啟邏輯
                                                    when (downloadStatus) {
                                                        is DownloadStatus.NotStarted, is DownloadStatus.Error -> {
                                                            viewModel.downloadFile(message, displayFileName)
                                                        }
                                                        is DownloadStatus.Completed -> {
                                                            onRowClick(message) // 觸發下載完成後的開啟檔案
                                                        }
                                                        is DownloadStatus.Downloading -> {
                                                            // 下載中可以留空，或者未來實作取消下載
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // 3. 🎯 根據密封介面 (Sealed Interface) 狀態切換 Icon 與進度條
                                            when (downloadStatus) {
                                                is DownloadStatus.NotStarted -> {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowDownward,
                                                        contentDescription = "下載檔案",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                is DownloadStatus.Downloading -> {
                                                    // 💡 這裡的 progress 是從 downloadStatus.progress 拿出來的
                                                    val progress = downloadStatus.progress

                                                    if (progress >= 0f) {
                                                        CircularProgressIndicator(
                                                            progress = progress,
                                                            modifier = Modifier.fillMaxSize().padding(2.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            strokeWidth = 3.dp
                                                        )
                                                    } else {
                                                        // 如果後端沒給 Content-Length (progress = -1f)，改跑無限轉圈
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.fillMaxSize().padding(2.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            strokeWidth = 3.dp
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                                        contentDescription = "下載中",
                                                        tint = Color.Gray.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                is DownloadStatus.Completed -> {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                                        contentDescription = "下載完成",
                                                        tint = Color.Green,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                is DownloadStatus.Error -> {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.Warning, // 或者是 Refresh 圖示
                                                        contentDescription = "下載失敗，點擊重試",
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // 右側檔名與資訊
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayFileName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = when (downloadStatus) {
                                                    is DownloadStatus.Downloading -> {
                                                        val progress = downloadStatus.progress
                                                        if (progress >= 0f) "下載中... ${(progress * 100).toInt()}%" else "下載中..."
                                                    }
                                                    is DownloadStatus.Completed -> "已下載"
                                                    is DownloadStatus.Error -> "下載失敗，點擊重試"
                                                    else -> "檔案"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 核心分流 3：通話紀錄
                        callLog != null -> {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
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
                        }

                        // 核心分流 4：純文字訊息
                        else -> {
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
                    }

                    // 3. 按讚/反應功能
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