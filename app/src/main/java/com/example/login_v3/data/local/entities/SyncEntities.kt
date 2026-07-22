package com.example.login_v3.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.login_v3.data.api.api_class.LastMessage
import com.example.login_v3.data.api.api_class.Partner

@Entity(tableName = "friends")
data class FriendLocalEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: String,
    val presenceStatus: String
)

@Entity(tableName = "rooms")
data class RoomLocalEntity(
    @PrimaryKey val roomId: String,
    val nickname: String?,
    val isHidden: Boolean,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val unreadCount: Int,
    val mentionCount: Int,
    val partner: Partner?,
    val lastMessage: LastMessage?
)

@Entity(tableName = "blocks")
data class BlockedUserLocalEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val blockedAt: String
)
