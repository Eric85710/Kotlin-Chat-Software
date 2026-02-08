package com.example.login_v3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.login_v3.auth.LoginScreen
import com.example.login_v3.ui.theme.Login_V3Theme


@Composable
fun App() {
    Login_V3Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFDA7029),
                            Color(0xFF777777),
                            Color(0xFFB34800)
                        )
                    )
                )
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent
            ) { innerPadding ->
                LoginScreen(paddingValues = innerPadding)
            }
        }
    }
}






