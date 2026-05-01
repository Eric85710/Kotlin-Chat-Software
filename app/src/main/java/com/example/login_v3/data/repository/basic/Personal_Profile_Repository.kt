package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.UserProfile
import javax.inject.Inject

class PersonalProfileRepository @Inject constructor(
    private val api: TecnologiaApi
) {
    suspend fun getMyProfile(): UserProfile {
        val response = api.getUserProfile()
        if (response.isSuccessful) {
            // body()!! 雖然簡單，但建議確保 API 回傳格式正確
            return response.body() ?: throw Exception("回傳資料為空")
        } else {
            // 這裡可以根據 response.code() 給出更詳細的錯誤訊息
            throw Exception("讀取失敗 (${response.code()})")
        }
    }
}