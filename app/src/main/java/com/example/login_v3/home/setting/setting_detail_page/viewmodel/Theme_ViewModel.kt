package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.local.UserPreferences.UserPreferencesRepository
import com.example.login_v3.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // 1. 加上 Hilt 註解
class Theme_ViewModel @Inject constructor(
    private val repository: UserPreferencesRepository // 2. 注入 Repository
) : ViewModel() {

    // 3. 讀取：將 repository 的 Flow 映射為 AppTheme，並轉為 StateFlow
    val currentTheme: StateFlow<AppTheme> = repository.themeModeFlow
        .map { themeName ->
            try {
                AppTheme.valueOf(themeName.uppercase()) // 假設 AppTheme 是 Enum
            } catch (e: Exception) {
                AppTheme.DARK // 轉換失敗時的預設值
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.DARK // 初始值
        )

    // 4. 更新：除了更新狀態，還要存入 DataStore
    fun updateTheme(newTheme: AppTheme) {
        Log.d("ThemeDebug", "ViewModel 更新主題並儲存至 DataStore: $newTheme")
        viewModelScope.launch {
            repository.saveThemeMode(newTheme.name) // 儲存 Enum 的名稱 (例如 "DARK")
        }
    }
}