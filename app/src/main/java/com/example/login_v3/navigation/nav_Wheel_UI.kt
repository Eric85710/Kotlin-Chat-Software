package com.example.login_v3.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun NavWheelUI() {
    val options = listOf("Home", "Message", "Setting", "Marketplace")
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // 計算點擊角度
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val angle = (atan2(dy, dx).toDegrees() + 360) % 360

                    // 對應選項
                    val sectorAngle = 360f / options.size
                    val index = ((angle + sectorAngle / 2) / sectorAngle).toInt() % options.size
                    selectedOption = options[index]
                    println("Selected: ${options[index]}")
                }
            }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 3f
            val sectorAngle = 360f / options.size

            // 畫圓形選項區塊
            for (i in options.indices) {
                val startAngle = -90f + i * sectorAngle
                val sweepAngle = sectorAngle
                drawArc(
                    color = if (selectedOption == options[i]) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )

                // 計算文字位置
                val angleRad = (startAngle + sweepAngle / 2f).toRadians()
                val textX = center.x + cos(angleRad) * radius * 0.6f
                val textY = center.y + sin(angleRad) * radius * 0.6f

                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        options[i],
                        textX,
                        textY,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 40f
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}

// 輔助函數
fun Float.toRadians(): Float = (this * PI / 180).toFloat()
fun Float.toDegrees(): Float = (this * 180 / PI).toFloat()
