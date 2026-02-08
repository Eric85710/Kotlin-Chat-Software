package com.example.login_v3.navigation

import android.R.attr.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.auth.LoginScreen
import com.example.login_v3.home.HomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    paddingValues: PaddingValues,
    isLoggedIn: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "home" else "login"
    ) {

        composable("login") {
            LoginScreen(
                paddingValues = paddingValues,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }


        composable("home") {
            HomeScreen(
                paddingValues = paddingValues,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }


    }
}
