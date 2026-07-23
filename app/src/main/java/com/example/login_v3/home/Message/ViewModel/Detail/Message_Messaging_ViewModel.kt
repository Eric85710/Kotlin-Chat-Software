package com.example.login_v3.home.Message.ViewModel.Detail


import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.*
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.login_v3.R
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.login_v3.home.Message.UI.Detail.MessageUiModel
import com.example.login_v3.home.Message.UI.Detail.toUiModel
import com.example.login_v3.data.api.api_class.fullContactAvatarUrl
import com.example.login_v3.data.repository.basic.TokenManager
import com.example.login_v3.home.Message.ViewModel.UserStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

//emoji reaction state
sealed class ReactionUsersState {
    object Idle : ReactionUsersState()                                          // 沒點擊時的閒置狀態
    object Loading : ReactionUsersState()                                       // 載入中
    data class Success(val users: List<String>) : ReactionUsersState()          // 成功取得名單
    data class Error(val message: String) : ReactionUsersState()                // 取得失敗
}

// 🎯 修正後的發送狀態管理
sealed interface SendMessageState {
    object Idle : SendMessageState
    object Loading : SendMessageState
    object Success : SendMessageState // 👈 🌟 這裡改成 object
    data class Error(val message: String) : SendMessageState
}


// 刪除訊息狀態
sealed class DeleteMessageState {
    object Idle : DeleteMessageState()                                     // 閒置
    object Loading : DeleteMessageState()                                  // 刪除中
    object Success : DeleteMessageState()                                  // 刪除成功
    data class Error(val message: String) : DeleteMessageState()           // 刪除失敗
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(
        val roomTitle: String,
        val partnerStatus: UserStatus,
        val partnerAvatarUrl: Any?,
        val messages: List<MessageUiModel>,
        val currentUserId: String
    ) : MessagesUiState

    data class Error(val message: String) : MessagesUiState
}

//audio status
data class AudioPlaybackState(
    val currentPlayingMessageId: String? = null, // 當前播放的 Message ID
    val isPlaying: Boolean = false,              // 是否正在播放
    val currentPosition: Long = 0L,              // 當前播放位置 (毫秒)
    val duration: Long = 0L                      // 總時長 (毫秒)
)

// 🎯 檔案下載狀態管理
sealed interface DownloadStatus {
    object NotStarted : DownloadStatus
    data class Downloading(val progress: Float) : DownloadStatus // 儲存 0.0 ~ 1.0f 的進度
    object Completed : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRoomsRepository,
    private val tokenManager: TokenManager,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 🚀 核心優化：避免重複預載相同的 URL
    private val prefetchedUrls = mutableSetOf<String>()

    // 從導航參數自動獲取 roomId
    val roomId: String = savedStateHandle.get<String>("roomId").orEmpty()

