package com.example.login_v3.data.repository.dm

import com.example.login_v3.data.api.TecnologiaApi
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
}