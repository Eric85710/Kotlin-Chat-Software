package com.example.login_v3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.navigation.AppNavGraph
import com.example.login_v3.ui.theme.Login_V3Theme


@Composable
fun Technologia() {

    val navController = rememberNavController()
    val isLoggedIn = true // 從 ViewModel 也可以


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        AppNavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            paddingValues = padding
        )

    }
}

