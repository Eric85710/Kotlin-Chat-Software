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
            .baseUrl("http://192.168.0.217/")
            .client(okHttpClient) // 🔥 關鍵：將攔截器綁定到 Retrofit
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // 連線逾時
            .readTimeout(15, TimeUnit.SECONDS)    // 讀取逾時
            .addInterceptor { chain ->
                val token = runBlocking { tokenManager.accessToken.first() }
                val requestBuilder = chain.request().newBuilder()

                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val response = chain.proceed(requestBuilder.build())

                if (response.code == 401) {
                    runBlocking { tokenManager.clearAuthData() }
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
