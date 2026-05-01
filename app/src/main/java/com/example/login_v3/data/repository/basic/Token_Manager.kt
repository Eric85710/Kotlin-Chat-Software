package com.example.login_v3.data.repository.basic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        // 建議 Key 名稱與變數名稱一致
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val IS_LOGIN = booleanPreferencesKey("is_login")
    }

    // 1.2.1 版本依然使用 suspend edit
    suspend fun saveAuthData(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
            preferences[IS_LOGIN] = true
        }
    }

    // 使用 Flow 讀取資料
    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val isLogin: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGIN] ?: false
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}