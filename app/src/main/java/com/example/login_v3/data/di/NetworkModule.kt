package com.example.login_v3.data.di

import androidx.lifecycle.ViewModel
import com.example.login_v3.data.api.TecnologiaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTecnologiaApi(): TecnologiaApi { // 補上函數宣告
        return Retrofit.Builder()
            .baseUrl("https://192.168.0.217:8443") // 記得最後面通常要有斜槓 /
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(TecnologiaApi::class.java) // 這裡要對應你的 Api 介面類別
    }
}