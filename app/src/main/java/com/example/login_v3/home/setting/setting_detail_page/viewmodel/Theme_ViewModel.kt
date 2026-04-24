package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.login_v3.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Theme_ViewModel : ViewModel() {
    private val _currentTheme = MutableStateFlow(AppTheme.DARK)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun updateTheme(newTheme: AppTheme) {
        Log.d("ThemeDebug", "ViewModel 更新主題為: $newTheme")
        _currentTheme.value = newTheme
    }
}