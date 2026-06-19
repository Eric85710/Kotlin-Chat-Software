package com.example.login_v3.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.login_v3.home.Tg_MarketPlace
import com.example.login_v3.home.Message.UI.Tg_Message
import com.example.login_v3.home.Tg_Server
import com.example.login_v3.home.setting.Tg_Setting
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel


import androidx.hilt.navigation.compose.hiltViewModel // 確保有引入 hiltViewModel
import com.example.login_v3.navigation.components.AppWallpaperBackground

@Composable
fun MainScreen_tab(
    // 注入 Theme_ViewModel 來讀取壁紙狀態
    themeViewModel: Theme_ViewModel = hiltViewModel()
) {
    val screensViewModel: ScreensViewModel = viewModel()
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    // show bottom bar or not
    val showBottomBar by bottomBarViewModel.showBottomBar.collectAsState()
    val navHeight = 64.dp
    val gap = 40.dp
    val bottomBarHeight = navHeight + gap

    // 使用剛剛做好的壁紙元件包在最外層
    AppWallpaperBackground(viewModel = themeViewModel) {
        Scaffold(
            containerColor = Color.Transparent, // 必須保持透明，底層的壁紙才看得到
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBarAnimated(
                    visible = showBottomBar,
                    height = bottomBarHeight,
                    content = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(navHeight)
                            ) {
                                nav_Wheel_display_block(screensViewModel = screensViewModel)
                            }
                            Spacer(modifier = Modifier.height(gap))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Screens_NavGraph(
                paddingValues = innerPadding,
                screensViewModel = screensViewModel,
                bottomBarViewModel = bottomBarViewModel
            )
        }
    }
}

@Composable
fun BottomBarAnimated(
    visible: Boolean,
    height: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    // 位移與透明度動畫
    val offsetY by animateDpAsState(targetValue = if (visible) 0.dp else height)
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .offset(y = offsetY)
            .graphicsLayer { this.alpha = alpha }
            .background(Color.Transparent),
        content = content
    )
}

@Composable
fun Screens_NavGraph(
    paddingValues: PaddingValues,
    screensViewModel: ScreensViewModel,
    bottomBarViewModel: BottomBarViewModel,
) {
    //nav_wheel_selecting tab
    val selectedScreen by screensViewModel.selected.collectAsState()


    when (selectedScreen) {
        Screen.Message -> Tg_Message()
        Screen.Server -> Tg_Server(viewModel = viewModel())
        Screen.MarketPlace -> Tg_MarketPlace()
        Screen.Setting -> Tg_Setting(
            bottomBarViewModel = bottomBarViewModel
        )
    }
}


