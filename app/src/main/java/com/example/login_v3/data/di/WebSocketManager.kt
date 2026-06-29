package com.example.login_v3.data.di

import android.util.Log
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Singleton
class ChatWebSocketManager @Inject constructor(
    private val client: OkHttpClient // 從 NetworkModule 注入
) {
    private var webSocket: WebSocket? = null
    private var currentUrl: String? = null // 🎯 新增：用來記錄當前的連線網址

    // 用 SharedFlow 把收到的字串推出去給 Repository 監聽
    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    fun connect(url: String) {
        // 🎯 核心修正：檢查是否已經有全域連線存在
        // 我們去掉網址後面的 ?token=... 參數，只比對前半段的 Base URL (wss://tg.technologia-tw.com/api/ws)
        val newBaseUrl = url.split("?").firstOrNull()
        val currentBaseUrl = currentUrl?.split("?")?.firstOrNull()

        if (webSocket != null && newBaseUrl == currentBaseUrl) {
            Log.d("ChatWebSocketManager", "全域 WebSocket 已在連線中，跳過重複連線。")
            return
        }

        // 如果連線完全不存在，或者域名變了，才需要建立新連線（安全起見先清理舊連線）
        if (webSocket != null) {
            disconnect()
        }

        currentUrl = url

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatWebSocketManager", "WebSocket 連線成功！")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 收到伺服器即時訊息，丟進 Flow
                Log.d("ChatWebSocketManager", "收到 Socket 訊息: $text")
                _incomingMessages.tryEmit(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatWebSocketManager", "WebSocket 已關閉: $reason")
                clearConnection()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWebSocketManager", "WebSocket 連線失敗: ${t.message}, Code: ${response?.code}")
                clearConnection()
                // 💡 這裡未來可以實作計時器自動重連 (例如 delay 5秒後重新呼叫 connect)
            }
        })
    }

    // 進入房間時，通知後端我們要訂閱這個 Room 的即時訊息
    fun subscribeToRoom(roomId: String) {
        // 💡 提示：請根據你與後端約定的 JSON 格式去修改這一行。
        // 這裡假設後端接收的格式是 {"action":"subscribe","roomId":"xxxx"}
        val subscribeJson = "{\"action\":\"subscribe\",\"roomId\":\"$roomId\"}"

        val isSent = webSocket?.send(subscribeJson) ?: false
        if (isSent) {
            Log.d("ChatWebSocketManager", "已成功發送訂閱房間訊息: $roomId")
        } else {
            Log.e("ChatWebSocketManager", "發送訂閱房間失敗，Socket 可能未連線")
        }
    }

    fun disconnect() {
        // 1000 代表正常關閉
        webSocket?.close(1000, "User exit")
        clearConnection()
    }

    // 🎯 提取出來的清理專用方法
    private fun clearConnection() {
        webSocket = null
        currentUrl = null
    }
}