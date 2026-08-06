package com.example.login_v3.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel
import java.io.File

@Composable
fun AppWallpaperBackground(
    viewModel: Theme_ViewModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 監聽來自 DataStore 的壁紙路徑
    val wallpaperPath by viewModel.customWallpaperPath.collectAsState()

    // 檢查檔案是否真的存在
    val wallpaperFile = wallpaperPath?.let { File(it) }
    val hasCustomWallpaper = wallpaperFile != null && wallpaperFile.exists()

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCustomWallpaper) {
            // 1. 如果有自訂壁紙，使用 Coil 填滿背景
            AsyncImage(
                model = wallpaperFile,
                contentDescription = "App Custom Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // 裁切填滿
            )
        } else {
            // 2. 如果沒有，使用你原本的三色漸層
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
            )
        }

        // 3. 將原本的 Scaffold 或內容蓋在背景上方
        content()
    }
}