package com.example.login_v3.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.UserInfo
import com.example.login_v3.data.repository.basic.AuthRepository
import com.example.login_v3.data.repository.basic.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserInfo) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    // --- 新增：供 UI 顯示所有已登入帳號與當前帳號 ---
    val allUserIds: StateFlow<Set<String>> = tokenManager.allUserIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val currentUserId: StateFlow<String?> = tokenManager.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    // ------------------------------------------

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val request = LoginRequest(username = username, password = password)
                val response = repository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 💡 1. 提取 user 物件並用 val 鎖定，讓 Kotlin 可以進行 Smart Cast
                    val userInfo = loginResponse.user

                    if (userInfo != null) {
                        // 這裡的 userInfo 已經被 Kotlin 自動辨別為「絕對非空」的 UserInfo 了
                        val userId = userInfo.id

                        tokenManager.saveAuthData(
                            userId = userId,
                            accessToken = loginResponse.access_token,
                            refreshToken = loginResponse.refresh_token,
                            expiresInSec = loginResponse.expires_in
                        )

                        _loginState.value = LoginUiState.Success(userInfo)
                    } else {
                        // 💡 2. 防禦後端雖然回傳 200，但 user 物件卻是 null 的極端狀況
                        _loginState.value = LoginUiState.Error("登入失敗：伺服器未回傳用戶資訊")
                    }
                } else {
                    _loginState.value = LoginUiState.Error("登入失敗：${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("網路異常：${e.localizedMessage}")
            }
        }
    }

    // --- 新增：切換帳號功能 ---
    fun switchAccount(userId: String) {
        viewModelScope.launch {
            tokenManager.switchAccount(userId)
            // 切換後，通常會需要重新初始化某些頁面資料，或發送一個事件通知 UI
        }
    }

    // --- 新增：登出功能 ---
    fun logout(userId: String) {
        viewModelScope.launch {
            tokenManager.logout(userId)
        }
    }
}