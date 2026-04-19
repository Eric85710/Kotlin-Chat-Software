package com.example.login_v3.data.health

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.repository.basic.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthCheckViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    // 使用 Compose 的 mutableState，這樣 UI 會自動根據這個值的改變而重繪
    var healthStatus by mutableStateOf("Ready to Check")
        private set

    // 用來顯示更詳細的訊息（例如錯誤內容）
    var detailMessage by mutableStateOf("")
        private set

    fun checkApiHealth() {
        viewModelScope.launch {
            healthStatus = "Checking..."
            detailMessage = "Connecting to server..."

            val result = repository.performHealthCheck()

            result.onSuccess {
                healthStatus = "Success"
                detailMessage = "✅ Connection established! (200 OK)"
            }.onFailure { exception ->
                healthStatus = "Failed"
                // 這裡會抓到連線逾時、404、或是找不到伺服器等錯誤
                detailMessage = "❌ Error: ${exception.localizedMessage}"
            }
        }
    }
}