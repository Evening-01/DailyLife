package com.evening.dailylife.ui.screens.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DetailsScreen() {
    // 这里是明细页面内容


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("明细页面")
    }
}