package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import android.content.Context
import android.net.Uri
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

    // 新增 1：讀取自訂壁紙路徑的 StateFlow
    val customWallpaperPath: StateFlow<String?> = repository.wallpaperPathFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // 初始值為 null，代表還沒有自訂壁紙
        )

    // 4. 更新：除了更新狀態，還要存入 DataStore
    fun updateTheme(newTheme: AppTheme) {
        Log.d("ThemeDebug", "ViewModel 更新主題並儲存至 DataStore: $newTheme")
        viewModelScope.launch {
            repository.saveThemeMode(newTheme.name) // 儲存 Enum 的名稱 (例如 "DARK")
        }
    }

    /**
     * 新增 2：呼叫 Repository 儲存壁紙圖片
     */
    fun uploadWallpaper(context: Context, uri: Uri) {
        Log.d("ThemeDebug", "ViewModel 開始上傳壁紙: $uri")
        viewModelScope.launch {
            val success = repository.saveWallpaper(context, uri)
            if (success) {
                Log.d("ThemeDebug", "壁紙儲存成功")
            } else {
                Log.e("ThemeDebug", "壁紙儲存失敗")
            }
        }
    }

    /**
     * 新增 3：呼叫 Repository 刪除壁紙圖片並清理 DataStore
     */
    fun deleteWallpaper(context: Context) {
        Log.d("ThemeDebug", "ViewModel 開始刪除壁紙")
        viewModelScope.launch {
            repository.clearWallpaper(context)
        }
    }
}