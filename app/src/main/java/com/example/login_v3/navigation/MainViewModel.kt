package com.example.login_v3.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var selectedTab by mutableStateOf(1)
        private set

    fun selectTab(index: Int) {
        selectedTab = index
    }
}
