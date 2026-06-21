package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun ImageLightbox(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    // 使用 Dialog 才能真正做到全螢幕蓋住 TopBar 與 BottomBar
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // 關閉預設寬度限制，達到真正全螢幕
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)) // 質感黑半透明背景
                .clickable { onDismiss() }, // 點擊背景任何地方都能關閉
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "放大檢視圖片",
                contentScale = ContentScale.Fit, // 確保整張圖片能完整塞進螢幕
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f) // 留一點上下操作空間
                    .clickable(enabled = false) { } // 阻止點擊圖片本身時觸發關閉
            )
        }
    }
}