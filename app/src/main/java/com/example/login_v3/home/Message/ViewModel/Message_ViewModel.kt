// Message_ViewModel.kt
package com.example.login_v3.home.Message.ViewModel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.repository.dm.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



//message sending state
sealed class SendMessageState {
    object Idle : SendMessageState()      // 閒置狀態（預設）
    object Loading : SendMessageState()   // 發送中
    data class Success(val message: Message) : SendMessageState() // 發送成功，並帶回新訊息
    data class Error(val message: String) : SendMessageState()    // 發送失敗，帶有錯誤訊息
}


@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val repository: ChatRoomsRepository
) : ViewModel() {

    private val _roomsState = MutableStateFlow<List<ChatRoom>>(emptyList())
    val roomsState: StateFlow<List<ChatRoom>> = _roomsState

    // 1. 新增：發送訊息的 UI 狀態 Flow
    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState

    fun loadRooms() {
        viewModelScope.launch {
            val result = repository.fetchRooms()
            result.onSuccess { response ->
                _roomsState.value = response.rooms

                response.rooms.forEach { room ->
                    val status = room.partner?.status
                    val name = room.partner?.displayName
                    println("Debug_API: User: $name, Status: '$status'")
                }
            }.onFailure { error ->
                Log.e("ChatRoomsViewModel", "Fetch failed", error)
            }
        }
    }

    // 2. 新增：發送訊息的 function
    fun sendMessage(roomId: String, content: String, replyToId: String? = null) {
        // 如果內容是空的，就直接攔截不送出
        if (content.isBlank()) return

        viewModelScope.launch {
            // 切換成載入中狀態
            _sendMessageState.value = SendMessageState.Loading

            // 呼叫 repository
            val result = repository.sendMessage(roomId, content, replyToId)

            result.onSuccess { newMessage ->
                // 發送成功
                _sendMessageState.value = SendMessageState.Success(newMessage)

                // 【可選】如果你的聊天訊息列表也是歸這個 ViewModel 管，
                // 你可以在這裡順便把新訊息加進當前的訊息列表 Flow 中，這樣畫面就會立刻跳出新訊息。

            }.onFailure { error ->
                // 發送失敗
                Log.e("ChatRoomsViewModel", "Send message failed", error)
                _sendMessageState.value = SendMessageState.Error(error.message ?: "未知錯誤")
            }
        }
    }

    // 3. 新增：重設發送狀態（通常在 UI 顯示完錯誤跳出視窗、或成功送出清空輸入框後呼叫）
    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }
}

enum class UserStatus(val color: Color) {
    ONLINE(Color.Green),
    OFFLINE(Color.Gray),
    UNKNOWN(Color.Transparent); // 或者是你原本設定的顏色

    companion object {
        fun fromString(status: String?): UserStatus {
            if (status == null) return UNKNOWN

            // 使用 try-catch 或者是 safe valueOf 確保絕對不閃退
            return try {
                // uppercase() 預防後端一下給大寫一下給小寫 (例如 "online" vs "ONLINE")
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN // 找不到對應的狀態就安全地回傳 UNKNOWN
            }
        }
    }
}