package com.example.login_v3.auth.reg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.RegisterRequest
import com.example.login_v3.data.repository.basic.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    fun handleRegister() {
        val request = RegisterRequest(
            display_name = "測試員",
            email = "test@example.com",
            password = "password123",
            username = "tester01"
        )

        viewModelScope.launch {
            try {
                val response = repository.register(request)
                if (response.isSuccessful) {
                    // 註冊成功，處理 response.body()
                    val successMsg = response.body()?.message
                    println("註冊成功: $successMsg")
                } else {
                    // 註冊失敗，處理錯誤訊息 (例如 400 重複註冊)
                    val errorJson = response.errorBody()?.string()
                    println("註冊失敗: $errorJson")
                }
            } catch (e: Exception) {
                // 處理網路連線異常
                println("網路錯誤: ${e.message}")
            }
        }
    }
}