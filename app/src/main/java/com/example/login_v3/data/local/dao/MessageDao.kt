package com.example.login_v3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.login_v3.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatRoomId = :roomId ORDER BY createdAt DESC")
    fun getMessagesFlow(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateList(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT id FROM messages WHERE chatRoomId = :roomId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMessageId(roomId: String): String?

    @Transaction
    suspend fun replaceTempMessageWithSuccess(tempId: String, successEntity: MessageEntity) {
        deleteById(tempId)
        insertOrUpdate(successEntity)
    }

    @Transaction
    suspend fun insertOrUpdateListPreservingLocalData(messages: List<MessageEntity>) {
        messages.forEach { msg ->
            val existing = getMessageById(msg.id)
            if (existing != null) {
                // 🎯 核心：保留本地已下載的標記與路徑，其餘由網路資料覆蓋
                val updated = msg.copy(
                    isDownloaded = existing.isDownloaded,
                    localPath = existing.localPath
                )
                insertOrUpdate(updated)
            } else {
                insertOrUpdate(msg)
            }
        }
    }

    @Query("UPDATE messages SET isDownloaded = :isDownloaded, localPath = :localPath WHERE id = :messageId")
    suspend fun updateDownloadStatus(messageId: String, isDownloaded: Boolean, localPath: String?)
}
