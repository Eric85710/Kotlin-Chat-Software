package com.example.login_v3.auth.reg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.RegisterRequest
import com.example.login_v3.data.repository.basic.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 在 RegisterViewModel.kt 中修改
data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // 使用 StateFlow 管理 UI 狀態
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun handleRegister(display_name: String, email: String, pass: String, user: String) {
        val request = RegisterRequest(display_name, email, pass, user)

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true) // 開始載入
            try {
                val response = repository.register(request)
                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState(isSuccess = true)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "註冊失敗"
                    _uiState.value = RegisterUiState(errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(errorMessage = "網路異常: ${e.message}")
            }
        }
    }
}