package com.example.login_v3.home.setting

import android.net.Uri
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.login_v3.navigation.BottomBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tg_Setting(
    bottomBarViewModel: BottomBarViewModel
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SharedTransitionLayout() {

                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "setting_list"
                ){

                    //setting_list_page
                    composable("setting_list") {
                        setting_list_Screen(
                            //scaling animation effect
                            onItemClick = { title, iconKey ->
                                val encodedTitle = Uri.encode(title)
                                navController.navigate("setting_detail/$encodedTitle/$iconKey")
                            },
                            animatedVisibilityScope = this
                        )
                    }

                    //setting_detail_page
                    composable(
                        route = "setting_detail/{title}/{iconKey}",
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("iconKey") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        val iconKey = backStackEntry.arguments?.getString("iconKey") ?: ""
                        setting_detail_Screen(title = title,
                            settingIcon = iconKey,
                            animatedVisibilityScope = this,
                            bottomBarViewModel = bottomBarViewModel
                        )
                    }
                }
            }
        }
    }
}
