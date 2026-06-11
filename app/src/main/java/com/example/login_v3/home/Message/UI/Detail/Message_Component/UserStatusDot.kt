package com.example.login_v3.home.Message.UI.Detail.Message_Component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.login_v3.home.Message.ViewModel.UserStatus

@Composable
fun UserStatusDot(
    status: UserStatus,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp // 預設 14.dp，但也允許外部微調大小
) {
    if (status != UserStatus.UNKNOWN) {
        Box(
            modifier = modifier
                .size(size)
                .background(status.color, CircleShape)  // 圓形背景
                .border(2.dp, Color.White, CircleShape) // 白色外圈邊線
        )
    }
}