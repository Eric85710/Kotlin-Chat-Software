package com.example.login_v3.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.UserInfo
import com.example.login_v3.data.repository.basic.AuthRepository
import com.example.login_v3.data.repository.basic.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val tokenManager: TokenManager // 注入 TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val request = LoginRequest(username = username, password = password)
                val response = repository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 1. 先把 Token 存入 DataStore
                    tokenManager.saveAuthData(loginResponse.access_token)

                    // 2. 更新 UI 狀態
                    _loginState.value = LoginUiState.Success(loginResponse.user)
                } else {
                    _loginState.value = LoginUiState.Error("登入失敗：${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("網路異常：${e.localizedMessage}")
            }
        }
    }
}