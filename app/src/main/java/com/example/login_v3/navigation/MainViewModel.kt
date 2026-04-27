package com.example.login_v3.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    var selectedTab by mutableStateOf(1)
        private set

    fun selectTab(index: Int) {
        selectedTab = index
    }
}


//init navigation
enum class AppScreen {
    PreReg, Login, Register, Home, ScreensTab
}

class AppViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.PreReg)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    fun goTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun onLoginSuccess() {
        _currentScreen.value = AppScreen.Home
    }

    fun onLogout() {
        _currentScreen.value = AppScreen.PreReg
    }
}
