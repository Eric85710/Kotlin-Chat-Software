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
    // 💡 修正這裡：WHERE 條件欄位改為 chatRoomId
    @Query("SELECT * FROM messages WHERE chatRoomId = :roomId ORDER BY createdAt DESC") // 👈 確保是 DESC（降序）
    fun getMessagesFlow(roomId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)
}