package com.example.login_v3.data.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.login_v3.home.Message.ViewModel.Detail.MessageDao
import com.example.login_v3.home.Message.ViewModel.Detail.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}