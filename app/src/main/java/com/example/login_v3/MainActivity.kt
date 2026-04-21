package com.example.login_v3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.login_v3.auth.HealthCheckScreen
import com.example.login_v3.ui.theme.AppTheme
import com.example.login_v3.ui.theme.Login_V3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        installSplashScreen()
        setContent {
            Login_V3Theme(appTheme = AppTheme.DARK) {
                Technologia()
            }
        }
    }
}
