// Message_ViewModel.kt
package com.example.login_v3.home.Message.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.ChatRoom
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

                // 打印偵錯訊息
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

