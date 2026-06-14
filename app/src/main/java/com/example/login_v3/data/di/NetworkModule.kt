package com.example.login_v3.data.di

import androidx.lifecycle.ViewModel
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.repository.basic.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // 在 OkHttp 的背景執行緒中安全地進行同步等待
                val (token, userId) = runBlocking(Dispatchers.IO) {
                    val t = tokenManager.currentAccessToken.first()
                    val id = tokenManager.currentUserId.first()
                    t to id
                }

                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val response = chain.proceed(requestBuilder.build())

                // 處理 Token 失效 (401)
                if (response.code == 401) {
                    userId?.let { id ->
                        // 使用 Dispatchers.IO 確保不會卡死主執行緒
                        runBlocking(Dispatchers.IO) {
                            tokenManager.logout(id)
                        }
                    }
                }

                response
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // 修正：更換為新的 HTTPS 正式網域
            .baseUrl("https://tg.technologia-tw.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTecnologiaApi(retrofit: Retrofit): TecnologiaApi {
        return retrofit.create(TecnologiaApi::class.java)
    }
}