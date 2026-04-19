package com.example.login_v3.navigation

import android.R.attr.padding
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.auth.LoginScreen
import com.example.login_v3.auth.LoginViewModel
import com.example.login_v3.auth.Login_or_Reg_page
import com.example.login_v3.auth.Register_Screen
import com.example.login_v3.home.HomeScreen
import com.example.login_v3.home.Tg_MarketPlace

@Composable
fun AppNavGraph(
    paddingValues: PaddingValues,
    appViewModel: AppViewModel = viewModel()
) {
    val currentScreen by appViewModel.currentScreen.collectAsState()

    when (currentScreen) {
        AppScreen.PreReg -> Login_or_Reg_page(
            paddingValues = paddingValues,
            onLoginClick = { appViewModel.goTo(AppScreen.Login) },
            onRegClick = { appViewModel.goTo(AppScreen.Register) }
        )

        AppScreen.Login -> LoginScreen(
            paddingValues = paddingValues,
            onLoginSuccess = { appViewModel.onLoginSuccess() }
        )

        AppScreen.Register -> {
            //back to pre_reg
            BackHandler {
                appViewModel.goTo(AppScreen.PreReg)
            }
            Register_Screen(paddingValues = paddingValues)
        }

        AppScreen.Home -> HomeScreen(
            paddingValues = paddingValues,
            onLogout = { appViewModel.onLogout() }
        )

        AppScreen.ScreensTab -> MainScreen_tab()
    }
}