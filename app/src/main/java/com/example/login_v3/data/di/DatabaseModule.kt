package com.example.login_v3.data.di

import android.content.Context
import androidx.room.Room
import com.example.login_v3.home.Message.ViewModel.Detail.MessageDao
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
            // .fallbackToDestructiveMigration() // 💡 提示：未來如果修改 Entity 欄位導致崩潰，可以先取消這行註解來清除舊資料庫
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        // 💡 告訴 Hilt：當有人要 MessageDao 時，就從 database 裡面把 messageDao() 拿出來提供給他！
        return database.messageDao()
    }
}