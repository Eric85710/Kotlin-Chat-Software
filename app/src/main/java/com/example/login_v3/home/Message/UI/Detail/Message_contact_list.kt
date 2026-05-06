package com.example.login_v3.home.Message.UI.Detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.home.Message.ViewModel.Detail.ContactListViewModel
import com.example.login_v3.navigation.BottomBarViewModel

@Composable
fun Message_contact_list(
    navController: NavController,
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
){
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
                items(friends) { friend ->
                    FriendRow(friend)
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
                model = friend.avatarUrl,
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