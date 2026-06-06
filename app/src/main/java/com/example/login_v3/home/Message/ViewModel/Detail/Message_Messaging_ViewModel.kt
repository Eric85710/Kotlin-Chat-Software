package com.example.login_v3.home.Message.ViewModel.Detail


import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

//emoji reaction state
sealed class ReactionUsersState {
    object Idle : ReactionUsersState()                                          // 沒點擊時的閒置狀態
    object Loading : ReactionUsersState()                                       // 載入中
    data class Success(val users: List<String>) : ReactionUsersState()          // 成功取得名單
    data class Error(val message: String) : ReactionUsersState()                // 取得失敗
}

//message sending state
sealed class SendMessageState {
    object Idle : SendMessageState()      // 閒置狀態（預設）
    object Loading : SendMessageState()   // 發送中
    data class Success(val message: Message) : SendMessageState() // 發送成功
    data class Error(val message: String) : SendMessageState()    // 發送失敗
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

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRoomsRepository,
    private val tokenManager: TokenManager
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
        // 先取消上一次的監聽（如果有切換房間的話）
        messageListenerJob?.cancel()

        messageListenerJob = viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading

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

            // 🌟 關鍵點：開始收集來自 Room Database 的冷流（Flow）
            repository.getChatMessagesFlow(roomId).collect { localMessages ->
                // 只要本地資料庫有任何風吹草動（多一條假訊息、狀態改變、被刪除），這裡都會立刻觸發
                _uiState.value = MessagesUiState.Success(
                    roomTitle = title,
                    partnerStatus = status,
                    partnerAvatarUrl = avatarUrl,
                    messages = localMessages, // 👈 直接使用來自 Room 的最新訊息列表
                    currentUserId = currentLoggedInUserId
                )
            }
        }

        // 🌟 背景默默同步：去後端拉取最新訊息寫入 Room
        viewModelScope.launch {
            repository.refreshChatMessages(roomId).onSuccess {
                // 已讀功能保持
                repository.markAsRead(roomId).onFailure { error ->
                    Log.e("ChatViewModel", "標記已讀失敗: ${error.message}")
                }
            }.onFailure { error ->
                Log.e("ChatViewModel", "背景同步失敗，改用純離線快取", error)
                // 這裡不用特地切換到 Error 狀態，因為 getChatMessagesFlow 可能已經把離線快取顯示在畫面上了
            }
        }
    }

    // =====================================================================
    // 💡 核心改造二：發送文字訊息（樂觀更新）
    // =====================================================================
    fun sendMessage(roomId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            val replyToId = _replyingMessage.value?.id
            val currentLoggedInUserId = tokenManager.currentUserId.first() ?: ""

            // 呼叫樂觀更新方法
            val result = repository.sendMessageOptimistically(
                roomId = roomId,
                currentUserId = currentLoggedInUserId,
                content = content,
                replyToId = replyToId
            )

            // 清空回覆狀態
            _replyingMessage.value = null

            result.onSuccess {
                // 🌟 發送成功！此時 Room 內對應的臨時訊息已經被改為 SUCCESS 了
                // Flow 會自動讓 UI 刷新，ViewModel 這裡「不需要」手動做 updatedMessages + newMessage
                _sendMessageState.value = SendMessageState.Idle
            }.onFailure { error ->
                Log.e("ChatViewModel", "Send message failed", error)
                // 🌟 發送失敗！Room 內的臨時訊息已被改為 FAILED，畫面上會顯示驚嘆號
                _sendMessageState.value = SendMessageState.Error(error.message ?: "未知錯誤")
            }
        }
    }

    // =====================================================================
    // 💡 核心改造三：刪除訊息
    // =====================================================================
    fun deleteMessage(roomId: String, messageId: String) {
        viewModelScope.launch {
            _deleteMessageState.value = DeleteMessageState.Loading

            val result = repository.deleteMessage(roomId, messageId)

            result.onSuccess {
                _deleteMessageState.value = DeleteMessageState.Success
                clearActionMessage()

                // 🌟 刪除成功！Repository 已經把本地 Room 的資料刪除
                // Flow 會自動發送全新的列表給 UI，這裡「完全不需要」再手動 map 改 content 了！
            }.onFailure { error ->
                Log.e("ChatViewModel", "Delete message failed", error)
                _deleteMessageState.value = DeleteMessageState.Error(error.message ?: "刪除訊息失敗")
            }
        }
    }

    // --- 其餘 uploadAttachment、Emoji 功能保持原樣 ---
    fun uploadAttachment(roomId: String, fileUri: Uri) {
        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading
            val result = repository.uploadAttachment(roomId, fileUri)
            result.onSuccess { newMessage ->
                _sendMessageState.value = SendMessageState.Success(newMessage)
                // 注意：如果上傳檔案未來也想做樂觀更新，可以用跟 sendMessage 相同的邏輯改造 Repository。
                // 如果目前保持原樣，因為原本的 loadMessages 是 Flow，它會持續監聽，只要 refreshChatMessages 被觸發或後端有推播，它就會出現。
                // 為了保險起見，若 uploadAttachment 還沒改造，可以先留著手動塞入的邏輯，或者直接在成功後呼叫 repository.refreshChatMessages(roomId)
            }.onFailure { error ->
                Log.e("ChatViewModel", "Upload attachment failed", error)
                _sendMessageState.value = SendMessageState.Error(error.message ?: "上傳失敗")
            }
        }
    }

    private val _reactionUsersState = MutableStateFlow<ReactionUsersState>(ReactionUsersState.Idle)
    val reactionUsersState: StateFlow<ReactionUsersState> = _reactionUsersState.asStateFlow()

    fun loadMessageReactionUsers(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            _reactionUsersState.value = ReactionUsersState.Loading
            repository.getMessageReactionUsers(roomId, messageId, emoji)
                .onSuccess { response -> _reactionUsersState.value = ReactionUsersState.Success(response.users) }
                .onFailure { error -> _reactionUsersState.value = ReactionUsersState.Error(error.message ?: "無法取得按讚名單") }
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

    fun resetDeleteMessageState() { _deleteMessageState.value = DeleteMessageState.Idle }
    fun resetReactionUsersState() { _reactionUsersState.value = ReactionUsersState.Idle }
    fun resetSendMessageState() { _sendMessageState.value = SendMessageState.Idle }
}