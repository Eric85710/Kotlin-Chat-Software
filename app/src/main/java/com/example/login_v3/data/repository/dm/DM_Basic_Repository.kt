package com.example.login_v3.data.repository.dm

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.MessageResponse
import com.example.login_v3.data.api.api_class.RoomListResponse
import com.example.login_v3.data.api.api_class.SendMessageRequest
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

    // 在 ChatRoomsRepository 裡面加入：
    suspend fun getChatRoom(roomId: String): Result<ChatRoom?> {
        return try {
            // 呼叫你原本就有的 fetchRooms() 獲取所有房間
            val responseResult = fetchRooms()

            responseResult.map { roomListResponse ->
                // 從列表裡面找出 roomId 相同的那間房間
                roomListResponse.rooms.find { it.roomId == roomId }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //read ed message
    suspend fun markAsRead(roomId: String): Result<Unit> {
        return try {
            val response = api.markAsRead(roomId)

            if (response.isSuccessful) {
                // 204 成功時沒有 body，直接回傳 Unit 即可
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("標記已讀失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //send message
    suspend fun sendMessage(
        roomId: String,
        content: String,
        replyToId: String? = null
    ): Result<Message> {
        return try {
            // 打包 Request Body
            val requestBody = SendMessageRequest(
                content = content,
                replyToId = replyToId
            )

            // 呼叫 API
            val response = api.sendMessage(roomId, requestBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("發送成功但回應身體為空 (Empty response body)"))
                }
            } else {
                // 抓取後端傳回的錯誤訊息
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("發送訊息失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            // 捕捉網路斷線或解析 JSON 失敗等狀況
            Result.failure(e)
        }
    }
}