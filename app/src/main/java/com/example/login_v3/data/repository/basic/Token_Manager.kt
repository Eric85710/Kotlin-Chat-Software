package com.example.login_v3.data.repository.basic

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.flatMapLatest

class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val allUserIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[USER_ID_LIST] ?: emptySet()
    }

    companion object {
        private val USER_ID_LIST = stringSetPreferencesKey("user_id_list")
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")

        private fun accessTokenKey(userId: String) = stringPreferencesKey("access_token_$userId")
        private fun refreshTokenKey(userId: String) = stringPreferencesKey("refresh_token_$userId")
        private fun expiresAtKey(userId: String) = longPreferencesKey("expires_at_$userId")
    }

    // 儲存資料（維持原樣，這是安全的）
    suspend fun saveAuthData(userId: String, accessToken: String, refreshToken: String, expiresInSec: Int) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey(userId)] = accessToken
            preferences[refreshTokenKey(userId)] = refreshToken

            val expiresAt = System.currentTimeMillis() + (expiresInSec * 1000L)
            preferences[expiresAtKey(userId)] = expiresAt

            preferences[CURRENT_USER_ID] = userId

            val currentList = preferences[USER_ID_LIST] ?: emptySet()
            preferences[USER_ID_LIST] = currentList + userId
        }
    }

    val currentUserId: Flow<String?> = dataStore.data.map { it[CURRENT_USER_ID] }

    // 🌟 核心修正一：提供一個可以直接傳入 userId 獲取 Token 的 suspend 函式
    // 這樣在 Authenticator 內，當你已經有 currentUserId 時，直接用這個抓最安全！
    suspend fun getAccessToken(userId: String): String? {
        return dataStore.data.map { preferences ->
            preferences[accessTokenKey(userId)]
        }.first()
    }

    // 🌟 核心修正二：改用 flatMapLatest (需要 import kotlinx.coroutines.flow.flatMapLatest)
    // 確保當 CURRENT_USER_ID 改變時，舊的 Flow 會被立即取消並重導向，防止讀到髒資料
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentAccessToken: Flow<String?> = currentUserId.flatMapLatest { userId ->
        dataStore.data.map { preferences ->
            if (userId == null) null else preferences[accessTokenKey(userId)]
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentRefreshToken: Flow<String?> = currentUserId.flatMapLatest { userId ->
        dataStore.data.map { preferences ->
            if (userId == null) null else preferences[refreshTokenKey(userId)]
        }
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