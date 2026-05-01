package com.example.login_v3.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.basic.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    PreReg, Login, Register, Home, ScreensTab, Loading
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Loading) // 增加一個 Loading 狀態
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // 暴露 Token 給其他需要的 Screen 使用
    val userToken: StateFlow<String?> = tokenManager.accessToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // 讀取 DataStore 中的 isLogin 狀態
            tokenManager.isLogin.collect { loggedIn ->
                if (loggedIn) {
                    _currentScreen.value = AppScreen.ScreensTab
                } else {
                    _currentScreen.value = AppScreen.PreReg // 或 AppScreen.Register
                }
            }
        }
    }

    fun goTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun onLogout() {
        viewModelScope.launch {
            tokenManager.clearAuthData()
            // 清除後，checkLoginStatus 的 collect 會自動觸發切換回 PreReg
        }
    }
}
