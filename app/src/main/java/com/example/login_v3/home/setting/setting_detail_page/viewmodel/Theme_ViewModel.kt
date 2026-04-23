package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.login_v3.ui.theme.AppTheme

class Theme_ViewModel : ViewModel() {
    var currentTheme by mutableStateOf(AppTheme.DARK)
        private set

    fun updateTheme(newTheme: AppTheme) {
        currentTheme = newTheme
    }
}