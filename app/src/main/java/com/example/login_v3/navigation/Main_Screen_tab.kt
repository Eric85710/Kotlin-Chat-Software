package com.example.login_v3.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.navigation.components.AppWallpaperBackground

@Composable
fun MainScreen_tab(
    navController: NavHostController,
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // 移除內建 Insets 以達成真正全螢幕
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
                                nav_Wheel_display_block(
                                    navController = navController,
                                    screensViewModel = screensViewModel
                                )
                            }
                            Spacer(modifier = Modifier.height(gap))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Screens_NavGraph(
                navController = navController,
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
    // 🎯 核心修正：讓實體高度也參與動畫
    // 當 visible 為 false 時，這塊 Box 的高度會縮減到 0，從而讓 Scaffold 重新計算 innerPadding
    val animatedHeight by animateDpAsState(targetValue = if (visible) height else 0.dp)
    
    // 位移與透明度動畫保持不變
    val offsetY by animateDpAsState(targetValue = if (visible) 0.dp else height)
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight) // 關鍵：動態高度決定了內容區域下方的留白
            .graphicsLayer { this.alpha = alpha }
            .background(Color.Transparent)
    ) {
        // 內部的內容容器則維持原高度，並配合 offset 達成滑出效果
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = offsetY)
        ) {
            content()
        }
    }
}

@Composable
fun Screens_NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    screensViewModel: ScreensViewModel,
    bottomBarViewModel: BottomBarViewModel,
) {
    val mainScreens = remember { listOf(Screen.Message, Screen.Server, Screen.MarketPlace, Screen.Setting) }
    val pagerState = rememberPagerState(pageCount = { mainScreens.size })
    val selectedScreen by screensViewModel.selected.collectAsState()

    // 1. Sync ViewModel selection to Pager (Wheel -> Pager)
    LaunchedEffect(selectedScreen) {
        val targetPage = mainScreens.indexOf(selectedScreen)
        if (targetPage != -1 && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // 2. Sync Pager swipe to ViewModel (Pager -> Wheel)
    LaunchedEffect(pagerState.currentPage) {
        if (mainScreens[pagerState.currentPage] != screensViewModel.selected.value) {
            screensViewModel.select(mainScreens[pagerState.currentPage])
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1, // Pre-load adjacent screens for smoothness
        userScrollEnabled = true    // Allow swiping between main tabs
    ) { page ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            when (mainScreens[page]) {
                Screen.Message -> Tg_Message()
                Screen.Server -> Tg_Server(viewModel = viewModel())
                Screen.MarketPlace -> Tg_MarketPlace()
                Screen.Setting -> Tg_Setting(bottomBarViewModel = bottomBarViewModel)
            }
        }
    }
}


