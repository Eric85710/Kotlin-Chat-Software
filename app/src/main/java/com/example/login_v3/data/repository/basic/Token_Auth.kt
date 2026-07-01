package com.example.login_v3.data.repository.basic

import android.util.Log
import com.example.login_v3.data.api.RefreshRequest
import com.example.login_v3.data.api.TecnologiaApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: dagger.Lazy<TecnologiaApi>
) : Authenticator {

    companion object {
        // 🌟 關鍵：全域共用同一把鎖，防止多個 API 同時過期時重複打刷新介面
        private val refreshMutex = Mutex()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("AuthDebug", "--- 進入 Authenticator (觸發網址: ${response.request.url}) ---")

        // 使用 runBlocking 配合 Mutex，讓同時間進來的 401 請求排隊
        return runBlocking {
            refreshMutex.withLock {

                // 1. 檢查目前 DataStore 裡的 Access Token
                val currentTokenInStore = tokenManager.currentAccessToken.first()
                val oldRequestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                // 🌟 防禦機制：如果 DataStore 裡的 Token 已經跟當初發起請求時的舊 Token 不同了
                // 代表排在你前面的請求已經幫忙刷新成功了！這裡直接拿新 Token 重試即可，不用再打一次 API。
                if (currentTokenInStore != oldRequestToken && !currentTokenInStore.isNullOrEmpty()) {
                    Log.d("AuthDebug", "分流防禦：偵測到其他請求已刷新過 Token，直接重複利用新 Token")
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentTokenInStore")
                        .build()
                }

                // 2. 真正準備去打後端刷新
                val refreshToken = tokenManager.currentRefreshToken.first()
                val currentUserId = tokenManager.currentUserId.first()
                Log.d("AuthDebug", "當前用戶: $currentUserId, Token是否存在: ${!refreshToken.isNullOrEmpty()}")

                if (refreshToken.isNullOrEmpty() || currentUserId.isNullOrEmpty()) {
                    return@runBlocking null
                }

                // 3. 同步發送網路請求
                val refreshResponse = try {
                    apiService.get().refreshToken(RefreshRequest(refreshToken))
                } catch (e: Exception) {
                    Log.e("AuthDebug", "打刷新 API 發生網路異常: ${e.message}")
                    null
                }

                // 4. 判斷刷新是否成功
                if (refreshResponse != null && refreshResponse.isSuccessful) {
                    val newAuthData = refreshResponse.body()
                    if (newAuthData != null) {
                        Log.d("AuthDebug", "✅ Token 刷新成功，準備寫入 DataStore")

                        // 導正順序：確實執行完 saveAuthData
                        tokenManager.saveAuthData(
                            userId = currentUserId,
                            accessToken = newAuthData.access_token,
                            refreshToken = newAuthData.refresh_token,
                            expiresInSec = newAuthData.expires_in
                        )

                        Log.d("AuthDebug", "✅ DataStore 寫入完成，重新發送原請求")

                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${newAuthData.access_token}")
                            .build()
                    }
                }

                // 5. 失敗：連 Refresh Token 都失效，強制登出
                Log.e("AuthDebug", "❌ Refresh Token 已失效，執行強制登出！")
                tokenManager.logout(currentUserId)
                return@runBlocking null
            }
        }
    }
}