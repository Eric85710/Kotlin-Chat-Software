package com.example.login_v3.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.ui.theme.light_orange
import com.example.login_v3.ui.theme.main_orange

@Composable
fun Login_or_Reg_page(
    paddingValues: PaddingValues,
    onLoginClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFDA7029),
                        Color(0xFF777777),
                        Color(0xFFB34800)
                    )
                )
            )
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Welcome",
            fontSize = 42.sp,      // 字體大小（像標題）
            fontWeight = FontWeight.Bold,
            color = Color.White
        )



        Spacer(modifier = Modifier.weight(1f))


        //login and reg button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {


            //login_button
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,  // 背景顏色
                    contentColor = Color.White      // 文字顏色
                ),
                onClick = onLoginClick
            ) {
                Text( text = "Login",
                    fontSize = 20.sp)
            }


            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(end = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = light_orange,  // 背景顏色
                    contentColor = Color.White      // 文字顏色
                ),
                onClick = {}
            ) {
                Text( text = "Register",
                    fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.weight(0.02f))



    }
}