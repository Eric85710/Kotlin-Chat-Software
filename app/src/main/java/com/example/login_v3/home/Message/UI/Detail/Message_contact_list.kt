package com.example.login_v3.home.Message.UI.Detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.api.api_class.fullFriendsAvatarUrl
import com.example.login_v3.data.api.api_class.fullPendingAvatarUrl
import com.example.login_v3.home.Message.ViewModel.Detail.ContactListViewModel
import com.example.login_v3.navigation.BottomBarViewModel

@Composable
fun Message_contact_list(
    navController: NavController,
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
){

    val uiState by contactListViewModel.uiState.collectAsStateWithLifecycle()
    val friends by contactListViewModel.friends.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose {
            bottomBarViewModel.setVisible(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        //pending friends list
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
                if (uiState.pendingRequests.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ) {
                        item {
                            Text(
                                "好友請求 (${uiState.pendingRequests.size})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(uiState.pendingRequests) { request ->
                            PendingFriendRow(request)
                        }
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                    }
                }

        }

        //friends list
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (friends.isEmpty()) {
                // 如果列表是空的，顯示提示（或載入指示器）
                Box(
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("目前沒有好友")
                }
            } else {
                // 顯示好友列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    item {
                        Text(
                            "所有好友",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (uiState.friends.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("目前沒有好友")
                            }
                        }
                    } else {
                        items(uiState.friends) { friend ->
                            FriendRow(friend)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FriendRow(friend: Friend) {
    ListItem(
        headlineContent = { Text(friend.displayName) },
        supportingContent = { Text("@${friend.username}") },
        leadingContent = {
            // 使用 Coil 載入頭像
            AsyncImage(
                model = friend.fullFriendsAvatarUrl,
                contentScale = ContentScale.Crop,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.avatar_v1),
                error = painterResource(id = R.drawable.avatar_v1)
            )
        },
        trailingContent = {
            // 簡單顯示狀態
            Text(
                text = friend.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (friend.status == "online") Color.Green else Color.Gray
            )
        }
    )
}

@Composable
fun PendingFriendRow(request: PendingFriendApiModel) {
    ListItem(
        headlineContent = { Text(request.displayName) },
        supportingContent = { Text("想加你為好友") },
        leadingContent = {
            AsyncImage(
                // 這裡可以使用我們之前寫的擴充函式 .toFullImageUrl()
                model = request.fullPendingAvatarUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape),
                error = painterResource(id = R.drawable.avatar_v1)
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = { /* TODO: 呼叫 ViewModel 拒絕 */ }) {
                    Icon(Icons.Default.Close, contentDescription = "拒絕", tint = Color.Red)
                }
                IconButton(onClick = { /* TODO: 呼叫 ViewModel 接受 */ }) {
                    Icon(Icons.Default.Check, contentDescription = "接受", tint = Color.Green)
                }
            }
        }
    )
}