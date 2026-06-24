package com.example.login_v3.home.Message.UI.Detail.Message_Component

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage // 建議使用 Coil 來載入本地圖片 Uri，效能極佳
import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight


// 🎯 定義底部功能按鈕的資料結構
data class AttachmentOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun MessageAttachmentBar(
    onImageSelected: (Uri) -> Unit,
    onCancel: () -> Unit,
    onAudioClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val localImages = remember { mutableStateListOf<Uri>() }
    var refreshTrigger by remember { mutableStateOf(0) }

    // 🎯 1. 新增一個 State 來追蹤目前畫面上應該顯示什麼標題，預設為 "Attachment"
    var currentMenuTitle by remember { mutableStateOf("Attachment") }

    // attachment menu
    val attachmentOptions = remember {
        listOf(
            AttachmentOption("圖片", Icons.Default.Image, {
                currentMenuTitle = "Media" // 點擊圖片或重置時回到原來的標題
            }),
            AttachmentOption("音訊", Icons.Default.Audiotrack, {
                currentMenuTitle = "Audio"      // 🎯 變更標題
                onAudioClick()
            }),
            AttachmentOption("文件", Icons.Default.Description, {
                currentMenuTitle = "Document"   // 🎯 變更標題
                onDocumentClick()
            }),
            AttachmentOption("位置", Icons.Default.LocationOn, {
                currentMenuTitle = "Location"   // 🎯 變更標題
                onLocationClick()
            }),
            AttachmentOption("聯絡人", Icons.Default.ContactPage, {
                currentMenuTitle = "Contact"    // 🎯 變更標題
                onContactClick()
            })
        )
    }

    val isPartialAccess by remember(refreshTrigger) {
        derivedStateOf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshTrigger++
    }

    LaunchedEffect(refreshTrigger) {
        val imageUris = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                var count = 0
                while (cursor.moveToNext() && count < 24) { // 🎯 調整為 24 張
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    imageUris.add(contentUri)
                    count++
                }
            }
            localImages.clear()
            localImages.addAll(imageUris)
        } catch (e: SecurityException) {
            localImages.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 毛玻璃背景與邊框
        Box(modifier = Modifier.matchParentSize().blur(10.dp))
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) // 稍微加寬圓角迎合高面板
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        )

        // 🎯 核心修改：改為 Column 佈局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // 內邊距均勻分布
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 頂部列：左邊是標題或功能名稱，右邊是關閉按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Send $currentMenuTitle",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // 🎯 精緻化縮小後的關閉按鈕
                Box(
                    modifier = Modifier
                        .size(24.dp) // 1. 控制整個外圈圓形背景的大小
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .clip(CircleShape)
                        .clickable { onCancel() }, // 2. 點擊事件直接綁在圓圈上
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "返回輸入",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp) // 3. 控制內層叉叉 Icon 的尺寸
                    )
                }
            }

            // 中部列：放大後的媒體檢視器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // 🎯 顯著拉高 Attachment Bar 容器
            ) {
                if (localImages.isEmpty() && !isPartialAccess) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "無最近媒體檔案", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 🎯 固定 3 直欄網格（你也可以改成 4）
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Android 14+ 管理選取範圍按鈕 (在網格的第一格)
                        if (isPartialAccess) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f) // 確保正方形
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "管理照片", tint = Color.White)
                                        Text("管理檔案", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // 渲染相片列表
                        items(localImages) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "媒體預覽",
                                modifier = Modifier
                                    .aspectRatio(1f) // 🎯 強制圖片縮圖為完美的正方形
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onImageSelected(uri) },
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp) // 加一條細緻的分割線

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp), // 按鈕之間的間距
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(attachmentOptions) { option ->
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .clickable { option.onClick() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 圖示圓圈背景
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.title,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}