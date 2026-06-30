package com.example.login_v3.data.repository.basic

import com.example.login_v3.data.api.RefreshRequest
import com.example.login_v3.data.api.TecnologiaApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import okhttp3.Response
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    // 💡 關鍵：使用 dagger.Lazy 避免 Hilt 的循環依賴 (Circular Dependency)
    private val apiService: dagger.Lazy<TecnologiaApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. 取得 DataStore 中的當前帳號資訊
        val refreshToken = runBlocking { tokenManager.currentRefreshToken.first() }
        val currentUserId = runBlocking { tokenManager.currentUserId.first() }

        if (refreshToken.isNullOrEmpty() || currentUserId.isNullOrEmpty()) {
            return null // 沒有憑證資訊，不重試，直接拋出原 401 錯誤
        }

        // 2. 同步發送網路請求，拿 refresh_token 去換新 Token
        val refreshResponse = runBlocking {
            try {
                // 用 apiService.get() 來取得真正需要用的實例
                apiService.get().refreshToken(RefreshRequest(refreshToken))
            } catch (e: Exception) {
                null
            }
        }

        // 3. 判斷刷新是否成功
        if (refreshResponse != null && refreshResponse.isSuccessful) {
            val newAuthData = refreshResponse.body()
            if (newAuthData != null) {

                // 4. 成功：更新本地 DataStore 資料
                runBlocking {
                    tokenManager.saveAuthData(
                        userId = currentUserId,
                        accessToken = newAuthData.access_token,
                        refreshToken = newAuthData.refresh_token,
                        expiresInSec = newAuthData.expires_in
                    )
                }

                // 5. 成功：用新的 Access Token 重新建構剛才失敗的 API 請求
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${newAuthData.access_token}")
                    .build()
            }
        }

        // 6. 失敗：如果連 Refresh Token 都失效（例如過期被後端拒絕），則強制登出，導回登入頁
        runBlocking {
            tokenManager.logout(currentUserId)
            // 這裡可以透過 EventBus、SharedFlow 或 LiveData 貼出全域通知，要求 UI 跳轉到 LoginActivity/LoginScreen
        }

        return null
    }
}