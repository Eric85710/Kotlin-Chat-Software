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

                    // 修改：存入 token 時同時存入 userId (假設你的 user 物件裡有 id)
                    // 如果 LoginResponse 沒給 id，也可以用 username 作為唯一識別碼
                    val userId = loginResponse.user.id.toString()

                    tokenManager.saveAuthData(
                        userId = userId,
                        token = loginResponse.access_token
                    )

                    _loginState.value = LoginUiState.Success(loginResponse.user)
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