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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(roomId: String): Result<MessageResponse> {
        return try {
            val response = api.getChatMessages(roomId) // 確保 TecnologiaApi 裡有 getChatMessages 方法

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("回應身體為空 (Empty response body)"))
                }
            } else {
                Result.failure(Exception("網路請求失敗，錯誤碼: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}