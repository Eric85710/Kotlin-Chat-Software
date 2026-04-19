package com.example.login_v3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.navigation.AppNavGraph
import com.example.login_v3.navigation.AppViewModel
import com.example.login_v3.navigation.MainViewModel

@Composable
fun Technologia() {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->

        //在navController
        AppNavGraph(
            paddingValues = padding
        )

    }
}

