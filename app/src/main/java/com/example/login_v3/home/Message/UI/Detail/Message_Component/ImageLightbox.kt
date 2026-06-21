package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun ImageLightbox(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        // 1. 宣告手勢狀態
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                // 💡 如果圖片處於放大狀態，點擊背景不觸發關閉，方便使用者操作；縮回原大小時點擊背景才關閉
                .clickable(enabled = scale == 1f) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "放大檢視圖片",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    // 2. 透過 graphicsLayer 將手勢狀態反映到畫面上
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    // 3. 監聽雙指與單指手勢
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // 計算新的縮放值，並限制在 1倍 到 4倍 之間
                            val newScale = (scale * zoom).coerceIn(1f, 4f)

                            // 計算新的位移值（只有在放大狀態下才允許平移拖曳）
                            val newOffset = if (newScale > 1f) {
                                offset + pan
                            } else {
                                Offset.Zero // 縮回 1 倍時自動歸零
                            }

                            scale = newScale
                            offset = newOffset
                        }
                    }
                    // 阻斷點擊圖片本身會關閉 Dialog 的行為
                    .clickable(enabled = false) { }
            )
        }
    }
}