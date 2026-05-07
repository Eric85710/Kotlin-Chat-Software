package com.example.login_v3.home.Message.ViewModel.Detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.repository.basic.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ContactUiState(
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<PendingFriendApiModel> = emptyList(),
    val error: String? = null
)
@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: FriendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    // 🚩 建議刪除獨立的 _friends，全部統一使用 _uiState

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchFriends()
        fetchPendingRequests()
    }

    fun fetchFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.getFriendList()

            result.onSuccess { list ->
                Log.d("DEBUG_API", "好友列表抓取成功，數量: ${list.size}")
                _uiState.update { it.copy(isLoading = false, friends = list) }
            }.onFailure { error ->
                Log.e("DEBUG_API", "好友列表抓取失敗: ${error.message}")
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun fetchPendingRequests() {
        viewModelScope.launch {
            val result = repository.getPendingRequests()

            result.onSuccess { requests ->
                Log.d("DEBUG_API", "待處理請求抓取成功，數量: ${requests.size}")
                _uiState.update { it.copy(pendingRequests = requests) }
            }.onFailure { error ->
                // 🚩 這裡一定要加 Log，否則 API 壞了你不知道
                Log.e("DEBUG_API", "待處理請求抓取失敗: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }
}