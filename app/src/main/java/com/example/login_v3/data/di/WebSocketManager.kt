package com.example.login_v3.data.di

import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Singleton
class ChatWebSocketManager @Inject constructor(
    private val client: OkHttpClient // 可以從 Hilt 模組注入
) {
    private var webSocket: WebSocket? = null

    // 用 SharedFlow 把收到的字串推出去給 Repository 監聽
    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    fun connect(url: String) {
        if (webSocket != null) return // 避免重複連線

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // 收到伺服器即時訊息，丟進 Flow
                _incomingMessages.tryEmit(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@ChatWebSocketManager.webSocket = null
                // 這裡可以寫自動重連邏輯
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@ChatWebSocketManager.webSocket = null
                // 處理連線失敗
            }
        })
    }

    // 進入房間時，通知後端我們要訂閱這個 Room 的即時訊息（看後端協議決定要不要傳這個）
    fun subscribeToRoom(roomId: String) {
        // 例如送出 JSON: { "action": "subscribe", "roomId": "xxx" }
        webSocket?.send("{\"action\":\"subscribe\",\"roomId\":\"$roomId\"}")
    }

    fun disconnect() {
        webSocket?.close(1000, "User exit")
        webSocket = null
    }
}