package com.example.login_v3.home.Message.ViewModel.Detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.api.api_class.UserDetail
import com.example.login_v3.data.repository.basic.FriendsRepository
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
    // 新增：搜尋結果與關鍵字
    val searchResults: List<UserDetail> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)
@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: FriendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            // 1. 開始 Loading 並清除舊錯誤
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. 使用 zip 或單獨 fetch，這裡示範簡單的循序或並行處理
            val friendsResult = repository.getFriendList()
            val pendingResult = repository.getPendingRequests()

            // 3. 統一處理結果
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    friends = friendsResult.getOrDefault(state.friends),
                    pendingRequests = pendingResult.getOrDefault(state.pendingRequests),
                    error = (friendsResult.exceptionOrNull() ?: pendingResult.exceptionOrNull())?.message
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
            // 1. 顯示載入中，並清除舊的錯誤訊息
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. 呼叫 Repository
            val result = repository.sendFriendRequest(friendId)

            // 3. 處理結果
            result.onSuccess {
                // 發送成功後，通常會重新整理資料
                // 這樣如果你的「待處理清單」包含「已發送的申請」，畫面就會更新
                refreshAll()
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
        _uiState.update { it.copy(searchQuery = newQuery) }
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