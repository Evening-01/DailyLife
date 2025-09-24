package com.evening.dailylife.ui.screens.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DiscoverScreen() {
    // 这里是发现页面内容
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("发现页面")
    }
}