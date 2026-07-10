package com.example.login_v3.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.navigation.NavHostController
import com.example.login_v3.navigation.components.AppWallpaperBackground

@Composable
fun MainScreen_tab(
    navController: NavHostController,
    // 注入 Theme_ViewModel 來讀取壁紙狀態
    themeViewModel: Theme_ViewModel = hiltViewModel()
) {
    val screensViewModel: ScreensViewModel = viewModel()
    val bottomBarViewModel: BottomBarViewModel = hiltViewModel()

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
    // 🎯 高性能優化：使用單一 Transition 管理所有狀態，確保動畫同步
    val transition = updateTransition(targetState = visible, label = "BottomBarTransition")

    // 使用彈簧動畫 (Spring) 達成更流暢且自然的物理回饋感
    val progress by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                // 彈出時帶有一點點的回彈感 (LowBouncy)
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
            } else {
                // 隱藏時則較為快速乾脆
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            }
        },
        label = "progress"
    ) { state -> if (state) 1f else 0f }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 250) },
        label = "alpha"
    ) { state -> if (state) 1f else 0f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height * progress) // 只有高度變化會觸發 Layout (為了回收空間)
            .graphicsLayer { this.alpha = alpha }
            .background(Color.Transparent)
    ) {
        // 🚀 關鍵優化：內部內容使用 translationY，這是 GPU 加速的位移，不會觸發 Layout Pass
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    translationY = (height.toPx() * (1f - progress))
                }
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
    val showBottomBar by bottomBarViewModel.showBottomBar.collectAsState()

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
        userScrollEnabled = showBottomBar    // Allow swiping between main tabs
    ) { page ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            when (mainScreens[page]) {
                Screen.Message -> Tg_Message(bottomBarViewModel = bottomBarViewModel)
                Screen.Server -> Tg_Server(viewModel = viewModel())
                Screen.MarketPlace -> Tg_MarketPlace()
                Screen.Setting -> Tg_Setting(bottomBarViewModel = bottomBarViewModel)
            }
        }
    }
}


