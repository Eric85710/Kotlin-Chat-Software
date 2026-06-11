package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.login_v3.R

@Composable
fun UserAvatar(
    avatarUrl: Any?, // ✨ 改成 Any?
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = avatarUrl ?: R.drawable.avatar_v1, // 如果為 null，就用預設圖
        contentDescription = "用戶頭像",
        modifier = modifier
            .background(Color.LightGray, shape = CircleShape)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}