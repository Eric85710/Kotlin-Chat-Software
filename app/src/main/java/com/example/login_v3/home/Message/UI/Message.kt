package com.example.login_v3.home.Message.UI

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.login_v3.data.api.api_class.ChatRoom
import com.example.login_v3.data.api.api_class.fullContactAvatarUrl
import com.example.login_v3.home.Message.UI.Detail.Message_add_contact
import com.example.login_v3.home.Message.UI.Detail.Message_contact_list
import com.example.login_v3.home.Message.UI.Detail.Scan_QRcode
import com.example.login_v3.home.Message.ViewModel.ChatRoomsViewModel
import com.example.login_v3.navigation.BottomBarViewModel


sealed class Screen(val route: String) {
    object Messages : Screen("messages")
    object CreateContact : Screen("create_contact")
    object FriendsList : Screen("friends_list")
    object ScanQRcode : Screen("scan_QRcode")
}

@Composable
fun Tg_Message(
    bottomBarViewModel: BottomBarViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Messages.route,
        // 進入時的動畫（新頁面出現）
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        },
        // 退出時的動畫（舊頁面消失）
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        },
        // 按返回鍵進入時的動畫
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        },
        // 按返回鍵退出時的動畫
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        }
    ) {
        // 你的訊息主頁面
        composable(Screen.Messages.route) {
            Loaded_Tg_Message(navController)
        }

        // 新建聯絡頁面
        composable(Screen.CreateContact.route) {
            Message_add_contact(navController)
        }

        // 好友列表頁面
        composable(Screen.FriendsList.route) {
            Message_contact_list(
                navController,
                bottomBarViewModel = bottomBarViewModel
            )
        }

        //QR code
        composable(Screen.ScanQRcode.route) {
            Scan_QRcode(navController)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Loaded_Tg_Message(
    navController: NavController,
    viewModel: ChatRoomsViewModel = hiltViewModel(),
    onRoomClick: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    // 觀察 ViewModel 的 StateFlow
    val rooms by viewModel.roomsState.collectAsState()

    // 進入頁面時主動讀取一次
    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }


    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFDA7029),
                        Color(0xFF777777),
                        Color(0xFFB34800)
                    )
                )
            ),
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,   // ⭐ 改這裡
                    titleContentColor = Color.Black
                ),
                actions = {
                    // 2. 使用 Box 包裹按鈕與選單，確保選單彈出位置正確
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Add, contentDescription = "更多選項")
                        }

                        // 3. 下拉選單組件
                        DropdownMenu(
                            expanded = expanded,
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            onDismissRequest = { expanded = false },
                            shadowElevation = 0.dp,
                            modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.3f))
                        ) {

                            val itemColors = MenuDefaults.itemColors(
                                textColor = Color.White,
                                leadingIconColor = Color(0xFFDA7029) // 使用你背景的橘色作為點綴
                            )

                            DropdownMenuItem(
                                text = { Text("新建聯絡") },
                                onClick = {
                                    expanded = false
                                    navController.navigate(Screen.CreateContact.route)
                                },
                                colors = itemColors,
                                leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("好友列表") },
                                onClick = {
                                    expanded = false
                                    navController.navigate(Screen.FriendsList.route)
                                },
                                colors = itemColors,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("QR code") },
                                onClick = {
                                    expanded = false
                                    navController.navigate(Screen.ScanQRcode.route)
                                },
                                colors = itemColors,
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary, // ⭐ 調低透明度
                        shape = RoundedCornerShape(0.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (rooms.isEmpty()) {
                // 顯示空狀態或 Loading (這部分可根據需求擴充)
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("目前沒有聊天室")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(
                        items = rooms,
                        key = { it.roomId } // 增加效能，防止列表閃爍
                    ) { room ->
                        RoomItem(
                            room = room,
                            onClick = onRoomClick
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

        }
    }
}



@Composable
fun RoomItem(
    room: ChatRoom,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp) // 增加外邊距，讓 Card 之間有呼吸感
            .clickable { onClick(room.roomId) },
        shape = RoundedCornerShape(16.dp), // 較大的圓角看起來更現代
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // ✅ 透明背景
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // 增加輕微陰影
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            //glass effect
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(10.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp),
            ) {
                // 1. 頭像
                val imageUrl = room.roomIconUrl ?: room.partner?.fullContactAvatarUrl
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp) // 稍微加大一點
                        .clip(CircleShape)
                        .border(1.dp, Color.White, CircleShape) // 頭像加個細白邊
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 2. 中間內容
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = room.roomName.orEmpty().ifEmpty {
                            room.partner?.displayName ?: "Unknown"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val lastMsgText = when {
                        room.lastMessage?.isDeleted == true -> "訊息已刪除"
                        room.lastMessage?.content != null -> room.lastMessage.content
                        room.lastMessage?.attachment != null -> "[檔案] ${room.lastMessage.attachment.filename}"
                        else -> "尚未有訊息"
                    }

                    Row() {
                        Text(
                            text = lastMsgText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (room.unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFDA7029), // 使用你的主橘色
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (room.unreadCount > 99) "99+" else room.unreadCount.toString(),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))
                }

                // 3. 右側：時間與未讀計數
                Column(
                    verticalArrangement = Arrangement.Center
                ) {

                    val partner = room.partner // 先從 room 取得 partner
                    if (partner != null) {
                        val status = UserStatus.fromString(partner.status)
                        if (status != UserStatus.UNKNOWN) {
                            Canvas(
                                modifier = Modifier
                                    .size(14.dp)
                                    // 注意：在 Column 中 Alignment.BottomEnd 可能不適用
                                    // 如果你要讓點跟在文字旁邊，建議用 Box 或是調整 Alignment
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                drawCircle(color = status.color)
                            }
                        }
                    }
                }
            }


        }
    }
}

enum class UserStatus(val color: Color) {
    ONLINE(Color.Green),
    AWAY(Color.Yellow),
    OFFLINE(Color.Gray),
    UNKNOWN(Color.DarkGray);

    companion object {
        fun fromString(status: String?): UserStatus {
            return when (status?.lowercase()) {
                "online" -> ONLINE
                "away" -> AWAY
                "offline" -> OFFLINE
                else -> UNKNOWN
            }
        }
    }
}