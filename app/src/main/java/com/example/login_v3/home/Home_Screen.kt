package com.example.login_v3.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry

@Composable
fun HomeScreen(onLogout: () -> Unit, paddingValues: PaddingValues) {
    Button(onClick = onLogout) {
        Text("Logout")
    }
}
