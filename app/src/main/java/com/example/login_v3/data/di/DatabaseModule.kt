package com.example.login_v3.data.di

import android.content.Context
import androidx.room.Room
import com.example.login_v3.data.local.dao.BlockedUserDao
import com.example.login_v3.data.local.dao.FriendDao
import com.example.login_v3.data.local.dao.MessageDao
import com.example.login_v3.data.local.dao.RoomLocalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 💡 讓整個 App 生命週期都能拿到
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context // 💡 自動注入 ApplicationContext
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tecnologia_chat_db" // 資料庫檔案名稱
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideFriendDao(database: AppDatabase): FriendDao {
        return database.friendDao()
    }

    @Provides
    @Singleton
    fun provideRoomLocalDao(database: AppDatabase): RoomLocalDao {
        return database.roomLocalDao()
    }

    @Provides
    @Singleton
    fun provideBlockedUserDao(database: AppDatabase): BlockedUserDao {
        return database.blockedUserDao()
    }
}
