package com.example.login_v3.navigation

import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Message : Screen("message", label = "Message")
    object Server : Screen("server", label = "Server")
    object MarketPlace : Screen("marketplace", label = "MarketPlace")
    object Setting : Screen("setting", label = "Setting")
}