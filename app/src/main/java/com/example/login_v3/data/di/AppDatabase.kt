package com.example.login_v3.data.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.login_v3.data.local.dao.BlockedUserDao
import com.example.login_v3.data.local.dao.FriendDao
import com.example.login_v3.data.local.dao.MessageDao
import com.example.login_v3.data.local.dao.RoomLocalDao
import com.example.login_v3.data.local.entities.BlockedUserLocalEntity
import com.example.login_v3.data.local.entities.FriendLocalEntity
import com.example.login_v3.data.local.entities.MessageEntity
import com.example.login_v3.data.local.entities.RoomLocalEntity

@Database(
    entities = [
        MessageEntity::class,
        FriendLocalEntity::class,
        RoomLocalEntity::class,
        BlockedUserLocalEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun friendDao(): FriendDao
    abstract fun roomLocalDao(): RoomLocalDao
    abstract fun blockedUserDao(): BlockedUserDao
}
