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
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.text.style.TextOverflow


// 1. 定義目前支援的選單類型
enum class AttachmentType(val title: String) {
    MEDIA("Media"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    LOCATION("Location"),
    CONTACT("Contact")
}

// 音訊資料結構
data class AudioItem(
    val uri: Uri,
    val displayName: String,
    val duration: Long
)

data class AttachmentOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun MessageAttachmentBar(
    onImageSelected: (Uri) -> Unit,
    onAudioSelected: (Uri) -> Unit, // 🎯 新增：音訊選取回呼
    onCancel: () -> Unit,
    onDocumentClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // 🎯 2. 追蹤當前的選單狀態，預設為 MEDIA
    var currentType by remember { mutableStateOf(AttachmentType.MEDIA) }

    // 資料狀態
    val localImages = remember { mutableStateListOf<Uri>() }
    val localAudios = remember { mutableStateListOf<AudioItem>() }

    var refreshTrigger by remember { mutableStateOf(0) }

    // 下方功能按鈕清單
    val attachmentOptions = remember {
        listOf(
            AttachmentOption("圖片", Icons.Default.Image, { currentType = AttachmentType.MEDIA }),
            AttachmentOption("音訊", Icons.Default.Audiotrack, { currentType = AttachmentType.AUDIO }),
            AttachmentOption("文件", Icons.Default.Description, { currentType = AttachmentType.DOCUMENT; onDocumentClick() }),
            AttachmentOption("位置", Icons.Default.LocationOn, { currentType = AttachmentType.LOCATION; onLocationClick() }),
            AttachmentOption("聯絡人", Icons.Default.ContactPage, { currentType = AttachmentType.CONTACT; onContactClick() })
        )
    }

    // Android 14+ 圖片部分權限檢查
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
    ) { _ -> refreshTrigger++ }

    // 🎯 3. 讀取圖片的 Effect (保持原樣)
    LaunchedEffect(refreshTrigger) {
        val imageUris = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        try {
            context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                var count = 0
                while (cursor.moveToNext() && count < 24) {
                    val id = cursor.getLong(idColumn)
                    imageUris.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id))
                    count++
                }
            }
            localImages.clear()
            localImages.addAll(imageUris)
        } catch (e: SecurityException) {
            localImages.clear()
        }
    }

    // 🎯 4. 新增：讀取音訊的 Effect (記得在 AndroidManifest 宣告 READ_EXTERNAL_STORAGE 或 READ_MEDIA_AUDIO)
    LaunchedEffect(refreshTrigger) {
        val audioItems = mutableListOf<AudioItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        try {
            context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "未知音訊"
                    val duration = cursor.getLong(durationColumn)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    audioItems.add(AudioItem(uri, name, duration))
                    count++
                }
            }
            localAudios.clear()
            localAudios.addAll(audioItems)
        } catch (e: SecurityException) {
            localAudios.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 背景
        Box(modifier = Modifier.matchParentSize().blur(10.dp))
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 頂部列：顯示動態標題
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Send ${currentType.title}", // 🎯 動態標題
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .clip(CircleShape)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "返回輸入",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 中部列：使用 Crossfade 達成平滑切換內容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Crossfade(targetState = currentType, label = "ContentSwitch") { type ->
                    when (type) {
                        AttachmentType.MEDIA -> {
                            // 圖片網格邏輯 (維持原樣)
                            if (localImages.isEmpty() && !isPartialAccess) {
                                EmptyStateView("無最近媒體檔案")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isPartialAccess) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1f)
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

                                    items(localImages) { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "媒體預覽",
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onImageSelected(uri) },
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }

                        AttachmentType.AUDIO -> {
                            // 🎯 5. 新增：音訊渲染 UI (音訊檔名較長，改用垂直捲動清單比網格好看)
                            if (localAudios.isEmpty()) {
                                EmptyStateView("無最近音訊檔案")
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(localAudios) { audio ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .clickable { onAudioSelected(audio.uri) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Audiotrack,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = audio.displayName,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = formatDuration(audio.duration),
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 其他沒做完的狀態可以在這裡做對應的 Placeholder 處理
                        else -> {
                            EmptyStateView("${type.title} 功能尚未開放")
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

            // 下方選單 Icon 列
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(attachmentOptions) { option ->
                    // 🎯 6. 稍微優化：如果選中當前項目，給予特別的高亮白底
                    val isSelected = option.title == when(currentType) {
                        AttachmentType.MEDIA -> "圖片"
                        AttachmentType.AUDIO -> "音訊"
                        AttachmentType.DOCUMENT -> "文件"
                        AttachmentType.LOCATION -> "位置"
                        AttachmentType.CONTACT -> "聯絡人"
                    }

                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .clickable { option.onClick() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f)),
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

@Composable
fun EmptyStateView(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White.copy(alpha = 0.5f))
    }
}

// 輔助函式：將毫秒轉為 mm:ss 格式
fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}