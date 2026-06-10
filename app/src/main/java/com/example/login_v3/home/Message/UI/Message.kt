package com.example.login_v3.home.Message.UI

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.login_v3.home.Message.UI.Detail.MessageMessaging
import com.example.login_v3.home.Message.UI.Detail.Message_add_contact
import com.example.login_v3.home.Message.UI.Detail.Message_contact_list
import com.example.login_v3.home.Message.UI.Detail.Scan_QRcode
import com.example.login_v3.navigation.BottomBarViewModel
import com.example.login_v3.home.Message.UI.Detail.Loaded_Tg_Message


sealed class Screen(val route: String) {
    object Messages : Screen("messages")
    object CreateContact : Screen("create_contact")
    object FriendsList : Screen("friends_list")
    object ScanQRcode : Screen("scan_QRcode")
    object MessageMessaging : Screen("message_messaging?roomId={roomId}") {
        fun createRoute(roomId: String): String {
            return "message_messaging?roomId=$roomId"
        }
    }
}

@Composable
fun Tg_Message(
    bottomBarViewModel: BottomBarViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    SharedTransitionLayout{
        NavHost(
            navController = navController,
            startDestination = Screen.Messages.route,
        ) {
            // 你的訊息主頁面
            // 訊息主頁面
            composable(Screen.Messages.route) {
                Loaded_Tg_Message(
                    navController = navController,
                    // 2. 傳入 sharedTransitionScope (this@SharedTransitionLayout)
                    sharedTransitionScope = this@SharedTransitionLayout,
                    // 3. 傳入 animatedVisibilityScope (this@composable)
                    animatedVisibilityScope = this@composable,
                    onRoomClick = { roomId, _ ->
                        val route = Screen.MessageMessaging.createRoute(roomId)
                        navController.navigate(route)
                    }
                )
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
                Scan_QRcode(
                    navController,
                    bottomBarViewModel = bottomBarViewModel
                )
            }

            //Message detail
            composable(
                route = Screen.MessageMessaging.route,
                arguments = listOf(
                    navArgument("roomId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()

                MessageMessaging(
                    roomId = roomId,
                    navController = navController,
                    // 4. 同樣把這兩個 Scope 傳入詳情頁
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBackClick = { navController.popBackStack() },
                    bottomBarViewModel = bottomBarViewModel
                )
            }

        }

    }
}