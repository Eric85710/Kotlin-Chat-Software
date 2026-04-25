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
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            // 關鍵：一定要以 / 結尾
            .baseUrl("http://192.168.0.217/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTecnologiaApi(retrofit: Retrofit): TecnologiaApi {
        // 直接使用上面提供的 retrofit 實體
        return retrofit.create(TecnologiaApi::class.java)
    }
}
