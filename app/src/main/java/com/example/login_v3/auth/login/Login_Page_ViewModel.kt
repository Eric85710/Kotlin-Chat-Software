package com.example.login_v3.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.UserInfo
import com.example.login_v3.data.repository.basic.AuthRepository
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
    private val repository: AuthRepository
) : ViewModel() {

    // 用於內部修改的私有狀態
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    // 暴露給 Compose 觀察的公開狀態
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            try {
                val request = LoginRequest(username = username, password = password)
                val response = repository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    // 登入成功，儲存 User 資訊
                    _loginState.value = LoginUiState.Success(response.body()!!.user)

                    // 提示：通常這裡還會處理 Token 持久化（儲存在 DataStore 或 EncryptedSharedPreferences）
                } else {
                    _loginState.value = LoginUiState.Error("登入失敗：${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("網路異常：${e.localizedMessage}")
            }
        }
    }
}