package com.example.login_v3.home.Message.ViewModel.Detail

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
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    init {
        // 畫面一建立就自動抓取資料
        fetchFriends()
        fetchPendingRequests()
    }


    //friends list
    fun fetchFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFriendList()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, friends = list) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }


    // 獲取待處理請求 (API: /api/friends/pending)
    fun fetchPendingRequests() {
        viewModelScope.launch {
            repository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                }
                .onFailure { /* 處理錯誤 */ }
        }
    }
}