    // --- 核心狀態管理 (改為完全響應式) ---
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MessagesUiState> = combine(
        repository.getChatMessagesFlow(roomId),
        tokenManager.currentUserId,
        repository.getChatRoomFlow(roomId)
    ) { messages, userId, room ->
        if (userId == null) {
            MessagesUiState.Loading
        } else {
            val title = room?.partner?.displayName ?: room?.partner?.username ?: "聊天室"
            val userStatus = UserStatus.fromString(room?.partner?.status)
            val avatar = room?.partner?.fullContactAvatarUrl ?: R.drawable.avatar_v1
            
            MessagesUiState.Success(
                roomTitle = title,
                partnerStatus = userStatus,
                partnerAvatarUrl = avatar,
                messages = messages.map { it.toUiModel() },
                currentUserId = userId
            )
        }
    }.onEach { state ->
        // 🚀 核心優化：智能預加載媒體內容
        if (state is MessagesUiState.Success) {
            prefetchMedia(state.messages)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MessagesUiState.Loading)

    private fun prefetchMedia(messages: List<MessageUiModel>) {
        messages.forEach { msg ->
            if ((msg.isImage || msg.isVideo || msg.isGif) && !prefetchedUrls.contains(msg.mediaUrl)) {
                prefetchedUrls.add(msg.mediaUrl)
                val request = ImageRequest.Builder(application)
                    .data(msg.mediaUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    // 如果是影片，預載入時只抓取第一幀
                    .build()
                application.imageLoader.enqueue(request)
            }
        }
    }


    // --- 其餘狀態 (保持原樣) ---
    private val _deleteMessageState = MutableStateFlow<DeleteMessageState>(DeleteMessageState.Idle)
    val deleteMessageState: StateFlow<DeleteMessageState> = _deleteMessageState.asStateFlow()
    private val _actionMessage = MutableStateFlow<MessageUiModel?>(null)
    val actionMessage: StateFlow<MessageUiModel?> = _actionMessage.asStateFlow()

    fun setActionMessage(message: MessageUiModel?) { _actionMessage.value = message }
    fun clearActionMessage() { _actionMessage.value = null }

    private val _replyingMessage = MutableStateFlow<MessageUiModel?>(null)
    val replyingMessage: StateFlow<MessageUiModel?> = _replyingMessage.asStateFlow()
    fun setReplyingMessage(message: MessageUiModel?) { _replyingMessage.value = message }

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState

    // --- 分頁加載狀態控制 ---
    private var nextCursor: String? = null
    private var hasMore: Boolean = true
    private var isLoadingMore = false // 防止重複戳 API

    init {
        if (roomId.isNotEmpty()) {
            initChat(roomId)
        }
    }

    private fun initChat(roomId: String) {
        viewModelScope.launch {
            // 啟動 Session
            try { repository.startChatSession(roomId) } catch (e: Exception) { Log.e("ChatViewModel", "WS Error", e) }

            // 背景同步
            repository.refreshChatMessages(roomId, cursor = null, limit = 20)
                .onSuccess { response ->
                    nextCursor = response.nextCursor
                    hasMore = response.hasMore
                    repository.markAsRead(roomId)
                }
        }
    }

    // 相容性方法
    fun loadMessages(roomId: String) { }

    // =====================================================================
    // 💡 核心新增：往上滑載入更多老訊息 (補上 limit)
    // =====================================================================
    fun loadMoreMessages(roomId: String) {
        if (isLoadingMore) {
            Log.d("ChatDebug", "⚠️ [ViewModel] loadMoreMessages 略過: 正在加載中...")
            return
        }
        if (!hasMore) {
            Log.d("ChatDebug", "⚠️ [ViewModel] loadMoreMessages 略過: 沒有更多訊息了 (hasMore=false)")
            return
        }
        if (nextCursor.isNullOrBlank()) {
            Log.d("ChatDebug", "⚠️ [ViewModel] loadMoreMessages 略過: nextCursor 為空")
            return
        }

        isLoadingMore = true
        Log.d("ChatDebug", "🚀 [ViewModel] 開始載入更多 | 當前 cursor: $nextCursor | roomId: $roomId")

        viewModelScope.launch {
            // 🎯 補上 limit = 20 傳遞給 Repository 與 API
            repository.refreshChatMessages(roomId, cursor = nextCursor, limit = 20)
                .onSuccess { response ->
                    this@ChatViewModel.nextCursor = response.nextCursor
                    this@ChatViewModel.hasMore = response.hasMore
                    Log.d("ChatDebug", "✅ [ViewModel] 載入更多成功 | 新 cursor: $nextCursor | hasMore: $hasMore")
                }
                .onFailure { error ->
                    Log.e("ChatDebug", "❌ [ViewModel] 載入更多失敗", error)
                }

            isLoadingMore = false
        }
    }

    // =====================================================================
    // 💡 核心改造二：發送文字訊息（樂觀更新）
    // =====================================================================
    fun sendMessage(roomId: String, content: String) {
        // 1. 安全防護：空白直接攔截
        if (content.trim().isBlank()) return

        viewModelScope.launch {
            // 2. 進入 Loading，讓輸入框知道目前在打 API
            _sendMessageState.value = SendMessageState.Loading

            // 3. 保持你原本非常棒的 token 讀取機制
            val replyToId = _replyingMessage.value?.id
            val currentLoggedInUserId = tokenManager.currentUserId.first() ?: ""

            if (currentLoggedInUserId.isBlank()) {
                _sendMessageState.value = SendMessageState.Error("找不到使用者資訊，請重新登入")
                return@launch
            }

            // 4. 呼叫樂觀更新方法（此時 SENDING 狀態訊息已秒入 Room，畫面同步劃出半透明氣泡）
            val result = repository.sendMessageOptimistically(
                roomId = roomId,
                currentUserId = currentLoggedInUserId,
                content = content,
                replyToId = replyToId
            )

            // 5. 順手清空回覆狀態
            _replyingMessage.value = null

            result.onSuccess {
                // 🌟 完美：因為變成了 object，直接指派名字即可，絕不會再噴 Too many arguments！
                _sendMessageState.value = SendMessageState.Success

            }.onFailure { error ->
                Log.e("ChatViewModel", "Send message failed", error)
                _sendMessageState.value = SendMessageState.Error(error.message ?: "未知錯誤")
            }
        }
    }

    // =====================================================================
    // 💡 核心改造三：刪除訊息
    // =====================================================================
    // =====================================================================
    // 💡 核心改造三：刪除訊息（配合樂觀更新優化）
    // =====================================================================
    fun deleteMessage(roomId: String, messageId: String) {
        // 1. 🌟 樂觀更新的核心精神：立刻關閉長按選單（Action Menu）
        // 因為訊息在下一行就會從資料庫消失，選單如果還開著會點不到東西或出錯
        clearActionMessage()

        viewModelScope.launch {
            // 2. 💡 注意：這裡「不要」在 UI 顯示阻擋式的 Loading 轉圈圈。
            // 這裡的 Loading 狀態只留在背景，或者用於特定的微小 UI 提示（例如標題列小藍點）。
            _deleteMessageState.value = DeleteMessageState.Loading

            val result = repository.deleteMessage(roomId, messageId)

            result.onSuccess {
                _deleteMessageState.value = DeleteMessageState.Success
                // 🌟 刪除成功！本地早就不見了，Flow 也早已更新，這裡乾淨俐落。
            }.onFailure { error ->
                Log.e("ChatViewModel", "Delete message failed", error)
                _deleteMessageState.value = DeleteMessageState.Error(error.message ?: "刪除訊息失敗")

                // 3. 🌟 失敗處理：因為 Repository 把訊息彈回來了，我們可以讓 UI 知道
                // 這裡可以透過監聽 deleteMessageState 的 Error，在 Activity/Fragment/Compose 彈出 Toast 提示：「刪除失敗，請檢查網路」
            }
        }
    }

    // --- 其餘 uploadAttachment、Emoji 功能保持原樣 ---
    fun uploadAttachment(roomId: String, fileUri: Uri) {
        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            // 🎯 【偵錯步驟 1】檢查這個音訊 Uri 到底長怎樣、MimeType 是什麼
            try {
                val contentResolver = application.contentResolver
                val mimeType = contentResolver.getType(fileUri)

                // 🎯 【偵錯步驟 2】呼叫 Repository
                val result = repository.uploadAttachment(roomId, fileUri)

                result.onSuccess {
                    _sendMessageState.value = SendMessageState.Success
                    repository.refreshChatMessages(roomId)
                }.onFailure { error ->
                    // 🎯 【偵錯步驟 3】這裡能抓到 Repository 傳回來的 API 錯誤
                    Log.e("ChatDebug", "Repository 回報上傳失敗: ${error.message}", error)
                    _sendMessageState.value = SendMessageState.Error(error.message ?: "上傳失敗")
                }

            } catch (e: Exception) {
                // 🎯 【偵錯步驟 4】這裡能抓到本機讀取檔案就崩潰的錯誤（例如權限或 Stream 問題）
                Log.e("ChatDebug", "ViewModel 處理檔案時發生異常: ${e.message}", e)
                _sendMessageState.value = SendMessageState.Error(e.message ?: "讀取檔案失敗")
            }
        }
    }


    fun addMessageReaction(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.addMessageReaction(roomId, messageId, emoji)
                .onSuccess {
                    // 🌟 這裡也不用 loadMessages(roomId) 了，直接背景 refresh 即可
                    repository.refreshChatMessages(roomId)
                }
        }
    }

