package com.example.login_v3.home.Message.ViewModel.Detail

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import androidx.room.Query

enum class MessageStatus { SENDING, SUCCESS, FAILED }

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String, // 臨時訊息可以用 UUID.randomUUID().toString()
    val roomId: String,
    val content: String,
    val senderId: String,
    val timestamp: Long,
    val status: MessageStatus // 👈 核心：用來控制 UI 顯示樣式
)

@Dao
interface MessageDao {
    // 💡 監聽特定聊天室的訊息流
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesFlow(roomId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)
}