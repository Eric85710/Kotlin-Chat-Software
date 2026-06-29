package com.example.login_v3.home.Message.ViewModel.Detail


import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.*
import com.example.login_v3.R
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.fullContactAvatarUrl
import com.example.login_v3.data.repository.basic.TokenManager
import com.example.login_v3.home.Message.ViewModel.UserStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        val messages: List<Message>,
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
    private val application: Application
) : ViewModel() {

    // --- 訊息長按動作選單與回覆狀態 (保持原樣) ---
    // 💡 補上這兩行宣告
    private val _deleteMessageState = MutableStateFlow<DeleteMessageState>(DeleteMessageState.Idle)
    val deleteMessageState: StateFlow<DeleteMessageState> = _deleteMessageState.asStateFlow()
    private val _actionMessage = MutableStateFlow<Message?>(null)
    val actionMessage: StateFlow<Message?> = _actionMessage.asStateFlow()

    fun setActionMessage(message: Message?) { _actionMessage.value = message }
    fun clearActionMessage() { _actionMessage.value = null }

    private val _replyingMessage = MutableStateFlow<Message?>(null)
    val replyingMessage: StateFlow<Message?> = _replyingMessage.asStateFlow()
    fun setReplyingMessage(message: Message?) { _replyingMessage.value = message }

    // --- 核心狀態管理 ---
    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    // 💡 提示：在做法 A 下，_sendMessageState 通常只用來控制輸入框的「Loading」轉圈圈或置灰，真正的訊息成功/失敗已經內嵌在 Message 本身的 status 裡了。
    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState

    // 用來管理監聽 Room Flow 的 Job，避免重複綁定
    private var messageListenerJob: Job? = null

    // =====================================================================
    // 💡 核心改造一：持續監聽本地資料庫的 Flow
    // =====================================================================
    fun loadMessages(roomId: String) {
        messageListenerJob?.cancel()

        messageListenerJob = viewModelScope.launch {
            // 🌟 優化防閃爍：只有在原本不是 Success 的情況下，才去 show 轉圈圈
            try {
                repository.startChatSession(roomId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "WebSocket 啟動失敗", e)
            }

            if (_uiState.value !is MessagesUiState.Success) {
                _uiState.value = MessagesUiState.Loading
            }

            val currentLoggedInUserId = tokenManager.currentUserId.first() ?: ""
            val roomResult = repository.getChatRoom(roomId)

            val room = roomResult.getOrNull()
            val (title, status, avatarUrl: Any?) = if (room != null) {
                val name = room.partner?.displayName ?: room.partner?.username ?: "未知用戶"
                val userStatus = UserStatus.fromString(room.partner?.status)
                val avatar = room.partner?.fullContactAvatarUrl
                Triple(name, userStatus, avatar)
            } else {
                Triple("聊天室", UserStatus.UNKNOWN, R.drawable.avatar_v1)
            }

            // 持續監聽本地資料庫
            repository.getChatMessagesFlow(roomId).collect { localMessages ->
                _uiState.value = MessagesUiState.Success(
                    roomTitle = title,
                    partnerStatus = status,
                    partnerAvatarUrl = avatarUrl,
                    messages = localMessages,
                    currentUserId = currentLoggedInUserId
                )
            }
        }

        // 背景默默跟遠端伺服器同步
        viewModelScope.launch {
            repository.refreshChatMessages(roomId).onSuccess {
                repository.markAsRead(roomId).onFailure { error ->
                    Log.e("ChatViewModel", "標記已讀失敗: ${error.message}")
                }
            }
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
    fun downloadFile(message: Message, displayFileName: String) {
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