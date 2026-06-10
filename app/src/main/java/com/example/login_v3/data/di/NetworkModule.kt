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

    // 🟢 修正：在這裡注入 okHttpClient
    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.189/")
            .client(okHttpClient)   // 關鍵：將攔截器綁定到 Retrofit
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            // 🟢 修正 2：如果使用 IP 且噴出 Hostname 錯誤才需要加這段。
            // 如果後端憑證的 SAN/CN 已經有包含 192.168.0.217，這行可以刪除。
            .hostnameVerifier { hostname, _ -> hostname == "192.168.0.217" }
            .addInterceptor { chain ->
                // 1. 動態獲取「目前選中帳號」的 Token
                // 使用 first() 確保只取目前最新的那一個值
                val token = runBlocking { tokenManager.currentAccessToken.first() }

                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val response = chain.proceed(requestBuilder.build())

                // 2. 處理 Token 失效 (401)
                if (response.code == 401) {
                    runBlocking {
                        // 獲取目前是哪個 ID 失效了
                        val currentId = tokenManager.currentUserId.first()
                        currentId?.let {
                            // 只登出當前失效的帳號，不影響其他帳號
                            tokenManager.logout(it)
                        }
                    }
                }

                response
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideTecnologiaApi(retrofit: Retrofit): TecnologiaApi {
        return retrofit.create(TecnologiaApi::class.java)
    }
}
