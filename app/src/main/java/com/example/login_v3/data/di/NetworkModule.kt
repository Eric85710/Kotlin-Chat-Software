package com.example.login_v3.data.di

import androidx.lifecycle.ViewModel
import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.repository.basic.TokenAuthenticator
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
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

    // TokenInterceptor
    @Provides
    @Singleton
    fun provideTokenInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath

            // 如果是認證相關的 API，直接放行，不附加 Token Header
            if (path.contains("api/auth/login") ||
                path.contains("api/auth/register") ||
                path.contains("api/auth/refresh") ||
                path.contains("api/health")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            // 在背景執行緒中安全地獲取當前 Token
            val token = runBlocking(Dispatchers.IO) {
                tokenManager.currentAccessToken.firstOrNull()
            }

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            // ⚠️ 注意：這裡不再手動處理 401 logout，純粹放行交給 Authenticator 處理
            chain.proceed(requestBuilder.build())
        }
    }

    // 2. 配置 OkHttpClient，把 Interceptor 和 Authenticator 綁進去
    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenInterceptor: Interceptor,
        tokenAuthenticator: TokenAuthenticator // 👈 注入前面實作好的 Authenticator
    ): OkHttpClient {
        val loggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            android.util.Log.d("ChatDebug", "🚀 [Network] Request: ${request.method} ${request.url}")
            val response = chain.proceed(request)
            android.util.Log.d("ChatDebug", "📦 [Network] Response: ${response.code} for ${request.url}")
            response
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)   // 負責 Log 網路請求
            .addInterceptor(tokenInterceptor)     // 負責幫一般請求加上 Token
            .authenticator(tokenAuthenticator)   // 負責在 401 時自動刷新 Token
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