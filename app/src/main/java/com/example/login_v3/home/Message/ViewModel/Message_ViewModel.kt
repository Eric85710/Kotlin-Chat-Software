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
import com.example.login_v3.data.repository.basic.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val repository: ChatRoomsRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    // 🌟 核心優化：直接將 Repository 的 Flow 轉換為 StateFlow
    // SharingStarted.Eagerly 確保 ViewModel 一建立就開始讀取資料庫
    val roomsState: StateFlow<List<ChatRoom>?> = repository.chatRoomsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        // App 啟動時或進入頁面時默默同步一次
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            Log.d("ChatRoomsDebug", "📡 [ChatRoomsViewModel] 觸發同步...")
            val result = syncRepository.performSync()
            result.onFailure { error ->
                Log.e("ChatRoomsDebug", "❌ 同步失敗", error)
            }
        }
    }
}

enum class UserStatus(val color: Color) {
    ONLINE(Color(0xFF4CAF50)),
    BUSY(Color(0xFFF44336)),
    OFFLINE(Color(0xFF9E9E9E)),
    UNKNOWN(Color.Transparent); 

    companion object {
        fun fromString(status: String?): UserStatus {
            if (status == null) return UNKNOWN

            // 使用 try-catch 或者是 safe valueOf 確保絕對不閃退
            return try {
                // uppercase() 預防後端一下給大寫一下給小寫 (例如 "online" vs "ONLINE")
                val normalizedStatus = status.uppercase()
                when (normalizedStatus) {
                    "ONLINE" -> ONLINE
                    "BUSY" -> BUSY
                    "OFFLINE" -> OFFLINE
                    else -> OFFLINE // 如果是其他自定義狀態，統一顯示為離線 (灰色)
                }
            } catch (e: Exception) {
                OFFLINE
            }
        }
    }
}