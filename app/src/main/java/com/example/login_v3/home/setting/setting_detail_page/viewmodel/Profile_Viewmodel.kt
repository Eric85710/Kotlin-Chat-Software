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
    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            // 如果你想要在上傳時讓 UI 顯示 Loading，可以在這開啟
            // _uiState.value = ProfileUiState.Loading

            try {
                // 1. 從 Uri 取得 InputStream 並讀取 Bytes
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() } ?: return@launch

                // 2. 封裝成 RequestBody (指定為圖片格式)
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                // 3. 建立 MultipartBody.Part
                // 注意："avatar" 必須跟後端 @Part("avatar") 裡面的名稱一模一樣
                val body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile)

                // 4. 呼叫 Repository
                repository.uploadAvatar(body)
                    .onSuccess {
                        // 上傳成功後，最保險的做法是重新 fetch 一次個人資料
                        // 這樣 UI 上的頭像網址才會更新成後端最新儲存的網址
                        fetchProfile()
                    }
                    .onFailure { e ->
                        // 處理錯誤，例如發送一個錯誤訊息給 UI
                        _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "上傳失敗")
                    }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("讀取檔案出錯: ${e.message}")
            }
        }
    }

}