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

    //reply function
    private val _replyingMessage = MutableStateFlow<Message?>(null)
    val replyingMessage: StateFlow<Message?> = _replyingMessage.asStateFlow()
    // 💡 新增：當使用者點選某條訊息的「回覆」按鈕時呼叫
    fun setReplyingMessage(message: Message?) {
        _replyingMessage.value = message
    }

    //UI state var
    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    //message var
    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState

    fun loadMessages(roomId: String) {
        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading


            //isMe
            val currentLoggedInUserId = tokenManager.currentUserId.first() ?: ""
            // 1. 同時獲取房間資訊與訊息列表
            val roomResult = repository.getChatRoom(roomId) // ⚠️ 需確保 Repository 有提供此方法
            val messagesResult = repository.getChatMessages(roomId)

            if (messagesResult.isSuccess) {
                val messagesResponse = messagesResult.getOrNull()!!

                // 2. 根據房間資訊決定標題邏輯
                val room = roomResult.getOrNull()
                // ✨ 這裡改用 Any? 來接收擴充屬性的結果
                val (title, status, avatarUrl: Any?) = if (room != null) {
                    val name = room.partner?.displayName ?: room.partner?.username ?: "未知用戶"
                    val userStatus = UserStatus.fromString(room.partner?.status)
                    // 👇 直接呼叫你寫好的擴充屬性
                    val avatar = room.partner?.fullContactAvatarUrl
                    Triple(name, userStatus, avatar)
                } else {
                    // 如果沒有房間資訊，就給預設的頭像資源
                    Triple("聊天室", UserStatus.UNKNOWN, R.drawable.avatar_v1)
                }

                _uiState.value = MessagesUiState.Success(
                    roomTitle = title,
                    partnerStatus = status,
                    partnerAvatarUrl = avatarUrl,  //這裡就會是完整的網址或 R.drawable.avatar_v1
                    messages = messagesResponse.messages,
                    currentUserId = currentLoggedInUserId
                )

                //已讀功能
                launch {
                    repository.markAsRead(roomId)
                        .onFailure { error ->
                            // 這裡通常不需要回報給 UI（不用跳 Error 畫面）
                            // 只需要印個 Log 知道出事了就好
                            Log.e("ChatViewModel", "標記已讀失敗: ${error.message}")
                        }
                }
            } else {
                val exception = messagesResult.exceptionOrNull()
                _uiState.value = MessagesUiState.Error(message = exception?.message ?: "未知錯誤")
            }
        }
    }


    //upload image
    fun sendMessage(roomId: String, content: String, replyToId: String? = null) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            // 💡 取得當前是否有正在回覆的訊息 ID
            val replyToId = _replyingMessage.value?.id

            val result = repository.sendMessage(
                roomId = roomId,
                content = content,
                replyToId = replyToId
            )

            result.onSuccess { newMessage ->
                _sendMessageState.value = SendMessageState.Success(newMessage)
                _replyingMessage.value = null

                // 😎【超讚的體驗優化】：發送成功後，直接把新訊息手動塞進現有的 UI 列表裡
                val currentState = _uiState.value
                if (currentState is MessagesUiState.Success) {
                    // 把新訊息加到原本的列表最後面
                    val updatedMessages = currentState.messages + newMessage

                    // 更新 uiState，這樣 Compose 畫面不需要重新 load 網路就能立刻刷新！
                    _uiState.value = currentState.copy(messages = updatedMessages)
                }

            }.onFailure { error ->
                Log.e("ChatViewModel", "Send message failed", error)
                _sendMessageState.value = SendMessageState.Error(error.message ?: "未知錯誤")
            }
        }
    }


    //upload file
    fun uploadAttachment(roomId: String, fileUri: Uri) {
        viewModelScope.launch {
            // 1. 將發送狀態切換為 Loading，讓 UI 顯示進度條
            _sendMessageState.value = SendMessageState.Loading

            // 2. 呼叫 Repository 處理檔案並上傳
            val result = repository.uploadAttachment(roomId, fileUri)

            result.onSuccess { newMessage ->
                // 3. 上傳成功，更新發送狀態
                _sendMessageState.value = SendMessageState.Success(newMessage)

                // 😎【體驗優化】：跟發送文字訊息一樣，直接把帶有圖片的新訊息塞進現有的 UI 列表裡
                val currentState = _uiState.value
                if (currentState is MessagesUiState.Success) {
                    val updatedMessages = currentState.messages + newMessage
                    _uiState.value = currentState.copy(messages = updatedMessages)
                }

            }.onFailure { error ->
                Log.e("ChatViewModel", "Upload attachment failed", error)
                // 4. 上傳失敗，通知 UI 顯示錯誤
                _sendMessageState.value = SendMessageState.Error(error.message ?: "上傳失敗，請稍後再試")
            }
        }
    }

    private val _reactionUsersState = MutableStateFlow<ReactionUsersState>(ReactionUsersState.Idle)
    val reactionUsersState: StateFlow<ReactionUsersState> = _reactionUsersState.asStateFlow()

    //get emoji reaction
    fun loadMessageReactionUsers(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            _reactionUsersState.value = ReactionUsersState.Loading

            val result = repository.getMessageReactionUsers(
                roomId = roomId,
                messageId = messageId,
                emoji = emoji
            )

            result.onSuccess { response ->
                _reactionUsersState.value = ReactionUsersState.Success(response.users)
            }.onFailure { error ->
                Log.e("ChatViewModel", "Get reaction users failed", error)
                _reactionUsersState.value = ReactionUsersState.Error(error.message ?: "無法取得按讚名單")
            }
        }
    }
    //add emoji reaction
    fun addMessageReaction(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            val result = repository.addMessageReaction(roomId, messageId, emoji)

            result.onSuccess {
                // 😎【超讚體驗優化】：點擊成功後，除了通知後端，
                // 也可以選擇直接呼叫 loadMessages(roomId) 來重新拉取最新帶有按讚數量的訊息列表
                loadMessages(roomId)
            }.onFailure { error ->
                Log.e("ChatViewModel", "Add reaction failed", error)
                // 這裡可以依需求決定要不要用 Toast 提示使用者點擊失敗
            }
        }
    }
    //delete emoji reaction
    fun removeMessageReaction(roomId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            val result = repository.deleteMessageReaction(roomId, messageId, emoji)

            result.onSuccess {
                // 移除成功後，重新拉取最新訊息列表以刷新 UI
                loadMessages(roomId)
            }.onFailure { error ->
                Log.e("ChatViewModel", "Remove reaction failed", error)
            }
        }
    }

    // 儲存刪除狀態的變數
    private val _deleteMessageState = MutableStateFlow<DeleteMessageState>(DeleteMessageState.Idle)
    val deleteMessageState: StateFlow<DeleteMessageState> = _deleteMessageState.asStateFlow()

    // 刪除訊息的 function
    fun deleteMessage(roomId: String, messageId: String) {
        viewModelScope.launch {
            _deleteMessageState.value = DeleteMessageState.Loading

            val result = repository.deleteMessage(roomId, messageId)

            result.onSuccess {
                _deleteMessageState.value = DeleteMessageState.Success

                // 😎【超讚的體驗優化】：不重新 call loadMessages，直接在本地修改該訊息的狀態
                val currentState = _uiState.value
                if (currentState is MessagesUiState.Success) {

                    val updatedMessages = currentState.messages.map { message ->
                        if (message.id == messageId) {
                            // 將該則訊息改為已刪除狀態（利用 copy 複製，並帶入對應欄位）
                            message.copy(
                                isDeleted = true,
                                content = "此訊息已被刪除" // 可選擇在這裡直接改 content，或交由 UI 根據 isDeleted 顯示對應文字
                            )
                        } else {
                            message
                        }
                    }

                    // 更新 uiState，Compose 畫面會立刻連動刷新
                    _uiState.value = currentState.copy(messages = updatedMessages)
                }

            }.onFailure { error ->
                Log.e("ChatViewModel", "Delete message failed", error)
                _deleteMessageState.value = DeleteMessageState.Error(error.message ?: "刪除訊息失敗")
            }
        }
    }

    // 重設刪除狀態的 function (供 UI 重置狀態使用)
    fun resetDeleteMessageState() {
        _deleteMessageState.value = DeleteMessageState.Idle
    }

    // 💡 請確保這段程式碼有確實待在 ChatViewModel 裡面
    fun resetReactionUsersState() {
        _reactionUsersState.value = ReactionUsersState.Idle
    }

    //新增：重設發送狀態的 function
    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }
}