package com.example.login_v3.home.Message.UI.Detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.login_v3.navigation.BottomBarViewModel

@Composable
fun Scan_QRcode(
    navController: NavController,
    bottomBarViewModel: BottomBarViewModel
){
    Column(
        modifier = Modifier
            .fillMaxSize()
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scan_QRcode")
    }
}