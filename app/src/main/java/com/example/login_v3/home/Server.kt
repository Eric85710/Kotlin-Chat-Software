package com.example.login_v3.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun Tg_Server(
    viewModel: ServerViewModel
){
    val serverList by viewModel.serverList.collectAsState()
    val currentServer by viewModel.currentServer.collectAsState()


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {

            //TOP_SPACER
            Spacer(
                modifier = Modifier.weight(0.6f)
            )


            Box(
                modifier = Modifier
                    .weight(8f)
                    .fillMaxWidth()
            ){
                //glass effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(10.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Color.Black.copy(alpha = 0.08f),
                            RoundedCornerShape(16.dp)
                        )
                )


                //row contain server list and function block
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    //server list
                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight()
                    ) {
                        LazyColumn {
                            items(serverList) { server ->
                                Image(
                                    painter = painterResource(id = server.server_icon),
                                    contentDescription = server.name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            viewModel.selectServer(server.id)
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }

                    }


                    //server function block
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {

                        Box(modifier = Modifier
                            .fillMaxSize()
                        ){
                            //glass effect for server function_block
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(16.dp)
                                    )
                            )


                            //column contain function block
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Spacer(modifier = Modifier.height(10.dp))

                                //server name
                                Text(
                                    text = if (currentServer != null) {
                                        "目前伺服器：${currentServer!!.name}"
                                    } else {
                                        "尚未選擇伺服器"
                                    },
                                    fontSize = 20.sp,              // 調整字體大小
                                    fontWeight = FontWeight.Bold, // 設定字體加粗
                                    color = Color(0xFFFFEDD9)
                                )


                                Divider(
                                    color = Color(0xFFFFEDD9),       // 線的顏色
                                    thickness = 2.dp,         // 線的粗細
                                    modifier = Modifier.padding(vertical = 8.dp) // 與上下內容的間距
                                )

                                //gallery
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(horizontal = 28.dp, vertical = 16.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFFEDD9), // 漸層起始色
                                                    Color(0xFFAB47BC)  // 漸層結束色
                                                ),
                                                start = Offset(0f, 0f),
                                                end = Offset(1000f, 1000f) // 控制漸層方向
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(24.dp)
                                ) {}

                            }
                        }
                    }

                }
            }

            //bottom spacer
            Spacer(
                modifier = Modifier.weight(1.4f)
            )

        }
    }
}