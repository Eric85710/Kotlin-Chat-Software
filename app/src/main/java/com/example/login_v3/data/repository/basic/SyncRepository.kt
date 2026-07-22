package com.example.login_v3.data.repository.basic

import androidx.room.withTransaction
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.di.AppDatabase
import com.example.login_v3.data.local.UserPreferences.UserPreferencesRepository
import com.example.login_v3.data.local.entities.BlockedUserLocalEntity
import com.example.login_v3.data.local.entities.FriendLocalEntity
import com.example.login_v3.data.local.entities.RoomLocalEntity
import kotlinx.coroutines.flow.first
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

    suspend fun performSync(): Result<Unit> {
        return try {
            val since = userPrefs.syncSinceFlow.first()
            val response = api.sync(since)

            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Sync body is null"))

                db.withTransaction {
                    if (body.fullSnapshot) {
                        friendDao.deleteAll()
                        roomDao.deleteAll()
                        blockDao.deleteAll()
                    }

                    // Process Blocks
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

                    // Process Friends
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

                    // Process Rooms
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
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync API failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
