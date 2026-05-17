package com.example.login_v3.home.Message.ViewModel.Detail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.login_v3.data.api.api_class.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRoomsRepository
) : ViewModel() {

    // 1. 使用 MutableStateFlow，並設定初始值
    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)

    // 2. 使用 asStateFlow() 公開唯讀的 StateFlow
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    fun loadMessages(roomId: String) {
        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading

            repository.getChatMessages(roomId)
                .onSuccess { response ->
                    // 3. 更新 Flow 的值
                    _uiState.value = MessagesUiState.Success(messages = response.messages)
                }
                .onFailure { exception ->
                    _uiState.value = MessagesUiState.Error(message = exception.message ?: "未知錯誤")
                }
        }
    }
}