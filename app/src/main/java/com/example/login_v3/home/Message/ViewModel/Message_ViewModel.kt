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






@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val repository: ChatRoomsRepository
) : ViewModel() {

    private val _roomsState = MutableStateFlow<List<ChatRoom>>(emptyList())
    val roomsState: StateFlow<List<ChatRoom>> = _roomsState

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