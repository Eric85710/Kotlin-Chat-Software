package com.example.login_v3.home.Message.UI.Detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.login_v3.data.api.api_class.Message
import com.example.login_v3.home.Message.ViewModel.Detail.ChatViewModel
import com.example.login_v3.home.Message.ViewModel.Detail.MessagesUiState
import com.example.login_v3.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageMessaging(
    roomId: String,
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("聊天室 ($roomId)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("<")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is MessagesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is MessagesUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        Text("目前沒有新訊息")
                    } else {
                        MessageList(messages = state.messages)
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
    messages: List<Message>, //  這裡是正確的 Message
    modifier: Modifier = Modifier
) {
    val currentUserId = "user_123"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // items 內部拿到的 message 會依循 List<Message> 的型態
        items(messages) { message ->
            val isMe = message.senderId == currentUserId
            MessageRow(message = message, isMe = isMe)
        }
    }
}

@Composable
fun MessageRow(
    message: Message, // ✅ 修正這裡：將 Screen.Message 改為 Message
    isMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
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
                    Text(
                        text = message.senderId, // ✅ 這樣型態對了，就能抓到欄位了！
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = message.content, // ✅ 這樣型態對了，就能抓到欄位了！
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}