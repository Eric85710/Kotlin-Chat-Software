package com.example.login_v3.data.repository.basic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val allUserIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[USER_ID_LIST] ?: emptySet()
    }

    companion object {
        private val USER_ID_LIST = stringSetPreferencesKey("user_id_list")
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")

        // 動態生成每個 User 專屬的 Key
        private fun accessTokenKey(userId: String) = stringPreferencesKey("access_token_$userId")
        private fun refreshTokenKey(userId: String) = stringPreferencesKey("refresh_token_$userId")
        private fun expiresAtKey(userId: String) = longPreferencesKey("expires_at_$userId")
    }

    // 儲存特定使用者的完整 Auth 資料（新增了 refreshToken 與 expiresIn）
    suspend fun saveAuthData(userId: String, accessToken: String, refreshToken: String, expiresInSec: Int) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey(userId)] = accessToken
            preferences[refreshTokenKey(userId)] = refreshToken

            // 計算絕對過期時間戳記：當前時間（毫秒）+ 有效秒數 * 1000
            val expiresAt = System.currentTimeMillis() + (expiresInSec * 1000L)
            preferences[expiresAtKey(userId)] = expiresAt

            preferences[CURRENT_USER_ID] = userId

            val currentList = preferences[USER_ID_LIST] ?: emptySet()
            preferences[USER_ID_LIST] = currentList + userId
        }
    }

    val currentUserId: Flow<String?> = dataStore.data.map { it[CURRENT_USER_ID] }

    // 獲取當前帳號的 Access Token
    val currentAccessToken: Flow<String?> = dataStore.data.map { preferences ->
        val userId = preferences[CURRENT_USER_ID] ?: return@map null
        preferences[accessTokenKey(userId)]
    }

    // 💡 新增：獲取當前帳號的 Refresh Token（供 Authenticator 使用）
    val currentRefreshToken: Flow<String?> = dataStore.data.map { preferences ->
        val userId = preferences[CURRENT_USER_ID] ?: return@map null
        preferences[refreshTokenKey(userId)]
    }

    // 切換帳號、登出等邏輯保持不變，但登出時記得一併移除 refresh_token 與 expires_at
    suspend fun logout(userId: String) {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey(userId))
            preferences.remove(refreshTokenKey(userId))
            preferences.remove(expiresAtKey(userId))

            val currentList = preferences[USER_ID_LIST] ?: emptySet()
            preferences[USER_ID_LIST] = currentList - userId

            if (preferences[CURRENT_USER_ID] == userId) {
                val remainingUser = (currentList - userId).firstOrNull()
                if (remainingUser != null) {
                    preferences[CURRENT_USER_ID] = remainingUser
                } else {
                    preferences.remove(CURRENT_USER_ID)
                }
            }
        }
    }

    suspend fun switchAccount(userId: String) {
        // 目前暫不實作
    }
}