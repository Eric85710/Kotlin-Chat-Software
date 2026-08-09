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
        val url = response.request.url
        Log.d("AuthDebug", "--- 進入 Authenticator (觸發網址: $url) ---")

        // 🌟 防禦機制：如果刷新 Token 的請求本身又回傳了 401，代表 Refresh Token 已徹底過期
        // 必須直接回傳 null，否則會進入無窮迴圈（甚至死鎖，因為 Mutex 不可重入）
        if (url.encodedPath.contains("api/auth/refresh")) {
            Log.e("AuthDebug", "❌ 刷新 Token 的請求本身又回傳了 401！停止重試。")
            return null
        }

        // 使用 runBlocking 配合 Mutex，讓同時間進來的 401 請求排隊
        return runBlocking {
            refreshMutex.withLock {

                // 1. 檢查目前 DataStore 裡的 Access Token
                val currentTokenInStore = tokenManager.currentAccessToken.first()
                val oldRequestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                Log.d("AuthDebug", "DataStore中的Token: ${currentTokenInStore?.take(10)}...")
                Log.d("AuthDebug", "原始請求的Token: ${oldRequestToken?.take(10)}...")

                // 🌟 防禦機制：如果 DataStore 裡的 Token 已經跟當初發起請求時的舊 Token 不同了
                // 代表排在你前面的請求已經幫忙刷新成功了！這裡直接拿新 Token 重試即可，不用再打一次 API。
                if (currentTokenInStore != oldRequestToken && !currentTokenInStore.isNullOrEmpty()) {
                    Log.d("AuthDebug", "分流防禦：偵測到其他請求已刷新過 Token，直接重複利用新 Token")
                    val requestBuilder = response.request.newBuilder()
                    requestBuilder.header("Authorization", "Bearer $currentTokenInStore")
                    
                    // 針對 WebSocket 更新 URL 中的 token 參數
                    if (response.request.url.queryParameter("token") != null) {
                        val newUrl = response.request.url.newBuilder()
                            .setQueryParameter("token", currentTokenInStore)
                            .build()
                        requestBuilder.url(newUrl)
                    }
                    return@runBlocking requestBuilder.build()
                }

                // 2. 真正準備去打後端刷新
                val refreshToken = tokenManager.currentRefreshToken.first()
                val currentUserId = tokenManager.currentUserId.first()
                Log.d("AuthDebug", "當前用戶: $currentUserId, Token是否存在: ${!refreshToken.isNullOrEmpty()}")

                if (refreshToken.isNullOrEmpty() || currentUserId.isNullOrEmpty()) {
                    return@runBlocking null
                }

                // 3. 同步發送網路請求
                Log.d("AuthDebug", "正在向伺服器請求刷新 Token... RefreshToken: ${refreshToken.take(10)}...")
                val refreshResponse = try {
                    apiService.get().refreshToken(RefreshRequest(refreshToken))
                } catch (e: Exception) {
                    Log.e("AuthDebug", "打刷新 API 發生網路異常: ${e.message}", e)
                    null
                }

                // 4. 判斷刷新是否成功
                if (refreshResponse != null) {
                    Log.d("AuthDebug", "刷新回應碼: ${refreshResponse.code()}, 是否成功: ${refreshResponse.isSuccessful}")
                    if (refreshResponse.isSuccessful) {
                        val newAuthData = refreshResponse.body()
                        val newAccessToken = newAuthData?.access_token ?: newAuthData?.token

                        if (!newAccessToken.isNullOrEmpty()) {
                            Log.d("AuthDebug", "✅ Token 刷新成功，新 AccessToken: ${newAccessToken.take(10)}...")

                            // 如果後端沒給新的 refresh_token，就沿用舊的
                            val nextRefreshToken = if (!newAuthData?.refresh_token.isNullOrEmpty()) {
                                newAuthData?.refresh_token!!
                            } else {
                                refreshToken
                            }

                            tokenManager.saveAuthData(
                                userId = currentUserId,
                                accessToken = newAccessToken,
                                refreshToken = nextRefreshToken,
                                expiresInSec = newAuthData?.expires_in ?: 3600
                            )

                            Log.d("AuthDebug", "✅ DataStore 寫入完成，重新發送原請求")

                            // 修正：同步更新 Header 與 URL 中的 token 參數 (針對 WebSocket)
                            val requestBuilder = response.request.newBuilder()
                            requestBuilder.header("Authorization", "Bearer $newAccessToken")
                            if (response.request.url.queryParameter("token") != null) {
                                val newUrl = response.request.url.newBuilder()
                                    .setQueryParameter("token", newAccessToken)
                                    .build()
                                requestBuilder.url(newUrl)
                            }
                            return@runBlocking requestBuilder.build()
                        } else {
                            Log.e("AuthDebug", "❌ 刷新成功但無法取得新 Token (Body: $newAuthData)")
                        }
                    } else {
                        Log.e("AuthDebug", "❌ 刷新失敗，錯誤代碼: ${refreshResponse.code()}, 錯誤內容: ${refreshResponse.errorBody()?.string()}")
                    }
                } else {
                    Log.e("AuthDebug", "❌ refreshResponse 為 null (網路異常)")
                }

                // 5. 失敗：連 Refresh Token 都失效，強制登出
                Log.e("AuthDebug", "❌ Refresh Token 已失效，執行強制登出！")
                tokenManager.logout(currentUserId)
                return@runBlocking null
            }
        }
    }
}