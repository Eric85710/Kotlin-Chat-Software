package com.example.login_v3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.login_v3.auth.HealthCheckScreen
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel
import com.example.login_v3.ui.theme.AppTheme
import com.example.login_v3.ui.theme.Login_V3Theme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: Theme_ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        installSplashScreen()
        setContent {

            //theme mode data
            val currentAppTheme by themeViewModel.currentTheme.collectAsState()

            Login_V3Theme(appTheme = currentAppTheme) {
                HealthCheckScreen()
            }
        }
    }
}
