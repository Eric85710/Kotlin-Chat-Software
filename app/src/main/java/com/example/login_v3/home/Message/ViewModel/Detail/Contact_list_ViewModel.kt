package com.example.login_v3.home.Message.ViewModel.Detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.repository.basic.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor
    (
    private val repository: FriendsRepository
            ) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    init {
        // 畫面一建立就自動抓取資料
        fetchFriends()
    }

    fun fetchFriends() {
        viewModelScope.launch {
            repository.getFriendList()
                .onSuccess { list ->
                    _friends.value = list
                }
                .onFailure {
                    // 這裡可以處理錯誤訊息，例如顯示 Toast
                }
        }
    }
}