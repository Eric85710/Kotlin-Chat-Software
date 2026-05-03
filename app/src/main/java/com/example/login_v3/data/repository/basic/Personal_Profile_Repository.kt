package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.UserProfile
import com.example.login_v3.data.api.api_class.UserProfileUpdateRequest
import okhttp3.MultipartBody
import javax.inject.Inject

class PersonalProfileRepository @Inject constructor(
    private val api: TecnologiaApi // 確保你的 API interface 裡有 getUserProfile 和 updateProfile
) {
    // 取得資料
    suspend fun getMyProfile(): Result<UserProfile> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("回傳資料為空"))
            } else {
                Result.failure(Exception("讀取失敗 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 更新資料 (PATCH)
    suspend fun patchUserProfile(request: UserProfileUpdateRequest): Result<Unit> {
        return try {
            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("更新失敗: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 上傳頭像圖片 (POST)
    suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<Unit> {
        return try {
            val response = api.uploadAvatar(avatarPart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("圖片上傳失敗: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
