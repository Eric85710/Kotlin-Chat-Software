package com.example.login_v3.data.repository.dm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.MessageReactionUsersResponse
import com.example.login_v3.data.api.api_class.MessageResponse
import com.example.login_v3.data.api.api_class.RoomListResponse
import com.example.login_v3.data.api.api_class.SendMessageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomsRepository @Inject constructor(
    private val api: TecnologiaApi,
    @ApplicationContext private val context: Context // 注入 ApplicationContext 用於處理 Uri 檔案
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

                    // 💡 核心邏輯：將訊息列表轉成 Map，方便用 ID 快速查找
                    val messageMap = body.messages.associateBy { it.id }

                    // 走訪每一條訊息，如果它有 replyToId，就去 Map 裡面找出那一條訊息塞給它
                    body.messages.forEach { message ->
                        if (!message.replyToId.isNullOrBlank()) {
                            message.repliedMessage = messageMap[message.replyToId]
                        }
                    }

                    Result.success(body)
                } else {
                    Result.failure(Exception("回應身體為空 (Empty response body)"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("網路請求失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
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


    //upload file
    suspend fun uploadAttachment(roomId: String, fileUri: Uri): Result<Message> {
        return try {
            val contentResolver = context.contentResolver
            var fileName = "upload_file"

            // 1. 透過 ContentResolver 取得檔案的名稱
            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            // 2. 取得原始 MIME 類型
            val rawMimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"

            val fileBytes: ByteArray
            val finalMimeType: String

            // ✨【關鍵轉檔邏輯】：如果是 HEIC/HEIF，在前端直接壓成 JPG
            if (rawMimeType == "image/heic" || rawMimeType == "image/heif") {
                val inputStream = contentResolver.openInputStream(fileUri)
                    ?: return Result.failure(Exception("無法開啟檔案輸入流"))

                // 利用 BitmapFactory 解碼，Android 系統會自動幫你把 HEIC 解成標準 Bitmap
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    val outputStream = ByteArrayOutputStream()
                    // 壓縮成 JPEG 格式，品質設為 85-90（兼顧畫質與檔案大小）
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    fileBytes = outputStream.toByteArray()
                    outputStream.close()
                    bitmap.recycle() // 釋放記憶體

                    finalMimeType = "image/jpeg"

                    // 修改副檔名為 .jpg，讓後端正確識別儲存
                    val dotIndex = fileName.lastIndexOf(".")
                    fileName = if (dotIndex != -1) {
                        "${fileName.substring(0, dotIndex)}.jpg"
                    } else {
                        "$fileName.jpg"
                    }
                } else {
                    return Result.failure(Exception("HEIC 圖片解碼失敗"))
                }
            } else {
                // 非 HEIC 檔案（普通的 JPG, PNG 等），走原本的邏輯，直接讀取原始 Byte
                val inputStream = contentResolver.openInputStream(fileUri)
                    ?: return Result.failure(Exception("無法開啟檔案輸入流"))
                fileBytes = inputStream.use { it.readBytes() }
                finalMimeType = rawMimeType
            }

            // 3. 轉換為 RequestBody
            val requestBody = fileBytes.toRequestBody(finalMimeType.toMediaTypeOrNull())

            // 4. 打包成 MultipartBody.Part (後端接收的 Key 值設定為 "file")
            val multipartBody = MultipartBody.Part.createFormData("file", fileName, requestBody)

            // 5. 呼叫 API
            val response = api.uploadAttachment(roomId, multipartBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("上傳成功但回應身體為空 (Empty response body)"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("上傳檔案失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 取得點擊特定訊息中某個 Emoji 的使用者清單
    suspend fun getMessageReactionUsers(
        roomId: String,
        messageId: String,
        emoji: String
    ): Result<MessageReactionUsersResponse> {
        return try {
            // 呼叫 API
            val response = api.getMessageReactionUsers(roomId, messageId, emoji)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("回應成功但回應身體為空 (Empty response body)"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("取得反應使用者失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            // 捕捉網路斷線等異常狀況
            Result.failure(e)
        }
    }

    // 新增訊息的 Emoji 反應
    suspend fun addMessageReaction(
        roomId: String,
        messageId: String,
        emoji: String
    ): Result<Unit> {
        return try {
            val response = api.addMessageReaction(roomId, messageId, emoji)

            if (response.isSuccessful) {
                // 204 成功時沒有 body，直接回傳 Unit
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("新增反應失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}