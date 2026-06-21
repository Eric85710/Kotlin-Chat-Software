package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
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
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        // 💡 控制下拉選單顯示/隱藏的狀態
        var menuExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(enabled = scale == 1f) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // 1. 底層：大圖檢視元件（保持不變）
            AsyncImage(
                model = imageUrl,
                contentDescription = "放大檢視圖片",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            val newOffset = if (newScale > 1f) offset + pan else Offset.Zero
                            scale = newScale
                            offset = newOffset
                        }
                    }
                    .clickable(enabled = false) { }
            )

            // 2. 上層：右上角「更多」按鈕與下拉選單容器
            // 透過 windowInsetsPadding 確保按鈕不會被手機的瀏海或狀態列擋住
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                // 包裹 IconButton 與 DropdownMenu 的小容器
                Box(modifier = Modifier.wrapContentSize()) {
                    IconButton(
                        onClick = { menuExpanded = true }, // 點擊開啟選單
                        modifier = Modifier.background(
                            color = Color.Black.copy(alpha = 0.4f), // 幫按鈕加個微黑底，避免遇到白圖時看不清
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多選項",
                            tint = Color.White
                        )
                    }

                    // Material 3 下拉選單
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false } // 點擊選單外部關閉
                    ) {
                        DropdownMenuItem(
                            text = { Text("儲存圖片") },
                            onClick = {
                                menuExpanded = false
                                // TODO: 實作下載圖片到相簿的邏輯
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = {
                                menuExpanded = false
                                // TODO: 實作系統分享 Intent 邏輯
                            }
                        )
                    }
                }
            }
        }
    }
}