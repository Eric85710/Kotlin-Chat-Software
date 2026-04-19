package com.example.login_v3.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.LoginResponse
import com.example.login_v3.data.repository.basic.ITecnologiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: ITecnologiaRepository
) : ViewModel() {

    // 使用 StateFlow 或 LiveData 來傳遞結果給 UI
    private val _loginResult = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginResult = _loginResult.asStateFlow()

    fun performLogin(username: String, password: String) {
        viewModelScope.launch {
            // 直接呼叫 repository，ViewModel 不關心資料是從網路還是資料庫來的
            val result = repository.login(username, password)
            _loginResult.value = result
        }
    }
}