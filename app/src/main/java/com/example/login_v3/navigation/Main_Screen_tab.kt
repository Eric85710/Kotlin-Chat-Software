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
import com.example.login_v3.home.Message.ViewModel.MessageViewModel
import com.example.login_v3.home.Tg_MarketPlace
import com.example.login_v3.home.Message.UI.Tg_Message
import com.example.login_v3.home.Tg_Server
import com.example.login_v3.home.setting.Tg_Setting
import androidx.lifecycle.viewmodel.compose.viewModel



@Composable
fun MainScreen_tab() {

    val screensViewModel: ScreensViewModel = viewModel()
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    //show bottom bar or not
    val showBottomBar by bottomBarViewModel.showBottomBar.collectAsState()
    val navHeight = 64.dp
    val gap = 40.dp
    val bottomBarHeight = navHeight + gap

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFDA7029),
                    Color(0xFF777777),
                    Color(0xFFB34800)
                )
            )),

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
                        Spacer(modifier = Modifier.height(gap)) // 真正的底下空白
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
    messageViewModel: MessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    //nav_wheel_selecting tab
    val selectedScreen by screensViewModel.selected.collectAsState()
    //message_contact_list_import
    val contacts by messageViewModel.contacts.collectAsState()


    when (selectedScreen) {
        Screen.Message -> Tg_Message()
        Screen.Server -> Tg_Server(viewModel = viewModel())
        Screen.MarketPlace -> Tg_MarketPlace()
        Screen.Setting -> Tg_Setting(
            bottomBarViewModel = bottomBarViewModel
        )
    }
}


