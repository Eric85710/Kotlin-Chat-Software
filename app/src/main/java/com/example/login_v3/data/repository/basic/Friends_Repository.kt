package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import javax.inject.Inject

class FriendsRepository @Inject constructor(
    private val apiService: TecnologiaApi
) {

    suspend fun getFriendList(): Result<List<Friend>>  {
        return try {
            val response = apiService.getFriends()
            // 回傳成功，並直接提取裡面的 List<Friend>
            Result.success(response.friends)
        } catch (e: Exception) {
            // 處理連線超時、404、500 等各種錯誤
            Result.failure(e)
        }
    }

    suspend fun getPendingRequests(): Result<List<PendingFriendApiModel>> {
        return try {
            val response = apiService.getFriendsPending()
            // 這裡回傳 API Model，轉換邏輯可以放在 ViewModel
            Result.success(response.pendingRequests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}