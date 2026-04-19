package com.example.login_v3.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ScreensViewModel : ViewModel() {
    private val _selected = MutableStateFlow<Screen>(Screen.Message)
    val selected: StateFlow<Screen> = _selected

    fun select(screen: Screen) {
        _selected.value = screen
    }
}

// BottomBarViewModel 放在同一個檔案，但仍是獨立的 ViewModel 類別
class BottomBarViewModel : ViewModel() {
    private val _showBottomBar = MutableStateFlow(true)
    val showBottomBar: StateFlow<Boolean> = _showBottomBar

    fun setVisible(visible: Boolean) {
        _showBottomBar.value = visible
    }
}