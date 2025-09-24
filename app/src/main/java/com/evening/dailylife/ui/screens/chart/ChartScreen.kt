package com.evening.dailylife.ui.screens.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChartScreen() {
    // 这里是你的图表页面内容
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("图表页面")
    }
}