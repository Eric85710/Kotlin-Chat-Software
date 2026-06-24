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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MessageAttachmentBar(
    onImageSelected: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val localImages = remember { mutableStateListOf<Uri>() }
    var refreshTrigger by remember { mutableStateOf(0) }

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
                while (cursor.moveToNext() && count < 20) {
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
                    text = "傳送媒體檔案",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "返回輸入",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // 中部列：放大後的媒體檢視器
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp), // 🎯 高度從 70dp 顯著提升到 110dp，預覽更大更清晰
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Android 14+ 管理選取範圍按鈕
                if (isPartialAccess) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp) // 同步放大為正方形
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

                if (localImages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth().height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "無最近媒體檔案", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    items(localImages) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "媒體預覽",
                            modifier = Modifier
                                .size(110.dp) // 🎯 同步放大
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageSelected(uri) },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            // 💡 提示：這裡可以作為未來的第三列空間，比如放置「文件、位置、名片」等其他 Icon 按鈕。
        }
    }
}