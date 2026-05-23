package com.example.login_v3.home.Message.UI.Detail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.home.Message.ViewModel.UserStatus
import com.example.login_v3.navigation.BottomBarViewModel
import com.example.login_v3.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageMessaging(
    roomId: String,
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose { bottomBarViewModel.setVisible(true) }
    }

    // 根據目前的 uiState 來動態決定 TopBar 要顯示什麼文字
    val topBarTitle = when (val state = uiState) {
        is MessagesUiState.Success -> state.roomTitle
        is MessagesUiState.Error -> "載入失敗"
        is MessagesUiState.Loading -> "載入中..."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //avatar
                        if (uiState is MessagesUiState.Success) {
                            val successState = uiState as MessagesUiState.Success
                            UserAvatar(
                                avatarUrl = successState.partnerAvatarUrl,
                                modifier = Modifier.size(36.dp) // 適中的 TopBar 頭像大小
                            )
                        }

                        Text(text = topBarTitle)

                        // ✨ 如果是 Success 狀態，且對方是在線狀態（ONLINE），就顯示綠色小圓點
                        if (uiState is MessagesUiState.Success) {
                            val successState = uiState as MessagesUiState.Success
                            if (successState.partnerStatus == UserStatus.ONLINE) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = UserStatus.ONLINE.color,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFDA7029),
                            Color(0xFF777777),
                            Color(0xFFB34800)
                        )
                    )
                )
            ,
            contentAlignment = Alignment.Center
        ) {
            // 根據不同的狀態渲染主畫面內容
            when (val state = uiState) {
                is MessagesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is MessagesUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        Text("目前沒有新訊息")
                    } else {
                        MessageList(
                            messages = state.messages,
                            partnerAvatarUrl = state.partnerAvatarUrl,
                            partnerDisplayName = state.roomTitle
                        )
                    }
                }
                is MessagesUiState.Error -> {
                    Text(
                        text = "載入失敗：${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MessageList(
    partnerAvatarUrl: Any?,
    partnerDisplayName: String,
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    val currentUserId = "user_123"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            val isMe = message.senderId == currentUserId
            MessageRow(
                message = message,
                partnerAvatarUrl = partnerAvatarUrl,
                partnerDisplayName = partnerDisplayName,
                isMe = isMe
            )
        }
    }
}

@Composable
fun MessageRow(
    message: Message,
    isMe: Boolean,
    partnerAvatarUrl: Any? ,
    partnerDisplayName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        // ✨ 如果是對方發的訊息，在對話框左邊顯示頭像
        if (!isMe) {
            UserAvatar(
                avatarUrl = partnerAvatarUrl,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp) // 訊息旁的頭像稍微小一點
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMe) Color(0xFFDCF8C6) else Color(0xFFECEFF1),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (!isMe) {
                    // ✨ 這裡成功的將原本的 message.senderId 改成對方的 display_name 了！
                    Text(
                        text = partnerDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


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