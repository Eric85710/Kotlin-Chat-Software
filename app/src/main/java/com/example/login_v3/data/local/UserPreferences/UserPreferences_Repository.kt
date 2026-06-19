package com.example.login_v3.data.local.UserPreferences

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences // 確保是這個 Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        // 新增：儲存自訂壁紙檔案路徑的 Key
        val CUSTOM_WALLPAPER_PATH = stringPreferencesKey("custom_wallpaper_path")
    }

    // 讀取設定：直接在宣告時賦值，簡潔又安全
    val themeModeFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    // 2. 新增：讀取自訂壁紙路徑（預設為 null，代表使用系統預設背景）
    val wallpaperPathFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.CUSTOM_WALLPAPER_PATH]
        }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }


    /**
     * 4. 新增：將用戶選擇的圖片複製到內部存儲，並將路徑存入 DataStore
     * @param context 用於獲取 filesDir 與 contentResolver
     * @param uri 用戶從相簿選擇的圖片 Uri
     * @return 儲存成功返回 true，失敗返回 false
     */
    suspend fun saveWallpaper(context: Context, uri: Uri): Boolean {
        return try {
            // 將圖片固定命名為 "custom_wallpaper.jpg"，這樣每次上傳新圖片會自動覆蓋舊圖
            val fileName = "custom_wallpaper.jpg"
            val targetFile = File(context.filesDir, fileName)

            // 開啟輸入流並複製到 App 的私有目錄下
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // 將檔案的絕對路徑存入 DataStore 記錄起來
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.CUSTOM_WALLPAPER_PATH] = targetFile.absolutePath
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 5. 新增：清除自訂壁紙設定，並刪除手機內的實體檔案（省空間）
     */
    suspend fun clearWallpaper(context: Context) {
        try {
            // 1. 刪除實體檔案
            val fileName = "custom_wallpaper.jpg"
            val targetFile = File(context.filesDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 2. 無論檔案刪除成功與否，都清除 DataStore 內的紀錄
            dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.CUSTOM_WALLPAPER_PATH)
            }
        }
    }
}