package com.example.login_v3.home.Message.ViewModel.Detail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.login_v3.data.api.api_class.Message


sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRoomsRepository
) : ViewModel() {

    // 內部的可變狀態，預設為 Loading
    private val _uiState = mutableStateOf<MessagesUiState>(MessagesUiState.Loading)

    // 公開給 Compose UI 讀取的唯讀狀態
    val uiState: State<MessagesUiState> = _uiState

    /**
     * 根據 roomId 獲取聊天訊息
     */
    fun loadMessages(roomId: String) {
        viewModelScope.launch {
            // 切換回載入中狀態（適用於切換房間或重新整理時）
            _uiState.value = MessagesUiState.Loading

            // 呼叫你的 Repository 方法
            repository.getChatMessages(roomId)
                .onSuccess { response ->
                    // 成功：將訊息列表塞入 Success 狀態
                    _uiState.value = MessagesUiState.Success(messages = response.messages)
                }
                .onFailure { exception ->
                    // 失敗：擷取錯誤訊息並塞入 Error 狀態
                    _uiState.value = MessagesUiState.Error(message = exception.message ?: "未知錯誤")
                }
        }
    }
}