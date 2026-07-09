package com.example.login_v3.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.yield
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavHostController


//nav_wheel_icon數據
data class WheelItemData(
    val icon: ImageVector,
    val screen: Screen
)


@Composable
fun nav_Wheel_display_block(
    navController: NavHostController,
    screensViewModel: ScreensViewModel
) {
    var isPressed by remember { mutableStateOf(false) }

    //position_animation
    val floatingOffset by animateDpAsState(
        targetValue = if (isPressed) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
    )
    //scale_animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    //different tab
    val items = listOf(
        WheelItemData(Icons.Filled.Message, Screen.Message),
        WheelItemData(Icons.Filled.Dns, Screen.Server),
        WheelItemData(Icons.Filled.Extension, Screen.MarketPlace),
        WheelItemData(Icons.Filled.Settings, Screen.Setting)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                translationY = floatingOffset.toPx()
                scaleX = scale
                scaleY = scale
            }
        ) {
            HorizontalWheelPicker(
                items = items,
                onValueChange = { item ->
                    if (screensViewModel.selected.value != item.screen) {
                        screensViewModel.select(item.screen)
                        // Note: Navigation is now handled by the Pager in MainScreen_tab
                        // which observes the screensViewModel.selected state.
                    }
                },
                onInteractionChanged = { isPressed = it }
            )
        }
    }
}





//wheel picker
@Composable
fun HorizontalWheelPicker(
    items: List<WheelItemData>,
    itemWidth: Dp = 80.dp,
    onValueChange: (WheelItemData) -> Unit,
    onInteractionChanged: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // 計算動態 horizontal padding（假設 LazyRow 佔滿螢幕寬度）
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp - itemWidth) / 2

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
    ) {
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                WheelItem(index = index, itemWidth = itemWidth, item = item)
            }
        }

        // 中央指示線（不變）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(itemWidth)
                .fillMaxHeight()
        ) {
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
            )
        }
    }

    val isInteracting by remember { derivedStateOf { listState.isScrollInProgress } }

    // 🎯 性能優化：只有當「中心項」真正改變時，才觸發更新
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@snapshotFlow -1

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            
            // 找到離中心最近的 Item
            val closest = visibleItems.minByOrNull { vi ->
                kotlin.math.abs((vi.offset + vi.size / 2) - viewportCenter)
            } ?: visibleItems.first()
            
            closest.key as Int
        }
            .filter { it != -1 }
            .distinctUntilChanged() // 關鍵：只有 Index 改變才往下走
            .collectLatest { selectedIndex ->
                yield() // 給予 UI 線程呼吸空間，防止快速滑動時卡頓
                onValueChange(items[selectedIndex])
            }
    }

    // 通知互動狀態
    LaunchedEffect(isInteracting) {
        onInteractionChanged(isInteracting)
    }
}



//wheel item


@Composable
fun WheelItem(
    index: Int,
    itemWidth: Dp,
    item: WheelItemData
) {
    Box(
        modifier = Modifier
            .width(itemWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

