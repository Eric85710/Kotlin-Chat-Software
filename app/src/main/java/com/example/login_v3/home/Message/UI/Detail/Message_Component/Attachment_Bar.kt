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

@Composable
fun MessageAttachmentBar(
    onImageSelected: (Uri) -> Unit, // 🎯 改為直接回傳選中的圖片 Uri
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val isPartialAccess by remember {
        derivedStateOf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        }
    }

    // 處理 Android 14 重新調整選取照片的 Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // 重新讀取或觸發相簿狀態更新（這裡會觸發 localImages 重新 query）
    }

    // 💡 非同步讀取手機內最新的數張圖片 Uri
    val localImages by remember {
        derivedStateOf {
            val imageUris = mutableListOf<Uri>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN
            )
            // 依時間倒序排序，讓最新的照片排在最前面
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                // 這裡限制讀取前 20 張，避免一次載入太多有效能隱憂
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    imageUris.add(contentUri)
                    count++
                }
            }
            imageUris
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 毛玻璃背景與邊框 (保持與 InputBar 一致)
        Box(modifier = Modifier.matchParentSize().blur(10.dp))
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🎯 橫向媒體檢視器
            LazyRow(
                modifier = Modifier.weight(1f).height(70.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🎯 核心優化：如果是 Android 14 部分允許，在最左邊固定顯示一個「編輯存取範圍」的按鈕
                if (isPartialAccess) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    // 再次要求 READ_MEDIA_IMAGES，系統會自動彈出「管理已選取的照片」視窗
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "管理照片", tint = Color.White)
                                Text("管理", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (localImages.isEmpty()) {
                    item {
                        Text(text = "無媒體檔案", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp))
                    }
                } else {
                    items(localImages) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "媒體預覽",
                            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageSelected(uri) },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 關閉/返回輸入框按鈕
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "返回輸入",
                    tint = Color.White
                )
            }
        }
    }
}