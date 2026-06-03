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
            Log.d("ChatRoomsDebug", "1. 開始呼叫 loadRooms()...")
            val result = repository.fetchRooms()

            result.onSuccess { response ->
                val roomsList = response.rooms
                Log.d("ChatRoomsDebug", "2. API 請求成功！拿到 ${roomsList?.size ?: 0} 個聊天室")

                if (roomsList.isNullOrEmpty()) {
                    Log.w("ChatRoomsDebug", "⚠️ 注意：後端回傳的 rooms 列表是空的(或為 null)！")
                } else {
                    roomsList.forEach { room ->
                        Log.d("ChatRoomsDebug", "-> 房間 ID: ${room.roomId}, 名稱: ${room.roomName}, Partner: ${room.partner?.displayName}, 狀態: ${room.partner?.status}")
                    }
                }

                // 更新狀態
                _roomsState.value = roomsList ?: emptyList()
                Log.d("ChatRoomsDebug", "3. _roomsState 已更新，當前長度: ${_roomsState.value.size}")

            }.onFailure { error ->
                // ⭐ 這裡非常重要，一定要印出 stackTrace 才知道為什麼失敗（例如：JSON 解析失敗、404、網路斷線）
                Log.e("ChatRoomsDebug", "❌ API 請求失敗 (onFailure)!!", error)
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