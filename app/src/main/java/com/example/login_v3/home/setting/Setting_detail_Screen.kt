package com.example.login_v3.home.setting

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.home.setting.setting_detail_page.setting_devices_page
import com.example.login_v3.home.setting.setting_detail_page.setting_profile_page
import com.example.login_v3.home.setting.setting_detail_page.setting_subscription_page
import com.example.login_v3.home.setting.setting_detail_page.setting_theme_page
import com.example.login_v3.navigation.BottomBarViewModel

@Composable
fun SharedTransitionScope.setting_detail_Screen(
    title: String,
    settingIcon: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomBarViewModel: BottomBarViewModel
){
    //control bottom bar behavior
    DisposableEffect(Unit) {
        bottomBarViewModel.setVisible(false)
        onDispose {
            bottomBarViewModel.setVisible(true)
        }
    }

    val setting_icon_decode: ImageVector = when (settingIcon) {
        "profile" -> Icons.Default.Person
        "theme" -> Icons.Default.Palette
        "devices" -> Icons.Default.Devices
        "subscribetion" -> Icons.Default.Subscriptions
        else -> Icons.Default.Help
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {

        val iconState = rememberSharedContentState(key = "setting_icon_${settingIcon}")
        val titleState = rememberSharedContentState(key = "setting_title_${settingIcon}_${Uri.encode(title)}")

        Spacer(modifier = Modifier.height(46.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = setting_icon_decode,
                contentDescription = title,
                modifier = Modifier
                    .size(48.dp)
                    .sharedElement(
                        sharedContentState = iconState,
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 400)

                        }
                    ),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        //setting_detail_page_Navigation
        val setting_detail_page_navController = rememberNavController()
        val startRoute = when (settingIcon) {
            "profile" -> "setting_Profile"
            "theme" -> "setting_Theme"
            "devices" -> "setting_Devices"
            "subscribetion" -> "setting_Subscription"
            else -> "setting_Profile"
        }

        //spacing between content and title
        Spacer(modifier = Modifier.height(26.dp))

        NavHost(
            navController = setting_detail_page_navController,
            startDestination = startRoute
        ) {
            composable("setting_Profile") { setting_profile_page() }
            composable("setting_Theme") { setting_theme_page() }
            composable("setting_Devices") { setting_devices_page() }
            composable("setting_Subscription") { setting_subscription_page() }
        }
    }
}