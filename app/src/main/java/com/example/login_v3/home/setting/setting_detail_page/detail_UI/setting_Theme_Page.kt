package com.example.login_v3.home.setting.setting_detail_page.detail_UI

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel
import com.example.login_v3.ui.theme.AppTheme
import java.io.File

@Composable
fun setting_theme_page(
    viewModel: Theme_ViewModel
){
    val context = LocalContext.current

    // 1. 監聽狀態
    val selectedTheme by viewModel.currentTheme.collectAsState()
    val wallpaperPath by viewModel.customWallpaperPath.collectAsState()

    // 2. 建立相簿照片選擇器 Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // 用戶選完照片後，若不為 null 則交給 ViewModel 儲存
        uri?.let { viewModel.uploadWallpaper(context, it) }
    }

    Column() {

        // theme mode
        Box(
            modifier = Modifier
                .padding(10.dp)
        ) {
            //glass effect
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(10.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            )


            //dark light mode
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "主題設定",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 遍歷所有 Enum 選項
                AppTheme.entries.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == selectedTheme),
                                onClick = { viewModel.updateTheme(theme) }
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = (theme == selectedTheme),
                            onClick = { viewModel.updateTheme(theme) }
                        )
                        Text(
                            text = when(theme) {
                                AppTheme.SYSTEM -> "follow"
                                AppTheme.LIGHT -> "light"
                                AppTheme.DARK -> "dark"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }


        // 3. 新增：upload wallpaper 區塊
        Box(
            modifier = Modifier
                .padding(10.dp)
        ) {
            // glass effect (與上方卡片一致)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(10.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            )

            // 卡片內元件
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "自訂背景壁紙",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左側：圖片預覽框（手機螢幕比例 9:16 微縮版）
                    Box(
                        modifier = Modifier
                            .size(90.dp, 160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val file = wallpaperPath?.let { File(it) }
                        if (file != null && file.exists()) {
                            // 讀取本地檔案並顯示
                            AsyncImage(
                                model = file,
                                contentDescription = "Wallpaper Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 沒上傳過，顯示預設文字
                            Text(
                                text = "未設定",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // 右側：操作按鈕們
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 選擇按鈕
                        Button(
                            onClick = {
                                // 啟動相簿選取器
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("選擇圖片")
                        }

                        // 刪除按鈕：有壁紙時才動畫浮現
                        AnimatedVisibility(visible = !wallpaperPath.isNullOrEmpty()) {
                            Button(
                                onClick = { viewModel.deleteWallpaper(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("清除壁紙")
                            }
                        }
                    }
                }
            }
        }
    }
}