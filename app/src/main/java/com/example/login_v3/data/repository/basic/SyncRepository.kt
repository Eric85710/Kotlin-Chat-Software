package com.example.login_v3.data.repository.basic

import android.util.Log
import androidx.room.withTransaction
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.di.AppDatabase
import com.example.login_v3.data.local.UserPreferences.UserPreferencesRepository
import com.example.login_v3.data.local.entities.BlockedUserLocalEntity
import com.example.login_v3.data.local.entities.FriendLocalEntity
import com.example.login_v3.data.local.entities.RoomLocalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val api: TecnologiaApi,
    private val db: AppDatabase,
    private val userPrefs: UserPreferencesRepository
) {
    private val friendDao = db.friendDao()
    private val roomDao = db.roomLocalDao()
    private val blockDao = db.blockedUserDao()
    
    // 🔒 使用 Mutex 確保同一時間只有一個同步任務在執行，避免重複請求
    private val syncMutex = Mutex()

    suspend fun performSync(): Result<Unit> {
        // 如果已經有同步在跑，直接回傳成功（避免重複排隊）
        if (syncMutex.isLocked) {
            Log.d("SyncDebug", "⏳ [Sync] 偵測到已有同步任務正在進行，跳過本次請求")
            return Result.success(Unit)
        }

        return syncMutex.withLock {
            try {
                val since = userPrefs.syncSinceFlow.first()
                Log.d("SyncDebug", "🚀 [Sync] 開始同步 | since: $since")

                val response = api.sync(since)

                if (response.isSuccessful) {
                    val body = response.body() ?: return Result.failure(Exception("Sync body is null"))

                    Log.d("SyncDebug", "✅ [Sync] API 成功 | FullSnapshot: ${body.fullSnapshot} | nextSince: ${body.nextSince}")
                    Log.d("SyncDebug", "📦 [Sync] 收到: Blocks(${body.blocks.size}), Friends(${body.friends.size}), Rooms(${body.rooms.size})")

                    db.withTransaction {
                        if (body.fullSnapshot) {
                            Log.w("SyncDebug", "⚠️ [Sync] 收到 FullSnapshot，正在清空本地舊資料...")
                            friendDao.deleteAll()
                            roomDao.deleteAll()
                            blockDao.deleteAll()
                        }

                        // 1. 處理黑名單
                        body.blocks.forEach { block ->
                            if (block.removed) {
                                blockDao.deleteById(block.userId)
                            } else {
                                blockDao.insertOrUpdate(listOf(
                                    BlockedUserLocalEntity(
                                        userId = block.userId,
                                        username = block.username,
                                        displayName = block.displayName,
                                        avatarUrl = block.avatarUrl,
                                        blockedAt = block.blockedAt
                                    )
                                ))
                            }
                        }

                        // 2. 處理好友
                        body.friends.forEach { friend ->
                            if (friend.removed) {
                                friendDao.deleteById(friend.userId)
                            } else {
                                friendDao.insertOrUpdate(listOf(
                                    FriendLocalEntity(
                                        userId = friend.userId,
                                        username = friend.username,
                                        displayName = friend.displayName,
                                        avatarUrl = friend.avatarUrl,
                                        status = friend.status,
                                        presenceStatus = friend.presenceStatus
                                    )
                                ))
                            }
                        }

                        // 3. 處理聊天室
                        body.rooms.forEach { room ->
                            if (room.removed) {
                                roomDao.deleteById(room.roomId)
                            } else {
                                roomDao.insertOrUpdate(listOf(
                                    RoomLocalEntity(
                                        roomId = room.roomId,
                                        nickname = room.nickname,
                                        isHidden = room.isHidden,
                                        isMuted = room.isMuted,
                                        isPinned = room.isPinned,
                                        unreadCount = room.unreadCount,
                                        mentionCount = room.mentionCount,
                                        partner = room.partner,
                                        lastMessage = room.lastMessage
                                    )
                                ))
                            }
                        }

                        userPrefs.saveSyncSince(body.nextSince)
                        Log.d("SyncDebug", "💾 [Sync] 同步完成，已更新 nextSince 為 ${body.nextSince}")
                    }
                    Result.success(Unit)
                } else {
                    Log.e("SyncDebug", "❌ [Sync] API 失敗 | Code: ${response.code()}")
                    Result.failure(Exception("Sync API failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("SyncDebug", "❌ [Sync] 發生異常", e)
                Result.failure(e)
            }
        }
    }

    /**
     * 🛠️ 調試專用：重置同步狀態並清空本地資料
     * 呼叫此方法後，下一次同步將會從 since=0 開始抓取完整資料
     */
    suspend fun resetSyncAndClearData() {
        Log.w("SyncDebug", "🧨 [Debug] 正在重置同步狀態並清空資料庫...")
        db.withTransaction {
            friendDao.deleteAll()
            roomDao.deleteAll()
            blockDao.deleteAll()
            userPrefs.saveSyncSince(0L)
        }
        Log.d("SyncDebug", "✅ [Debug] 重置完成")
    }
}
