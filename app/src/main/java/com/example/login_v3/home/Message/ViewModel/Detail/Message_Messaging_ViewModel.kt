package com.example.login_v3.home.Message.ViewModel.Detail


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.ViewModel.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(
        val roomTitle: String,
        val partnerStatus: UserStatus,
        val messages: List<Message>
    ) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRoomsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    fun loadMessages(roomId: String) {
        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading

            // 1. 同時獲取房間資訊與訊息列表
            val roomResult = repository.getChatRoom(roomId) // ⚠️ 需確保 Repository 有提供此方法
            val messagesResult = repository.getChatMessages(roomId)

            if (messagesResult.isSuccess) {
                val messagesResponse = messagesResult.getOrNull()!!

                // 2. 根據房間資訊決定標題邏輯
                val room = roomResult.getOrNull()
                val (title, status) = if (room != null) {
                    if (room.roomType == "DM") {
                        val name = room.partner?.displayName ?: room.partner?.username ?: "未知用戶"
                        val userStatus = UserStatus.fromString(room.partner?.status)
                        Pair(name, userStatus)
                    } else {
                        Pair(room.roomName ?: "未命名群組", UserStatus.UNKNOWN)
                    }
                } else {
                    // 如果真的拿不到房間資訊，給個安全預設值
                    Pair("聊天室", UserStatus.UNKNOWN)
                }

                _uiState.value = MessagesUiState.Success(
                    roomTitle = title,
                    partnerStatus = status,
                    messages = messagesResponse.messages
                )
            } else {
                val exception = messagesResult.exceptionOrNull()
                _uiState.value = MessagesUiState.Error(message = exception?.message ?: "未知錯誤")
            }
        }
    }
}