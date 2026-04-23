package com.example.login_v3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.login_v3.auth.HealthCheckScreen
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel
import com.example.login_v3.ui.theme.AppTheme
import com.example.login_v3.ui.theme.Login_V3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //color mode viewmodel
    private val themeViewModel by viewModels<Theme_ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        installSplashScreen()
        setContent {
            Login_V3Theme(appTheme = themeViewModel.currentTheme) {
                Technologia()
            }
        }
    }
}
