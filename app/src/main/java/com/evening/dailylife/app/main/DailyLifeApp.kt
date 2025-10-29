package com.evening.dailylife.app.main

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.app.navigation.AppNavHost

@Composable
fun DailyLifeApp(navController: NavHostController) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AppNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        UnsupportedVersionContent()
    }
}

@Composable
private fun UnsupportedVersionContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.unsupported_android_version),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
