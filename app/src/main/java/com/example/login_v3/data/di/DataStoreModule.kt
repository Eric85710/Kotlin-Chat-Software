package com.example.login_v3.data.di

import android.content.Context
import androidx.datastore.core.DataStore
// 修正 1: 確保匯入的是 androidx 的 Preferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.login_v3.data.local.UserPreferences.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 定義擴充屬性，這裡保持為檔案私有 (private) 是沒問題的
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
// 修正 2: 移除 private，讓 Hilt 可以訪問這個模組
object DataStoreModule {

    //UserPreferencesRepository
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context.dataStore)
    }
}