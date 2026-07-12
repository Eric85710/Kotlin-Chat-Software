package com.example.login_v3.home.setting

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.login_v3.navigation.ScreensViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.setting_list_Screen(
    onItemClick: (String, String) -> Unit,
    viewModel: SettingViewModel = viewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope,   //scaling animation effect
){

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    val settings by viewModel.settings.collectAsState()

    // 搜尋過濾邏輯
    val filteredSettings = remember(query, settings) {
        if (query.isEmpty()) {
            settings
        } else {
            settings.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        (item.description?.contains(query, ignoreCase = true) ?: false) ||
                        item.keywords.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
    ) {

        Spacer(modifier = Modifier.height(46.dp))

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { active = false },
            active = active,
            shape = RoundedCornerShape(12.dp),
            onActiveChange = { active = it },
            placeholder = { Text("搜尋設定 (例如: dark, profile...)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)

        ) {
            // 這裡是 SearchBar 展開後的內容
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filteredSettings) { index, item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { item.description?.let { Text(it) } },
                        leadingContent = { Icon(item.setting_icon, contentDescription = null) },
                        modifier = Modifier.clickable {
                            onItemClick(item.title, item.iconKey)
                            active = false
                        }
                    )
                }
            }
        } // ← 注意這個大括號要結束 SearchBar

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp), // 每個 item 之間 8.dp
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            itemsIndexed(filteredSettings){ index, item ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
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
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                    )

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable{ onItemClick(item.title, item.iconKey)}
                    ) {
                        val iconState = rememberSharedContentState(key = "setting_icon_${item.iconKey}")
                        val titleState = rememberSharedContentState(key = "setting_title_${item.iconKey}_${Uri.encode(item.title)}")


                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = " ${item.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .sharedElement(
                                        sharedContentState = titleState,
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            tween(durationMillis = 1000)

                                        }
                                    )
                                    .padding(bottom = 4.dp)
                            )
                            item.description?.let {
                                Text(text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(item.setting_icon,
                            contentDescription = item.title,
                            modifier = Modifier
                                .sharedElement(
                                    sharedContentState = iconState,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 400)

                                    }
                                )
                                .padding(bottom = 4.dp)
                                .size(54.dp),
                            tint = Color.White
                        )
                    }
                }

            }
        }
    }

}