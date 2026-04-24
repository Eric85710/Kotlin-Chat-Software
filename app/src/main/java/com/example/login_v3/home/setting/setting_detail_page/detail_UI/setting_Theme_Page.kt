package com.example.login_v3.home.setting.setting_detail_page.detail_UI

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.login_v3.home.setting.setting_detail_page.viewmodel.Theme_ViewModel
import com.example.login_v3.ui.theme.AppTheme

@Composable
fun setting_theme_page(
    viewModel: Theme_ViewModel
){
    // 直接讀取，不需要 .value
    val selectedTheme by viewModel.currentTheme.collectAsState()

    Box(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "主題設定",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 遍歷所有 Enum 選項
            AppTheme.entries.forEach { theme ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (theme == selectedTheme),
                            onClick = { viewModel.updateTheme(theme) }
                        )
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = (theme == selectedTheme),
                        onClick = { viewModel.updateTheme(theme) }
                    )
                    Text(
                        text = when(theme) {
                            AppTheme.SYSTEM -> "follow"
                            AppTheme.LIGHT -> "light"
                            AppTheme.DARK -> "dark"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}