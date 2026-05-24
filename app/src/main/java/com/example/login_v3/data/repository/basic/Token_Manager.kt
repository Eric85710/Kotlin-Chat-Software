package com.example.login_v3.data.repository.basic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // 獲取所有已登入的使用者 ID 清單
    val allUserIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[USER_ID_LIST] ?: emptySet()
    }

    // TokenManager 內部建議改動
    companion object {
        private val USER_ID_LIST = stringSetPreferencesKey("user_id_list") // 移到這裡
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        private fun accessTokenKey(userId: String) = stringPreferencesKey("access_token_$userId")
    }

    // 儲存特定使用者的 Token
    suspend fun saveAuthData(userId: String, token: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey(userId)] = token
            preferences[CURRENT_USER_ID] = userId

            // 更新 ID 清單：把新的 userId 加進去
            val currentList = preferences[USER_ID_LIST] ?: emptySet()
            preferences[USER_ID_LIST] = currentList + userId
        }
    }

    // 獲取「當前活躍使用者」的 ID
    val currentUserId: Flow<String?> = dataStore.data.map { it[CURRENT_USER_ID] }

    // 自動讀取「當前帳號」的 Token
    val currentAccessToken: Flow<String?> = dataStore.data.map { preferences ->
        val userId = preferences[CURRENT_USER_ID]
        if (userId != null) {
            preferences[accessTokenKey(userId)]
        } else {
            null
        }
    }

    // 切換帳號：只需更改 CURRENT_USER_ID
    suspend fun switchAccount(userId: String) {
        dataStore.edit { preferences ->
            // 先檢查該 userId 的 token 是否存在
            if (preferences.contains(accessTokenKey(userId))) {
                preferences[CURRENT_USER_ID] = userId
            }
        }
    }

    // 登出特定帳號
    suspend fun logout(userId: String) {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey(userId))

            // 從 ID 清單中移除
            val currentList = preferences[USER_ID_LIST] ?: emptySet()
            preferences[USER_ID_LIST] = currentList - userId

            // 如果登出的是當前帳號，自動選另一個帳號或清空
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
}