package com.example.login_v3.home

import android.net.Uri
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.login_v3.home.setting.setting_detail_Screen
import com.example.login_v3.home.setting.setting_list_Screen
import com.example.login_v3.navigation.BottomBarViewModel
import com.example.login_v3.navigation.ScreensViewModel

@Composable
fun Tg_MarketPlace(){

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "marketplace")
    }
}