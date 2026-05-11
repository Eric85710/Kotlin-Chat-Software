package com.example.login_v3.data.repository.basic

import android.util.Log
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.AddFriendRequest
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.api.api_class.UserDetail
import com.example.login_v3.data.api.api_class.UserSearchResponse
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

    //add friends
    suspend fun sendFriendRequest(friendId: String): Result<Unit> {
        Log.d("API_DEBUG", "準備發送好友申請，目標 ID: $friendId")
        val result = safeApiCall {
            val request = AddFriendRequest(friendId = friendId)
            apiService.sendFriendRequest(request)
        }
        result.onFailure { Log.e("API_DEBUG", "sendFriendRequest 最終失敗: ${it.message}") }
        return result
    }


    //search user
    suspend fun searchUsers(query: String): Result<UserSearchResponse> {
        return safeApiCallWithData {
            apiService.searchUsers(query) // 1. 使用 apiService 2. 確保 apiService 定義回傳 Response<UserSearchResponse>
        }
    }

    // 支援「回傳資料」的通用封裝 for search user
    private suspend fun <T> safeApiCallWithData(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("回傳資料為空"))
                }
            } else {
                val errorMsg = "API 錯誤 ${response.code()}: ${response.errorBody()?.string()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. 確保「全類別」只有這一個 safeApiCall 函式
    private suspend fun safeApiCall(call: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Log.d("API_DEBUG", "請求成功: ${response.raw().request.url}")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorCode = response.code()
                val errorMessage = "API 錯誤 $errorCode: $errorBody"
                Log.e("API_DEBUG", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "連線或執行異常: ${e.message}", e)
            Result.failure(e)
        }
    }

}