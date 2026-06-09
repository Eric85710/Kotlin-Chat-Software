package com.example.login_v3.home.Message.ViewModel.Detail

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import com.example.login_v3.data.api.api_class.Reaction

enum class MessageStatus { SENDING, SUCCESS, FAILED }

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatRoomId: String,  // 👈 對齊你的 @Json(name = "chat_room_id")
    val senderId: String,
    val content: String,
    val type: String,        // 👈 新增：文字、圖片等類型
    val createdAt: String,   // 👈 核心修正：改成與 API 一致的 String 格式
    val isEdited: Boolean,   // 👈 新增
    val isDeleted: Boolean,  // 👈 新增
    val replyToId: String?,  // 👈 新增
    val status: MessageStatus, // 👈 樂觀更新狀態控制 (SENDING, SUCCESS, FAILED)
    val reactions: List<Reaction>?
)

@Dao
interface MessageDao {
    // 1. 根據 roomId 監聽整個聊天室的訊息 Flow
    @Query("SELECT * FROM messages WHERE chatRoomId = :roomId ORDER BY createdAt DESC")
    fun getMessagesFlow(roomId: String): Flow<List<MessageEntity>>

    // 2. ✨【新增】根據訊息 id 查詢單筆資料（樂觀更新備份用）
    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    // 3. 插入或更新單筆訊息
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(message: MessageEntity)

    // 4. ✨【優化】支援批次插入或更新（可以順便把 refreshChatMessages 裡面的 forEach 換掉，效能更好）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateList(messages: List<MessageEntity>)

    // 5. 根據 id 刪除特定訊息
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)
}