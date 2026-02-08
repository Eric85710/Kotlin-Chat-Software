package com.example.login_v3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.ui.theme.Login_V3Theme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset.Companion.Unspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.ImageLoader
import coil.request.ImageRequest
import coil.compose.AsyncImage

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.login_v3.ui.theme.light_orange
import com.example.login_v3.ui.theme.main_orange


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Login_V3Theme {
                Box(
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
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent // 關鍵
                    ) { innerPadding ->
                        LoginScreen(paddingValues = innerPadding)
                    }
                }

            }
        }
    }
}








@Composable
fun SvgImage(
    resId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data("android.resource://${context.packageName}/$resId")
        .decoderFactory(SvgDecoder.Factory())
        .build()

    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = modifier,
        imageLoader = imageLoader
    )
}





@Composable
fun LoginScreen(paddingValues: PaddingValues){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {




        //Motion_logo
        SvgImage(
            resId = R.raw.tg_chat_v3,
            modifier = Modifier.size(180.dp)
        )





        //login Box
        Box(
            modifier = Modifier
                .size(360.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ){


            //glass effect
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp)) // 🔥 一定要有
                    .blur(
                        radius = 10.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
                    .background(
                        Color.White.copy(alpha = 0.18f)
                    )
                    .border( // ⭐ 玻璃邊框
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )






            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(text = "Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .padding(8.dp))

                Spacer(modifier = Modifier.height(16.dp))


                TextField(
                    value = email,
                    onValueChange = { email = it},
                    label = {
                        Text(
                            emailError.ifEmpty { "Email" },
                            color = if (emailError.isNotEmpty()) Red else androidx.compose.ui.graphics.Color.Unspecified
                        )
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AccountCircle,
                            contentDescription = ""
                        )
                    },
                    shape = RoundedCornerShape(8.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp),

                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Transparent,
                        unfocusedIndicatorColor = Transparent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))


                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            passwordError.ifEmpty { "Password" },
                            color = if (passwordError.isNotEmpty()) Red else androidx.compose.ui.graphics.Color.Unspecified
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Lock, contentDescription = "")
                    },

                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                    trailingIcon = {
                        val image = if (passwordVisible) {
                            Icons.Rounded.Visibility
                        } else {
                            Icons.Rounded.VisibilityOff
                        }

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },

                    shape = RoundedCornerShape(8.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp),

                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Transparent,
                        unfocusedIndicatorColor = Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = {
                        emailError = if(email.isBlank()) "Email is required" else ""
                        passwordError = if(password.isBlank()) "password is required" else ""
                        if (emailError.isEmpty() && passwordError.isEmpty()){
                            //login auth logic
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = main_orange,  // 背景顏色
                        contentColor = Color.White      // 文字顏色
                    )

                ) {
                    Text( text = "Login")
                }


                Spacer(modifier = Modifier.height(16.dp))

                Text( text = "forget password?",
                    color = light_orange,
                    modifier = Modifier
                        .clickable{}
                )
            }

        }

    }
}