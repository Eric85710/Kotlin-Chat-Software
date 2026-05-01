package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.UserProfile
import com.example.login_v3.data.repository.basic.PersonalProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 定義 UI 狀態
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class PersonalProfileViewModel @Inject constructor(
    private val repository: PersonalProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // 初始化時自動抓取
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = repository.getMyProfile()
                _uiState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                // 如果是 401，Interceptor 會處理登出，
                // 這裡處理一般的網路錯誤或 API 錯誤
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "未知錯誤")
            }
        }
    }
}