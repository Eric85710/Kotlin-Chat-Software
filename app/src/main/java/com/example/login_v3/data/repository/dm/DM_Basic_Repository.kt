package com.example.login_v3.data.repository.dm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.data.api.api_class.MessageResponse
import com.example.login_v3.data.api.api_class.Reaction
import com.example.login_v3.data.api.api_class.RoomListResponse
import com.example.login_v3.data.api.api_class.SendMessageRequest
import com.example.login_v3.data.api.api_class.WebSocketEventResponse
import com.example.login_v3.data.di.ChatWebSocketManager
import com.example.login_v3.data.repository.basic.TokenManager
import com.example.login_v3.data.local.dao.MessageDao
import com.example.login_v3.data.local.dao.RoomLocalDao
import com.example.login_v3.data.local.entities.MessageEntity
import com.example.login_v3.data.local.entities.MessageStatus
import com.example.login_v3.data.local.entities.RoomLocalEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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


@Singleton
class ChatRoomsRepository @Inject constructor(
    private val api: TecnologiaApi,
    private val messageDao: MessageDao,
    private val roomLocalDao: RoomLocalDao, // 👈 新增：注入房間 DAO
    private val webSocketManager: ChatWebSocketManager, // 👈 1. 注入 Socket 管理器
    private val moshi: Moshi, // 👈 用於解析 Socket 傳來的 JSON
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context // 注入 ApplicationContext 用於處理 Uri 檔案
) {

    // 🌟 核心：監聽本地資料庫的聊天室列表 Flow
    val chatRoomsFlow: Flow<List<ChatRoom>> = roomLocalDao.getAllRoomsFlow().map { entities ->
        entities.map { entity ->
            ChatRoom(
                roomId = entity.roomId,
                roomName = entity.nickname, // 或者 entity.partner?.displayName ? 根據 DTO 結構映射
                roomType = null, // Entity 暫時沒存，可視需求補上
                roomIconUrl = null, // Entity 暫時沒存，可視需求補上
                isMuted = entity.isMuted,
                isPinned = entity.isPinned,
                unreadCount = entity.unreadCount,
                mentionCount = entity.mentionCount,
                partner = entity.partner,
                lastMessage = entity.lastMessage
            )
        }
    }

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
                    // 1. 一口氣全解析完畢（包含外殼與內部的 payload）
                    val event = moshi.adapter(WebSocketEventResponse::class.java).fromJson(jsonString)

                    if (event != null && event.type == "new_message" && event.payload != null) {
                        val payload = event.payload

                        // 2. 直接拿解析好的強型別物件轉換為 MessageEntity
                        val entity = MessageEntity(
                            id = payload.id,
                            chatRoomId = payload.roomId, // 🎯 這裡百分之百能拿到後端的 room_id
                            senderId = payload.senderId,
                            content = payload.content,
                            type = payload.type,
                            createdAt = payload.createdAt,
                            isEdited = payload.isEdited,
                            isDeleted = payload.isDeleted,
                            replyToId = payload.replyToId,
                            status = MessageStatus.SUCCESS,
                            reactions = payload.reactions
                        )

                        // 3. 寫入本地資料庫
                        messageDao.insertOrUpdate(entity)
                        Log.d("ChatRoomsRepository", "WebSocket 訊息成功寫入資料庫: ${entity.content}")

                    } else if (event?.type == "dm:typing") {
                        // 💡 如果未來想要解析 typing 狀態，你可以另外把 event.payload 轉成 Map 來拿資料，因為 typing 的 payload 結構不同
                        // 由於我們把外層改成了 WebSocketMessagePayload?，若 type 是 dm:typing，外層依然可以解析，但 payload 會是 null 或解析失敗
                        // 如果你同時想處理 typing，可以用 `moshi.adapter(Map::class.java)` 單獨處理該事件。
                    }
                } catch (e: Exception) {
                    Log.e("ChatRoomsRepository", "解析或寫入 WebSocket 即時訊息失敗", e)
                }
            }
        }
    }

    // 🎯 1. 改成 suspend fun，以便讀取 tokenManager
    suspend fun startChatSession(roomId: String) {
        // 2. 從你的 tokenManager 撈出最新的 Access Token (與你原本在 OkHttp 裡撈取的方式一致)
        val token = tokenManager.currentAccessToken.first() ?: ""

        if (token.isBlank()) {
            Log.e("Repository", "WebSocket 連線失敗：找不到 Token")
            return
        }

        // 3. 🎯 動態組裝後端指定的網址，把 $jwt 替換成真正的 Token
        val webSocketUrl = "wss://tg.technologia-tw.com/api/ws?token=$token"

        // 4. 啟動連線
        webSocketManager.connect(webSocketUrl)

        // 5. 💡 重要核心：因為是全域連線，後端怎麼知道你現在在看哪一個房間？
        // 通常連線成功後，需要發送一個「訂閱房間」的 JSON 告訴後端：「Eric 現在進入了 roomId 聊天室，請把這個房間的即時訊息丟給我」
        // 如果你們後端的設計是「只要連上，全域所有房間的訊息都會無腦推下來」，那下面這行就可以註解掉。
        webSocketManager.subscribeToRoom(roomId)
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


    //api get message and push to local
    suspend fun refreshChatMessages(
        roomId: String,
        cursor: String? = null,
        limit: Int? = null
    ): Result<MessageResponse> {
        Log.d("ChatDebug", "📡 [Repository] refreshChatMessages 觸發 | roomId: $roomId | cursor: $cursor | limit: $limit")
        return try {
            // 1. 把 cursor 與 limit 傳給 API
            val response = api.getChatMessages(roomId, cursor = cursor, limit = limit)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val firstId = body.messages.firstOrNull()?.id
                    val lastId = body.messages.lastOrNull()?.id
                    Log.d("ChatDebug", "✅ [Repository] API 成功 | 收到 ${body.messages.size} 條訊息 | 首條ID: $firstId | 末條ID: $lastId | nextCursor: ${body.nextCursor}")

                    // 2. 轉換成 Entity 列表
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

                    // 3. 寫入資料庫
                    // 因為你寫了 OnConflictStrategy.REPLACE，歷史訊息會被乖乖追加進去，不會影響現有訊息！
                    messageDao.insertOrUpdateList(entities)

                    // 🎯 4. 關鍵：把整個 body 回傳回去（包含 nextCursor 和 hasMore）
                    // 這樣 ViewModel 才能知道下一次往上滑時，要帶哪一個 cursor！
                    Result.success(body)
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
        val currentIsoTime = java.time.Instant.now().toString()

        val tempMessageEntity = MessageEntity(
            id = tempId,
            chatRoomId = roomId,
            senderId = currentUserId,
            content = content,
            type = "text",
            createdAt = currentIsoTime,
            isEdited = false,
            isDeleted = false,
            replyToId = replyToId,
            status = MessageStatus.SENDING, // 👈 畫面立刻轉圈圈
            reactions = emptyList()
        )

        // 1. 先塞入暫存假資料
        messageDao.insertOrUpdate(tempMessageEntity)

        return try {
            val requestBody = SendMessageRequest(content = content, replyToId = replyToId)
            val response = api.sendMessage(roomId, requestBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {

                    val successEntity = MessageEntity(
                        id = body.id, // 後端給的正式 ID
                        chatRoomId = roomId,
                        senderId = body.senderId,
                        content = body.content,
                        type = body.type,
                        createdAt = body.createdAt,
                        isEdited = body.isEdited,
                        isDeleted = body.isDeleted,
                        replyToId = body.replyToId,
                        status = MessageStatus.SUCCESS, // 👈 狀態轉為成功
                        reactions = body.reactions
                    )

                    // 🎯 2. 【關鍵大優化】利用剛寫好的 Transaction 一口氣替換！
                    // 這會讓 Room 在同一個事務內完成「刪除 tempId + 寫入 successEntity」
                    // UI 監聽的 Flow 只會被觸發一次，完美絕緣任何閃爍問題！
                    messageDao.replaceTempMessageWithSuccess(tempId, successEntity)

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
            // 網路斷線或超時，直接把本地假資料狀態改成 FAILED，UI 就會立刻顯示驚嘆號
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