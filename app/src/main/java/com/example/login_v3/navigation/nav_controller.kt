package com.example.login_v3.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.login_v3.auth.login.LoginScreen
import com.example.login_v3.auth.Login_or_Reg_page
import com.example.login_v3.auth.SwitchAccount.AccountSwitchScreen
import com.example.login_v3.auth.SwitchAccount.AddAccountScreen
import com.example.login_v3.auth.reg.RegisterScreen
import com.example.login_v3.auth.reg.Register_Screen
import com.example.login_v3.home.HomeScreen

@Composable
fun AppNavGraph(
    paddingValues: PaddingValues,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val currentScreen by appViewModel.currentScreen.collectAsState()

    when (currentScreen) {

        AppScreen.Loading -> {
            // 顯示一個簡單的載入圈圈
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        AppScreen.PreReg -> Login_or_Reg_page(
            paddingValues = paddingValues,
            onLoginClick = { appViewModel.goTo(AppScreen.Login) },
            onRegClick = { appViewModel.goTo(AppScreen.Register) }
        )

        AppScreen.Login -> LoginScreen(
            paddingValues = paddingValues
        )

        AppScreen.AddAccount -> {
            BackHandler {
                appViewModel.goTo(AppScreen.ScreensTab)
            }
            AddAccountScreen(
                paddingValues = paddingValues,
                onBack = { appViewModel.goTo(AppScreen.ScreensTab) }
            )
        }

        AppScreen.Register -> {
            //back to pre_reg
            BackHandler {
                appViewModel.goTo(AppScreen.PreReg)
            }
            Register_Screen(
                paddingValues = paddingValues,
                onRegisterSuccess = {
                    // 🟢 註冊成功後，切換到登入畫面
                    appViewModel.goTo(AppScreen.Login)
                }
            )
        }

        AppScreen.Home -> HomeScreen(
            paddingValues = paddingValues,
            onLogout = { appViewModel.onLogout() }
        )

        AppScreen.ScreensTab -> MainScreen_tab()

        AppScreen.AccountSwitch -> {
            BackHandler {
                appViewModel.goTo(AppScreen.ScreensTab)
            }
            AccountSwitchScreen(
                // 點擊「登入另一個帳號」就跳轉到 Login 頁面
                onAddAccountClick = { appViewModel.goTo(AppScreen.AddAccount) },
                // 點擊返回則回到原本的主分頁 (ScreensTab)
                onBack = { appViewModel.goTo(AppScreen.ScreensTab) }
            )
        }
    }
}