package com.example.login_v3.home.setting.setting_detail_page.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.data.api.api_class.UserProfile
import com.example.login_v3.data.api.api_class.UserProfileUpdateRequest
import com.example.login_v3.data.repository.basic.PersonalProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    //get profile
    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val result = repository.getMyProfile()

            result
                .onSuccess { profile ->
                    _uiState.value = ProfileUiState.Success(profile)
                }
                .onFailure { e ->
                    _uiState.value =
                        ProfileUiState.Error(e.localizedMessage ?: "未知錯誤")
                }
        }
    }

    //update profile
    fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        bannerUrl: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            val request = UserProfileUpdateRequest(
                display_name = displayName,
                bio = bio,
                avatar_url = avatarUrl,
                banner_url = bannerUrl,
                status = status
            )

            repository.patchUserProfile(request).onSuccess {
                fetchProfile()
            }.onFailure {
                // 處理錯誤
            }
        }
    }

    // upload image
    // 1. 定義一個通用的處理邏輯
    private fun handleImageUpload(
        context: Context,
        uri: Uri,
        isAvatar: Boolean // 用來區分檔案名稱或特定邏輯
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            try {
                // 從 Uri 讀取檔案
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    _uiState.value = ProfileUiState.Error("無法讀取檔案")
                    return@launch
                }

                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                // 根據類型決定檔名（雖然通常後端只看 Part Name，但給正確副檔名比較保險）
                val fileName = if (isAvatar) "avatar.jpg" else "banner.jpg"
                val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

                // 根據類型呼叫對應的 Repository 方法
                val result = if (isAvatar) {
                    repository.uploadAvatar(body)
                } else {
                    repository.uploadBanner(body)
                }

                result.onSuccess {
                    fetchProfile() // 成功後統一刷新資料
                }.onFailure { e ->
                    _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "上傳失敗")
                }

            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("處理檔案時發生錯誤: ${e.message}")
            }
        }
    }

    // 2. 對外暴露的簡潔方法
    fun uploadAvatar(context: Context, uri: Uri) = handleImageUpload(context, uri, true)
    fun uploadBanner(context: Context, uri: Uri) = handleImageUpload(context, uri, false)

}