    fun removeMessageReaction(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.deleteMessageReaction(roomId, messageId, emoji)
                .onSuccess {
                    repository.refreshChatMessages(roomId)
                }
        }
    }



    //audio status
    private val _audioState = MutableStateFlow(AudioPlaybackState())
    val audioState: StateFlow<AudioPlaybackState> = _audioState.asStateFlow()

    // 唯一播放器實例
    private var exoPlayer: ExoPlayer? = null
    // 用來定時輪詢進度條的 Job
    private var progressLogJob: Job? = null

    init {
        // 初始化 ExoPlayer
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = Builder(application).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _audioState.value = _audioState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _audioState.value = _audioState.value.copy(
                                duration = duration.coerceAtLeast(0L)
                            )
                        }
                        Player.STATE_ENDED -> {
                            // 播放完畢，重置進度與播放狀態
                            stopProgressTracker()
                            seekTo(0)
                            _audioState.value = _audioState.value.copy(
                                isPlaying = false,
                                currentPosition = 0L
                            )
                        }
                    }
                }
            })
        }
    }

    // 點擊音訊氣泡的外部接口
    fun toggleAudioPlayback(messageId: String, audioUrl: String) {
        val player = exoPlayer ?: return
        val currentState = _audioState.value

        if (currentState.currentPlayingMessageId == messageId) {
            // 點擊的是同一個音訊 -> 切換 播放/暫停
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        } else {
            // 點擊的是新音訊 -> 停止上一個，載入新音訊播放
            player.stop()
            _audioState.value = AudioPlaybackState(
                currentPlayingMessageId = messageId,
                isPlaying = false
            )

            val mediaItem = MediaItem.fromUri(audioUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    // 拖動進度條時調整播放位置
    fun seekAudioTo(progressRatio: Float) {
        val player = exoPlayer ?: return
        val targetPosition = (progressRatio * _audioState.value.duration).toLong()
        player.seekTo(targetPosition)
        _audioState.value = _audioState.value.copy(currentPosition = targetPosition)
    }

    // 啟動協程計時器，即時刷新播放進度
    private fun startProgressTracker() {
        progressLogJob?.cancel()
        progressLogJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _audioState.value = _audioState.value.copy(
                        currentPosition = player.currentPosition
                    )
                }
                delay(200) // 每 200 毫秒刷新一次 UI
            }
        }
    }

    private fun stopProgressTracker() {
        progressLogJob?.cancel()
        progressLogJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracker()
        exoPlayer?.release()
        exoPlayer = null
    }
    // =====================================================================


    //file download
    val fileDownloadStates = mutableStateMapOf<String, DownloadStatus>()

    // 取得特定訊息的下載狀態，若無則預設為 NotStarted
    fun getDownloadStatus(messageId: String): DownloadStatus {
        return fileDownloadStates[messageId] ?: DownloadStatus.NotStarted
    }
    fun downloadFile(message: MessageUiModel, displayFileName: String) {
        val messageId = message.id

        // 🎯 取得後端給的直接 URL。請根據你實際的 Message 資料結構調整欄位名稱
        val fileUrl = message.content // 假設你把 URL 存放在 content 裡，或是 message.fileUrl

        if (fileUrl.isNullOrBlank()) {
            fileDownloadStates[messageId] = DownloadStatus.Error("檔案下載連結無效")
            return
        }

        if (fileDownloadStates[messageId] is DownloadStatus.Downloading) return

        viewModelScope.launch {
            fileDownloadStates[messageId] = DownloadStatus.Downloading(0f)

            // 呼叫剛剛改好的 URL 版 Flow
            repository.downloadFileFromUrlFlow(fileUrl, displayFileName)
                .collect { progressResult ->
                    progressResult.onSuccess { progress ->
                        if (progress >= 1.0f) {
                            fileDownloadStates[messageId] = DownloadStatus.Completed
                        } else {
                            fileDownloadStates[messageId] = DownloadStatus.Downloading(progress)
                        }
                    }.onFailure { error ->
                        Log.e("ChatViewModel", "下載失敗: ${error.message}")
                        fileDownloadStates[messageId] = DownloadStatus.Error(error.message ?: "下載失敗")
                    }
                }
        }
    }

    // （選填）如果想要清除特定訊息的狀態（例如錯誤後想重試）
    fun resetDownloadStatus(messageId: String) {
        fileDownloadStates.remove(messageId)
    }

    fun resetDeleteMessageState() { _deleteMessageState.value = DeleteMessageState.Idle }
    fun resetSendMessageState() { _sendMessageState.value = SendMessageState.Idle }
}