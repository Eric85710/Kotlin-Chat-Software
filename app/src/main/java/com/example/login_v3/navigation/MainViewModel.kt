package com.example.login_v3.navigation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.basic.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel : ViewModel() {

    var selectedTab by mutableStateOf(1)
        private set

    fun selectTab(index: Int) {
        selectedTab = index
    }
}


//init navigation
enum class AppScreen {
    PreReg, Login, Register, Home, ScreensTab, Loading, AccountSwitch, AddAccount
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Loading)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // 修改：監聽當前活躍帳號的 Token
    val userToken: StateFlow<String?> = tokenManager.currentAccessToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 修改：監聽「是否至少有一個帳號登入中」
    private val hasAccountLoggedIn: Flow<Boolean> = tokenManager.allUserIds.map { it.isNotEmpty() }

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // 修改：根據「帳號清單是否為空」來決定初始頁面
            hasAccountLoggedIn.collect { hasAccount ->
                if (hasAccount) {
                    // 只要清單不為空，就進入主頁面
                    _currentScreen.value = AppScreen.ScreensTab
                } else {
                    // 如果清單空了（所有人都登出了），回首頁/註冊頁
                    _currentScreen.value = AppScreen.PreReg
                }
            }
        }
    }

    fun goTo(screen: AppScreen) {
        Log.d("NavTest", "Attempting to go to: $screen")
        _currentScreen.value = screen
        Log.d("NavTest", "Current state value: ${_currentScreen.value}")
    }

    // 修改：登出當前帳號
    fun onLogout() {
        viewModelScope.launch {
            // 先獲取目前的 ID
            val currentId = tokenManager.currentUserId.first()
            currentId?.let {
                tokenManager.logout(it)
            }
            // logout 完後，hasAccountLoggedIn 的 collect 會自動觸發頁面跳轉邏輯
        }
    }
}
