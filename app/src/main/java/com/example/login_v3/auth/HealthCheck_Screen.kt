package com.example.login_v3.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.login_v3.data.health.HealthCheckViewModel

@Composable
fun HealthCheckScreen(
    viewModel: HealthCheckViewModel = hiltViewModel()
) {
    val status = viewModel.healthStatus
    val detail = viewModel.detailMessage

    //layout
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //health
            Text(text = "API Health", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // 顯示狀態
            Text(text = "Status: $status", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(8.dp))

            // 顯示詳細訊息
            Text(text = detail, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))

            // 如果正在檢查，顯示圓形進度
            if (status == "Checking...") {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row {
                Button(onClick = { viewModel.checkApiHealth() }) {
                    Text("Check Health")
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(onClick = {
                    // 若需要重置狀態，可在 ViewModel 加一個 reset 方法，或直接呼叫 repository 前先改狀態
                }) {
                    Text("Reset")
                }
            }
        }
    }
}
