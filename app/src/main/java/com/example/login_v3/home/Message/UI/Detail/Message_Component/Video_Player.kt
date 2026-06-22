package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoLightbox(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = true) {
        onDismiss()
    }

    val context = LocalContext.current

    // 初始化 ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // 自動播放
        }
    }

    // 當 Composable 銷毀時，一定要釋放播放器資源以免記憶體流失
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 全螢幕黑色背景黑燈箱
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onDismiss() }, // 點擊背景可以關閉
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true // 顯示預設的播放、暫停、進度條控制面板
                    // 點擊播放器本體時不要觸發外層的關閉事件
                    setOnClickListener { /* 阻止點擊事件穿透 */ }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}