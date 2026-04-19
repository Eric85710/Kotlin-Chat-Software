package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.TecnologiaApi
import com.example.login_v3.data.api.api_class.ApiResponse
import com.example.login_v3.data.api.api_class.FriendRequest
import com.example.login_v3.data.api.api_class.LoginRequest
import com.example.login_v3.data.api.api_class.LoginResponse
import com.example.login_v3.data.api.api_class.User
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TecnologiaRepositoryImpl @Inject constructor(
    private val api: TecnologiaApi // Hilt 會從 NetworkModule 找這個 API
) : ITecnologiaRepository {

    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMe(token: String): Result<User> {
        return try {
            val response = api.getMe("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(token: String, friendId: String): Result<ApiResponse> {
        return try {
            val response = api.sendFriendRequest("Bearer $token", FriendRequest(friendId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTecnologiaRepository(
        repositoryImpl: TecnologiaRepositoryImpl // 這裡要對應實作類別的名稱
    ): ITecnologiaRepository
}

interface ITecnologiaRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun getMe(token: String): Result<User>
    suspend fun sendFriendRequest(token: String, friendId: String): Result<ApiResponse>
}