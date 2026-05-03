package com.example.login_v3.home.setting.setting_detail_page.detail_UI

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.UserProfile
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.PersonalProfileViewModel
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.ProfileUiState
import com.example.login_v3.ui.theme.main_orange


@Composable
fun Setting_profile_page(
    viewModel: PersonalProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            CircularProgressIndicator() // 轉圈圈
        }
        is ProfileUiState.Success -> {
            // 顯示個人資料
            Loaded_setting_profile_page(state.profile)
        }
        is ProfileUiState.Error -> {
            // 顯示錯誤訊息與重試按鈕
            Column {
                Text(text = state.message, color = Color.Red)
                Button(onClick = { viewModel.fetchProfile() }) {
                    Text("重試")
                }
            }
        }
    }
}


@Composable
fun Loaded_setting_profile_page(
    profile: UserProfile,
    viewModel: PersonalProfileViewModel = hiltViewModel()
){

    //bia info
    var showDialog by remember { mutableStateOf(false) }
    var tempBio by remember { mutableStateOf(profile.bio ?: "") }


    //image chooser
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // 2. 當使用者選完照片後，會回傳一個 Uri
        if (uri != null) {
            // 直接把本地 Uri 轉成字串丟進去 (僅限本地 Demo，重整 App 後會失效)
            viewModel.updateProfile(avatarUrl = uri.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        val avatarSize = 80.dp
        val avatarOffset = avatarSize / 2 + 20.dp


        Box() {
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
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
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
                        AsyncImage(
                            model = profile.avatar_url,
                            contentDescription = "Avatar of ${profile.display_name}",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .clickable {
                                    // 假設你拿到了一個新網址
                                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.avatar_v1),
                            error = painterResource(R.drawable.avatar_v1)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(text = "${profile.display_name}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 14.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

            }
        }


        Spacer(modifier = Modifier.height(20.dp))

        //user detail
        Box(){
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
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            )

            //intro
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // mutual friends
                Text(text = "258 mutual friends",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //mutual server
                Text(text = "25 servers",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //gender
                Text(
                    text = "male",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //location
                Text(
                    text = "Taiwan",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                //intro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable {
                            tempBio = profile.bio ?: "" // 開啟時同步目前的 bio
                            showDialog = true
                        }
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    // 這裡改用 profile.bio
                    Text(
                        text = if (profile.bio.isNullOrBlank()) "Add a bio..." else profile.bio,
                        color = if (profile.bio.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onBackground
                    )
                }

                // 彈出編輯視窗
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.secondary,
                        textContentColor = MaterialTheme.colorScheme.onBackground,
                        title = { Text("Edit Bio") },
                        text = {
                            TextField(
                                value = tempBio,
                                onValueChange = { tempBio = it },
                                placeholder = { Text("Enter your bio") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.updateProfile(bio = tempBio) // 呼叫你寫好的 API 邏輯
                                showDialog = false
                            }) {
                                Text("Save", color = MaterialTheme.colorScheme.onBackground)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    )
                }

            }
        }
    }
}