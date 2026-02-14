package com.example.login_v3.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun Register_Screen(
    paddingValues: PaddingValues
){
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Reg",
            fontSize = 30.sp
        )
    }

}