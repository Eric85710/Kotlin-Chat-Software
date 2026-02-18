package com.example.login_v3.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var selectedTab by mutableStateOf("home")
        private set

    fun selectTab(route: String) {
        selectedTab = route
    }
}
