package com.example.login_v3.home.Message.UI.Detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.Friend
import com.example.login_v3.data.api.api_class.PendingFriendApiModel
import com.example.login_v3.data.api.api_class.UserDetail
import com.example.login_v3.data.api.api_class.fullFriendsAvatarUrl
import com.example.login_v3.data.api.api_class.fullPendingAvatarUrl
import com.example.login_v3.home.Message.ViewModel.Detail.ContactListViewModel
import com.example.login_v3.navigation.BottomBarViewModel
import androidx.compose.ui.text.input.ImeAction
import com.example.login_v3.data.api.api_class.fullSearchedAvatarUrl


@Composable
fun Message_contact_list(
    navController: NavController,
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    bottomBarViewModel: BottomBarViewModel
) {
    // 統一觀察 uiState 即可，避免觀察多個 Flow 導致狀態不同步
    val uiState by contactListViewModel.uiState.collectAsStateWithLifecycle()
    // 判斷目前是否處於「搜尋模式」
    val friends = uiState.friends // 直接從 uiState 裡面取

    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose { bottomBarViewModel.setVisible(true) }
    }

    // 使用單一 LazyColumn 處理所有內容
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 頂部間距
        item { Spacer(modifier = Modifier.height(60.dp)) }

        // --- 搜尋框 ---
        item {
            UserSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { contactListViewModel.onSearchQueryChanged(it) },
                onSearch = { contactListViewModel.performSearch() },
                isLoading = uiState.isLoading
            )
        }

        //is Searching or not
        if (uiState.isSearching) {

            // --- 搜尋結果區塊 (僅在有結果時顯示) ---
            if (uiState.searchResults.isNotEmpty()) {
                item {
                    Text(
                        "搜尋結果",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(uiState.searchResults) { user ->
                    // 這裡你可以自定義搜尋結果的 Row
                    SearchResultRow(
                        user = user,
                        onAddFriend = { contactListViewModel.sendFriendRequest(user.id) }
                    )
                }
            }


        //if not searching then display friends list
        }else {
            // --- 區塊一：待處理好友 (只有在有資料時才顯示) ---
            if (uiState.pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        "好友請求 (${uiState.pendingRequests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
                items(uiState.pendingRequests) { request ->
                    PendingFriendRow(
                        request,
                        isLoading = uiState.isLoading,
                        onAccept = { _ ->
                            request.fromUserId?.let { id -> contactListViewModel.acceptFriend(id) }
                        },
                        onReject = { _ ->
                            request.fromUserId?.let { id -> contactListViewModel.rejectFriend(id) }
                        }
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }
            }

            // --- 區塊二：正式好友標題 ---
            item {
                Text(
                    "所有好友",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            }

            // --- 區塊三：正式好友列表內容 ---
            if (uiState.friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.5f) // 佔據剩餘空間的一部分
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("目前沒有好友")
                        }
                    }
                }
            } else {
                items(uiState.friends) { friends ->
                    FriendRow(friends)
                }
            }
        }




    }
}


//friends list
@Composable
fun FriendRow(
    friend: Friend,
    onClick: () -> Unit = {} // 建議加入點擊事件，例如跳轉聊天室
) {
    // 定義狀態顏色
    val statusColor = when (friend.status?.lowercase()) {
        "online" -> Color(0xFF4CAF50) // 鮮豔的綠色
        "busy" -> Color(0xFFF44336)   // 紅色
        else -> Color(0xFF9E9E9E)     // 灰色 (offline/away)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp) // 外間距
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), // 圓角
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary // 使用 Surface 顏色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // 內邊距
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 大頭貼 (左側)
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(friend.fullFriendsAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar of ${friend.displayName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp) // 稍微加大一點
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape), // 加入細邊框
                    placeholder = painterResource(id = R.drawable.avatar_v1),
                    error = painterResource(id = R.drawable.avatar_v1)
                )

                // 狀態圓點疊加在頭像右下角
                Surface(
                    color = statusColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(14.dp)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape) // 白色外圈
                ) {}
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 文字資訊 (中間)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.displayName ?: "未知用戶",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "@${friend.username ?: "unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val isOnline = friend.status?.lowercase() == "online"
            // 定義圓點顏色
            val dotColor = if (isOnline) {
                Color(0xFF4CAF50) // 綠色 (Online)
            } else {
                Color(0xFF9E9E9E) // 灰色 (Offline)
            }

            // 實作圓點
            Box(
                modifier = Modifier
                    .size(10.dp) // 圓點大小
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
fun PendingFriendRow(
    request: PendingFriendApiModel,
    isLoading: Boolean,
    onAccept: (String) -> Unit, // 增加回呼
    onReject: (String) -> Unit  // 增加回呼
) {
    ListItem(
        headlineContent = { Text("${request.displayName}") },
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
                IconButton(
                    onClick = { request.friendshipId?.let { onReject(it) } },
                    enabled = !isLoading // 正在載入時禁用
                ) {
                    Icon(Icons.Default.Close, contentDescription = "拒絕", tint = Color.Red)
                }
                IconButton(
                    onClick = { request.friendshipId?.let { onAccept(it) } },
                    enabled = !isLoading // 正在載入時禁用
                ) {
                    Icon(Icons.Default.Check, contentDescription = "接受", tint = Color.Green)
                }
            }
        }
    )
}


//search user
@Composable
fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("搜尋使用者 ID 或名稱...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(12.dp)
    )
}
@Composable
fun SearchResultRow(
    user: UserDetail,
    onAddFriend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 這裡放頭像
            AsyncImage(
                model = user.fullSearchedAvatarUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(user.displayName, style = MaterialTheme.typography.bodyLarge)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall)
            }

            // 判斷是否顯示勾勾
            if (user.isRequestSent) {
                // 已發送狀態：綠色勾勾，且按鈕不具備點擊功能
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已發送",
                    tint = Color(0xFF4CAF50), // 綠色
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                // 尚未發送：加好友按鈕
                IconButton(onClick = onAddFriend) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "加好友"
                    )
                }
            }
        }
    }
}