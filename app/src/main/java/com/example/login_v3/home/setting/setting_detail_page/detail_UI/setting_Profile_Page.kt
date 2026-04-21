package com.example.login_v3.home.setting.setting_detail_page.detail_UI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login_v3.R

@Composable
fun setting_profile_page(){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        val avatarSize = 80.dp
        val avatarOffset = avatarSize / 2 + 20.dp


        Box() {
            //glass effect
            Box(modifier = Modifier.matchParentSize().blur(10.dp))
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            )

            Column() {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(272.dp)
                ){

                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ){
                        // 1. 封面圖 (背景)
                        Image(
                            painter = painterResource(id = R.drawable.thumbnail_v1),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(y = -10.dp)
                        ,
                        verticalAlignment = Alignment.CenterVertically

                    ){
                        Spacer(modifier = Modifier.width(36.dp))
                        //user avatar
                        Image(
                            painter = painterResource(id = R.drawable.avatar_v1), // 把 JPG 放在 res/drawable
                            contentDescription = "avatar",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                            ,
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(text = "Eric Chen",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 14.dp)
                        )
                    }
                }

            }
        }


        Spacer(modifier = Modifier.height(20.dp))

        //user detail
        Box(){
            //glass effect
            Box(modifier = Modifier.matchParentSize().blur(10.dp))
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            )

            //intro
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // mutual friends
                Text(text = "258 mutual friends",
                    fontSize = 20.sp,
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //mutual server
                Text(text = "25 servers",
                    fontSize = 20.sp,
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //gender
                Text(
                    text = "male",
                    fontSize = 22.sp
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //location
                Text(
                    text = "Taiwan",
                    fontSize = 22.sp
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //intro
                Text(
                    text = "Founder of JFF studios，Software Dev Guy，3D Artist",
                    fontSize = 16.sp
                )
            }
        }
    }
}