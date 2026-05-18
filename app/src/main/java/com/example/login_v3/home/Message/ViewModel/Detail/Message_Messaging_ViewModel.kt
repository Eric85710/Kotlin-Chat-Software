package com.example.login_v3.home.Message.ViewModel.Detail


import android.util.Log
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

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    fun loadMessages(roomId: String) {
        // 🔴 Debug 1: 檢查傳進來的 roomId 是不是正確的，還是空的、被錯誤編碼的？
        Log.d("ChatDebug", "ViewModel: loadMessages 開始執行, roomId = '$roomId'")

        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading

            repository.getChatMessages(roomId)
                .onSuccess { response ->
                    // 🔴 Debug 2: 檢查後端有沒有正確回傳，數量是多少？
                    Log.d("ChatDebug", "ViewModel: API 請求成功, 訊息數量 = ${response.messages.size}")

                    _uiState.value = MessagesUiState.Success(messages = response.messages)
                }
                .onFailure { exception ->
                    // 🔴 Debug 3: 檢查是不是 API 報錯（例如 404, 500）或是解析 JSON 失敗
                    Log.e("ChatDebug", "ViewModel: API 請求失敗", exception)

                    _uiState.value = MessagesUiState.Error(message = exception.message ?: "未知錯誤")
                }
        }
    }
}