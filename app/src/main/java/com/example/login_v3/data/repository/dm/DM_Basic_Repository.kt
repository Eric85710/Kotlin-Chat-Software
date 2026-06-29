package com.example.login_v3.data.repository.dm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.datastore.core.use
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.MessageReactionUsersResponse
import com.example.login_v3.data.api.api_class.MessageResponse
import com.example.login_v3.data.api.api_class.RoomListResponse
import com.example.login_v3.data.api.api_class.SendMessageRequest
import com.example.login_v3.data.di.ChatWebSocketManager
import com.example.login_v3.home.Message.ViewModel.Detail.MessageDao
import com.example.login_v3.home.Message.ViewModel.Detail.MessageEntity
import com.example.login_v3.home.Message.ViewModel.Detail.MessageStatus
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.use
import kotlin.jvm.java
import kotlin.use

@Singleton
class ChatRoomsRepository @Inject constructor(
    private val api: TecnologiaApi,
    private val messageDao: MessageDao,
    private val webSocketManager: ChatWebSocketManager, // 👈 1. 注入 Socket 管理器
    private val moshi: Moshi, // 👈 用於解析 Socket 傳來的 JSON
    @ApplicationContext private val context: Context // 注入 ApplicationContext 用於處理 Uri 檔案
) {

    // 建立一個跟著 Repository 生命週期走的 Scope
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // 👈 2. 在初始化時，開始默默監聽全域的 WebSocket 訊息
        observeWebSocketIncomingData()
    }

    private fun observeWebSocketIncomingData() {
        repositoryScope.launch {
            webSocketManager.incomingMessages.collect { jsonString ->
                try {
                    // 1. 🎯 精準對齊：使用你的 Message::class.java 進行解析
                    val networkMessage = moshi.adapter(Message::class.java).fromJson(jsonString)

                    if (networkMessage != null) {
                        // 2. 將 Message 轉換為你的 MessageEntity
                        val entity = MessageEntity(
                            id = networkMessage.id,
                            chatRoomId = networkMessage.chatRoomId, // 👈 完美對齊你的欄位名稱
                            senderId = networkMessage.senderId,
                            content = networkMessage.content,
                            type = networkMessage.type,
                            createdAt = networkMessage.createdAt,
                            isEdited = networkMessage.isEdited,
                            isDeleted = networkMessage.isDeleted,
                            replyToId = networkMessage.replyToId,
                            status = MessageStatus.SUCCESS, // WebSocket 推過來的必定是成功發送的
                            reactions = networkMessage.reactions
                        )

                        // 3. 寫入本地資料庫，觸發 UI Flow 更新
                        messageDao.insertOrUpdate(entity)
                    }
                } catch (e: Exception) {
                    Log.e("ChatRoomsRepository", "解析或寫入 WebSocket 即時訊息失敗", e)
                }
            }
        }
    }

    // 👈 4. 新增：提供給 ViewModel 在進入聊天室時呼叫的啟動連線功能
    fun startChatSession(roomId: String) {
        val webSocketUrl = "wss://tg.technologia-tw.com/api/ws?token=\$jwt"

        // 2. 啟動連線
        webSocketManager.connect(webSocketUrl)
    }

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

    fun getChatMessagesFlow(roomId: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(roomId).map { entities ->
            val domainMessages = entities.map { entity ->
                Message(
                    id = entity.id,
                    chatRoomId = entity.chatRoomId, // 👈 完美對齊
                    senderId = entity.senderId,
                    content = entity.content,
                    type = entity.type,
                    createdAt = entity.createdAt,   // 👈 String 對 String
                    isEdited = entity.isEdited,
                    isDeleted = entity.isDeleted,
                    replyToId = entity.replyToId,
                    status = entity.status,          // 👈 把 Room 儲存的狀態倒出來給 UI
                    reactions = entity.reactions
                )
            }

            // 你原本的處理回覆邏輯
            val messageMap = domainMessages.associateBy { it.id }
            domainMessages.forEach { message ->
                if (!message.replyToId.isNullOrBlank()) {
                    message.repliedMessage = messageMap[message.replyToId]
                }
            }
            domainMessages
        }
    }

    // api get data and put to local
    suspend fun refreshChatMessages(roomId: String): Result<Unit> {
        return try {
            val response = api.getChatMessages(roomId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {

                    // 1. 使用 map 把網路資料轉換成 Entity 列表
                    val entities = body.messages.map { networkMessage ->
                        MessageEntity(
                            id = networkMessage.id,
                            chatRoomId = roomId,
                            senderId = networkMessage.senderId,
                            content = networkMessage.content,
                            type = networkMessage.type,
                            createdAt = networkMessage.createdAt,
                            isEdited = networkMessage.isEdited,
                            isDeleted = networkMessage.isDeleted,
                            replyToId = networkMessage.replyToId,
                            status = MessageStatus.SUCCESS,
                            reactions = networkMessage.reactions
                        )
                    }

                    // 2. 一口氣整批塞進資料庫，Room 只會開啟一次資料庫事務 (Transaction)
                    messageDao.insertOrUpdateList(entities)

                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty body"))
                }
            } else {
                Result.failure(Exception("Error code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //fake sending
    suspend fun sendMessageOptimistically(
        roomId: String,
        currentUserId: String,
        content: String,
        replyToId: String? = null
    ): Result<Unit> {
        val tempId = UUID.randomUUID().toString()
        // 💡 配合你的 String 格式，產生 ISO 時間字串或乾淨的目前時間字串，例如 2026-06-07T05:26:48Z
        val currentIsoTime = java.time.Instant.now().toString()

        val tempMessageEntity = MessageEntity(
            id = tempId,
            chatRoomId = roomId,
            senderId = currentUserId,
            content = content,
            type = "text", // 預設普通文字
            createdAt = currentIsoTime,
            isEdited = false,
            isDeleted = false,
            replyToId = replyToId,
            status = MessageStatus.SENDING,
            reactions = emptyList()
        )

        messageDao.insertOrUpdate(tempMessageEntity)

        return try {
            val requestBody = SendMessageRequest(content = content, replyToId = replyToId)
            val response = api.sendMessage(roomId, requestBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // 成功：刪除假的，插入真的
                    messageDao.deleteById(tempId)

                    val successEntity = MessageEntity(
                        id = body.id,
                        chatRoomId = roomId,
                        senderId = body.senderId,
                        content = body.content,
                        type = body.type,
                        createdAt = body.createdAt, // 後端確認的正式時間
                        isEdited = body.isEdited,
                        isDeleted = body.isDeleted,
                        replyToId = body.replyToId,
                        status = MessageStatus.SUCCESS,
                        reactions = body.reactions
                    )
                    messageDao.insertOrUpdate(successEntity)
                    Result.success(Unit)
                } else {
                    messageDao.insertOrUpdate(tempMessageEntity.copy(status = MessageStatus.FAILED))
                    Result.failure(Exception("Body null"))
                }
            } else {
                messageDao.insertOrUpdate(tempMessageEntity.copy(status = MessageStatus.FAILED))
                Result.failure(Exception("API Error"))
            }
        } catch (e: Exception) {
            messageDao.insertOrUpdate(tempMessageEntity.copy(status = MessageStatus.FAILED))
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

    //mark read message
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

    // 移除訊息的 Emoji 反應
    suspend fun deleteMessageReaction(
        roomId: String,
        messageId: String,
        emoji: String
    ): Result<Unit> {

        // 1. 先從本地撈出原本的訊息作為備份
        val backupEntity = try {
            messageDao.getMessageById(messageId)
        } catch (e: Exception) {
            null
        }

        // 2. 樂觀更新：立刻在本地修改 Reaction 狀態
        if (backupEntity != null) {
            try {
                val currentReactions = backupEntity.reactions ?: emptyList()

                // 運算修改後的 Reactions 列表
                val updatedReactions = currentReactions.map { reaction ->
                    if (reaction.emoji == emoji) {
                        // 將數量減 1，並把自己點擊的狀態設為 false
                        reaction.copy(
                            count = (reaction.count - 1).coerceAtLeast(0),
                            meReacted = false
                        )
                    } else {
                        reaction
                    }
                }.filter { it.count > 0 } // 如果數量歸零，就直接從畫面上移除該 Emoji

                // 將更新後的反應放回 Entity，並寫入資料庫觸發 UI 更新
                val updatedEntity = backupEntity.copy(reactions = updatedReactions)
                messageDao.insertOrUpdate(updatedEntity)
            } catch (e: Exception) {
                // 如果本地資料庫寫入失敗，直接回傳錯誤，不戳 API
                return Result.failure(e)
            }
        }

        // 3. 呼叫後端 API
        return try {
            val response = api.deleteMessageReaction(roomId, messageId, emoji)

            if (response.isSuccessful) {
                // 真正成功！因為本地已經提早改好了，什麼都不用做
                Result.success(Unit)
            } else {
                // 後端回報失敗（例如：網路突然斷開、伺服器錯誤）：把剛才備份的原始資料塞回去，讓 UI 彈回原本的數字
                backupEntity?.let { messageDao.insertOrUpdate(it) }
                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("移除反應失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            // 網路斷線或 Timeout：回滾資料庫，讓 UI 彈回來
            backupEntity?.let { messageDao.insertOrUpdate(it) }
            Result.failure(e)
        }
    }


    // 刪除訊息（樂觀更新版本）
    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> {
        // 1. 先從本地資料庫撈出原本的訊息作為備份
        val backupEntity = try {
            messageDao.getMessageById(messageId)
        } catch (e: Exception) {
            null // 如果本地查詢就失敗，雖然機率極低，但我們還是設為 null 防呆
        }

        // 2. 樂觀更新：立刻從本地砍掉，UI 的 Flow 會在「一瞬間」同步消失，體驗極佳
        try {
            messageDao.deleteById(messageId)
        } catch (e: Exception) {
            // 如果本地 Room 刪除指令本身就失敗，直接回傳錯誤，不繼續往下戳 API
            return Result.failure(e)
        }

        // 3. 呼叫後端 API 進行實際刪除
        return try {
            val response = api.deleteMessage(roomId, messageId)

            if (response.isSuccessful) {
                // 真正成功了！因為本地已經提早刪除了，所以什麼都不用做，直接回傳 Unit
                Result.success(Unit)
            } else {
                // 後端回報失敗（例如：沒權限、訊息已被他人刪除）：把剛才備份的資料塞回去
                backupEntity?.let { messageDao.insertOrUpdate(it) }

                val errorMsg = response.errorBody()?.string() ?: "未知錯誤"
                Result.failure(Exception("刪除失敗，錯誤碼: ${response.code()}, 訊息: $errorMsg"))
            }
        } catch (e: Exception) {
            // 網路斷線、Timeout 或伺服器崩潰：一樣把備份資料塞回去，讓 UI 訊息彈回來
            backupEntity?.let { messageDao.insertOrUpdate(it) }
            Result.failure(e)
        }
    }



    //download file
    private val httpClient = OkHttpClient()
    fun downloadFileFromUrlFlow(fileUrl: String, fileName: String): Flow<Result<Float>> = flow {
        // 🎯 1. 檢查並自動拼接完整的 Base URL
        val finalUrl = if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            fileUrl
        } else {
            // 💡 這裡請換成你專案中實際使用的後端伺服器域名或 IP
            val baseUrl = "https://tg.technologia-tw.com"

            // 確保路徑拼接不會有多餘的斜線
            val cleanedBase = baseUrl.removeSuffix("/")
            val cleanedPath = if (fileUrl.startsWith("/")) fileUrl else "/$fileUrl"
            "$cleanedBase$cleanedPath"
        }

        Log.d("ChatDebug", "🚀 拼接完成後的下載任務\n🔗 修正後 URL: $finalUrl\n📁 檔名: $fileName")

        try {
            // 2. 使用修正後的完整網址發送請求
            val request = Request.Builder()
                .url(finalUrl)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "無回應內容"
                emit(Result.failure(Exception("HTTP 錯誤 ${response.code}: $errorBody")))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(Result.failure(Exception("ResponseBody 為 null")))
                return@flow
            }

            val totalBytes = body.contentLength()
            // 🎯 修正後的寫法：直接存入手機系統的公共 Download 資料夾
            val targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetFile = File(targetDirectory, fileName)

            var bytesCopied = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

            body.byteStream().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    var bytesRead = inputStream.read(buffer)
                    while (bytesRead != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead

                        if (totalBytes > 0) {
                            emit(Result.success(bytesCopied.toFloat() / totalBytes.toFloat()))
                        } else {
                            emit(Result.success(-1f))
                        }
                        bytesRead = inputStream.read(buffer)
                    }
                }
            }

            emit(Result.success(1.0f))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}