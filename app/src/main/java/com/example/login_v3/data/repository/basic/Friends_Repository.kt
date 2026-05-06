package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.Friend
import javax.inject.Inject

class FriendsRepository @Inject constructor
    (private val apiService: TecnologiaApi) {

    suspend fun getFriendList(): Result<List<Friend>> {
        return try {
            val response = apiService.getFriends()
            // 回傳成功，並直接提取裡面的 List<Friend>
            Result.success(response.friends)
        } catch (e: Exception) {
            // 處理連線超時、404、500 等各種錯誤
            Result.failure(e)
        }
    }
}