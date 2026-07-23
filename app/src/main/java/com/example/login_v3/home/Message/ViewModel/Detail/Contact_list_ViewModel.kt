package com.example.login_v3.home.Message.ViewModel.Detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.api.api_class.UserDetail
import com.example.login_v3.data.repository.basic.FriendsRepository
import com.example.login_v3.data.repository.basic.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val searchResults: List<UserDetail> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,

    //UI state
    val isSearching: Boolean = false
)
@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: FriendsRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        // 1. 監聽本地好友 Flow，一旦資料庫變動，UI 自動刷新
        observeFriends()
        // 2. 啟動時跑一次同步
        refreshAll()
    }

    private fun observeFriends() {
        viewModelScope.launch {
            repository.friendsFlow.collect { friends ->
                _uiState.update { it.copy(friends = friends) }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 🎯 同步核心：跑 Delta-Sync
            val syncResult = syncRepository.performSync()
            // 同時還是要抓一下 Pending Request (目前 Sync API 沒給這塊)
            val pendingResult = repository.getPendingRequests()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    pendingRequests = pendingResult.getOrDefault(state.pendingRequests),
                    error = (syncResult.exceptionOrNull() ?: pendingResult.exceptionOrNull())?.message
                )
            }
        }
    }


    //accept and reject
    fun acceptFriend(friendshipId: String) {
        viewModelScope.launch {

            //loading
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.acceptFriendRequest(friendshipId)

            result.onSuccess {
                // 關鍵：重新整理資料，讓 UI 自動更新
                refreshAll()
            }.onFailure { error ->
                _uiState.update { it.copy(error = "無法接受請求：${error.message}") }
            }
        }
    }
    fun rejectFriend(friendshipId: String) {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            val result = repository.rejectFriendRequest(friendshipId)

            result.onSuccess {
                refreshAll() // 刷新列表，該筆請求會消失
            }.onFailure { error ->
                _uiState.update { it.copy(error = "無法拒絕請求：${error.message}") }
            }
        }
    }

    //add friends
    fun sendFriendRequest(friendId: String) {
        viewModelScope.launch {
            // 1. 顯示載入中
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. 呼叫 Repository
            val result = repository.sendFriendRequest(friendId)

            // 3. 處理結果
            result.onSuccess {
                // 關鍵修改：更新搜尋結果清單中的 isRequestSent 狀態
                _uiState.update { state ->
                    val updatedSearchResults = state.searchResults.map { user ->
                        if (user.id == friendId) {
                            user.copy(isRequestSent = true) // 找到目標，標記為已發送
                        } else {
                            user
                        }
                    }
                    state.copy(
                        isLoading = false,
                        searchResults = updatedSearchResults
                    )
                }
                // refreshAll()
            }.onFailure { error ->
                Log.e("DEBUG_API", "發送好友申請錯誤: ${error.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "發送申請失敗：${error.message}"
                    )
                }
            }
        }
    }



    //search user
    fun onSearchQueryChanged(newQuery: String) {
        // 1. 即時更新文字輸入，確保 UI TextField 顯示順暢
        _uiState.update { it.copy(
            searchQuery = newQuery,
            isSearching = newQuery.isNotBlank()
        ) }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            // 如果關鍵字為空，清空搜尋結果
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            // 2. 顯示 Loading
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 3. 呼叫 Repository
            val result = repository.searchUsers(query)

            // 4. 處理結果
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = response.users // UserSearchResponse 裡的列表
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "搜尋失敗：${error.message}",
                        searchResults = emptyList() // 失敗時清空列表
                    )
                }
            }
        }
    }
}