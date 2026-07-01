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
import kotlinx.coroutines.flow.combine
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

    // 監聽當前活躍帳號的 Token
    val userToken: StateFlow<String?> = tokenManager.currentAccessToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 監聽「是否至少有一個帳號登入中」
    private val hasAccountLoggedIn: Flow<Boolean> = tokenManager.allUserIds.map { it.isNotEmpty() }

    init {
        // 1. App 啟動時先檢查一次狀態，決定落腳點
        checkLoginStatus()
        // 2. 額外監聽：處理在 App 使用過程中，因為過期被強制登出的狀況
        observeGlobalLogout()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // 🌟 改用 first()：只在 App 剛打開時決定是要去主頁 (ScreensTab) 還是歡迎頁 (PreReg)
            val hasAccount = hasAccountLoggedIn.first()
            if (hasAccount) {
                _currentScreen.value = AppScreen.ScreensTab
            } else {
                _currentScreen.value = AppScreen.PreReg
            }
        }
    }

    private fun observeGlobalLogout() {
        viewModelScope.launch {
            // 🌟 核心修正：只監聽「是不是連一個登入的帳號都沒有了」
            // 或者是你有明確實作「登出事件通知」
            hasAccountLoggedIn.collect { hasAccount ->
                Log.d("AuthDebug", "ViewModel 收到帳號清單狀態變更 -> hasAccount: $hasAccount, 當前畫面: ${_currentScreen.value}")

                if (!hasAccount) {
                    // 只有在真的完全沒有帳號登入，且不在註冊/登入頁時，才踢回歡迎頁
                    if (_currentScreen.value != AppScreen.PreReg &&
                        _currentScreen.value != AppScreen.Login &&
                        _currentScreen.value != AppScreen.Register) {

                        Log.e("AuthDebug", "🚨 偵測到完全無帳號登入，強制回歡迎頁")
                        _currentScreen.value = AppScreen.PreReg
                    }
                }
            }
        }
    }

    fun goTo(screen: AppScreen) {
        Log.d("NavTest", "Attempting to go to: $screen")
        _currentScreen.value = screen
        Log.d("NavTest", "Current state value: ${_currentScreen.value}")
    }

    // 登出當前帳號
    fun onLogout() {
        viewModelScope.launch {
            // 先獲取目前的 ID
            val currentId = tokenManager.currentUserId.first()
            currentId?.let {
                tokenManager.logout(it)
            }

            // 🌟 登出成功後，主動導回起點頁面
            _currentScreen.value = AppScreen.PreReg
        }
    }
}