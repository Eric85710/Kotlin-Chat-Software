package com.example.login_v3.data.repository.dm

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.MessageResponse
import com.example.login_v3.data.api.api_class.RoomListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomsRepository @Inject constructor(
    private val api: TecnologiaApi
) {

    suspend fun fetchRooms(): Result<RoomListResponse> {
        return try {
            val response = api.getChatRooms()
            if (response.isSuccessful) {
                // 使用 body() ?: return ... 可以減少一層 if 巢狀結構
                val body = response.body() ?: return Result.failure(Exception("Rooms response body is null"))
                Result.success(body)
            } else {
                Result.failure(Exception("Error code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(roomId: String): Result<MessageResponse> {
        return try {
            val response = api.getChatMessages(roomId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    // 如果後端 200 OK 但給空身體，這裏處理
                    Result.failure(Exception("回應身體為空 (Empty response body)"))
                }
            } else {
                // 建議把後端的錯誤訊息也抓出來（如果後端有寫 error body 的話）
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("網路請求失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            // 捕捉網路斷線 (IOException) 或解析失敗 (JsonDataException) 等狀況
            Result.failure(e)
        }
    }
}