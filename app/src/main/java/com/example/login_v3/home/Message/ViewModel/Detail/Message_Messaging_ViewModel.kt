package com.example.login_v3.home.Message.ViewModel.Detail


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


//message sending state
sealed class SendMessageState {
    object Idle : SendMessageState()      // 閒置狀態（預設）
    object Loading : SendMessageState()   // 發送中
    data class Success(val message: Message) : SendMessageState() // 發送成功
    data class Error(val message: String) : SendMessageState()    // 發送失敗
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


    fun sendMessage(roomId: String, content: String, replyToId: String? = null) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            val result = repository.sendMessage(roomId, content, replyToId)

            result.onSuccess { newMessage ->
                _sendMessageState.value = SendMessageState.Success(newMessage)

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

    // ✨ 4. 新增：重設發送狀態的 function
    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }
}