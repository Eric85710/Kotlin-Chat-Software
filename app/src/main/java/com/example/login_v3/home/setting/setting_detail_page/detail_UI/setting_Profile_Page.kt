package com.example.login_v3.home.setting.setting_detail_page.detail_UI

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.login_v3.R
import com.example.login_v3.data.api.api_class.UserProfile
import com.example.login_v3.data.api.api_class.fullAvatarUrl
import com.example.login_v3.data.api.api_class.fullBannerUrl
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.PersonalProfileViewModel
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.ProfileUiState
import com.example.login_v3.ui.theme.main_orange


@Composable
fun Setting_profile_page(
    viewModel: PersonalProfileViewModel = hiltViewModel(),
    onNavigateToAccountSwitch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            CircularProgressIndicator() // 轉圈圈
        }
        is ProfileUiState.Success -> {
            // 顯示個人資料
            Loaded_setting_profile_page(
                profile = state.profile,
                onNavigateToAccountSwitch = onNavigateToAccountSwitch // 補上這行
            )
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



// 定義目前正在編輯的目標
sealed class EditTarget(val title: String) {
    object Bio : EditTarget("Edit Bio")
    object DisplayName : EditTarget("Edit Display Name")
}

@Composable
fun Loaded_setting_profile_page(
    profile: UserProfile,
    viewModel: PersonalProfileViewModel = hiltViewModel(),
    onNavigateToAccountSwitch: () -> Unit
){

    //bia info
    // 管理 Dialog 是否顯示，以及目前編輯的對象
    var activeEditTarget by remember { mutableStateOf<EditTarget?>(null) }
    var tempText by remember { mutableStateOf("") }


    //image chooser
    val context = LocalContext.current // 務必獲取 Context，uploadAvatar 需要它來解析 Uri
    //avatar
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia() // 這裡改掉
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }
    //banner
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia() // 這裡改掉
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadBanner(context, it) }
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
                        AsyncImage(
                            model = profile.fullBannerUrl,
                            contentDescription = "Avatar of ${profile.display_name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clickable {
                                    bannerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.thumbnail_v1)
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
                            model = profile.fullAvatarUrl,
                            contentDescription = "Avatar of ${profile.display_name}",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .clickable {
                                    // 假設你拿到了一個新網址
                                    avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ,
                            contentScale = ContentScale.Crop,
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

                //display name
                Row() {
                    Text(text = "${profile.display_name}",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit bio",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable{
                                tempText = profile.display_name ?: ""
                                activeEditTarget = EditTarget.DisplayName
                            }
                    )
                }

                Divider(
                    color = Color.White.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

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

                Row() {
                    // 這裡改用 profile.bio
                    Text(
                        text = if (profile.bio.isNullOrBlank()) "Add a bio..." else profile.bio,
                        color = if (profile.bio.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit bio",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                tempText = profile.bio ?: ""
                                activeEditTarget = EditTarget.Bio
                            }
                    )
                }

                // 彈出編輯視窗
                if (activeEditTarget != null) {
                    AlertDialog(
                        onDismissRequest = { activeEditTarget = null },
                        containerColor = Color.White,
                        title = { Text(activeEditTarget?.title ?: "") },
                        text = {
                            TextField(
                                value = tempText,
                                onValueChange = { tempText = it },
                                placeholder = { Text("Enter here...") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                // 根據當前目標呼叫不同的 ViewModel 函式
                                when (activeEditTarget) {
                                    is EditTarget.Bio -> viewModel.updateProfile(bio = tempText)
                                    is EditTarget.DisplayName -> viewModel.updateProfile(displayName = tempText)
                                    else -> {}
                                }
                                activeEditTarget = null // 關閉視窗
                            }) {
                                Text("Save", color = MaterialTheme.colorScheme.onBackground)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { activeEditTarget = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }
        }


        //switch account button
        Spacer(modifier = Modifier.height(20.dp)) // 將按鈕推到底部

        Button(
            onClick = onNavigateToAccountSwitch,
            modifier = Modifier
                .fillMaxWidth()
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ManageAccounts, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage & Switch Accounts")
        }
    }
}