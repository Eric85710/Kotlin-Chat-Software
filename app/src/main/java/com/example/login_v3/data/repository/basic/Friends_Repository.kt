package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.AddFriendRequest
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import retrofit2.Response
import javax.inject.Inject

class FriendsRepository @Inject constructor(
    private val apiService: TecnologiaApi
) {

    suspend fun getFriendList(): Result<List<Friend>> {
        return try {
            val response = apiService.getFriends()
            // 如果 apiService 直接回傳 FriendListResponse (非 Response 封裝)
            // 則直接取用，並處理 null
            Result.success(response.friends ?: emptyList())
        } catch (e: Exception) {
            // 這裡可以根據 e 的型別做額外處理，例如 HttpException 或 IOException
            Result.failure(e)
        }
    }


    //pending list
    suspend fun getPendingRequests(): Result<List<PendingFriendApiModel>> {
        return try {
            val response = apiService.getFriendsPending()
            // 這裡同樣建議處理可能為 null 的情況
            Result.success(response.pendingRequests ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 接受
    suspend fun acceptFriendRequest(id: String) = safeApiCall {
        apiService.acceptFriendRequest(id)
    }

    // 拒絕
    suspend fun rejectFriendRequest(id: String) = safeApiCall {
        apiService.rejectFriendRequest(id)
    }

    // 統一處理 Response<Unit> 的工具函式
    private suspend fun safeApiCall(call: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("API 錯誤: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //add friends
    suspend fun sendFriendRequest(friendId: String): Result<Unit> {
        return safeApiCall {
            // 確保這裡的參數名稱與 Data Class 定義的一模一樣
            val request = AddFriendRequest(friendId = friendId)
            apiService.sendFriendRequest(request)
        }
    }

}