package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.feature.me.MeProfileStatsUiState

@Composable
fun MeProfileHeader(
    stats: MeProfileStatsUiState,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = stringResource(R.string.me_profile_avatar_content_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.me_profile_display_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                    )
                    Text(
                        text = stringResource(R.string.me_profile_signature),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                    )
                }
            }

            MeProfileStatsRow(
                stats = stats,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun MeProfileStatsRow(
    stats: MeProfileStatsUiState,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val consecutiveDaysText = if (stats.isLoading) "--" else stats.consecutiveCheckInDays.toString()
    val totalDaysText = if (stats.isLoading) "--" else stats.totalActiveDays.toString()
    val totalTransactionsText = if (stats.isLoading) "--" else stats.totalTransactions.toString()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MeProfileStatItem(
            value = consecutiveDaysText,
            label = stringResource(R.string.me_profile_stat_streak),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalDaysText,
            label = stringResource(R.string.me_profile_stat_total_days),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalTransactionsText,
            label = stringResource(R.string.me_profile_stat_total_transactions),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MeProfileStatItem(
    value: String,
    label: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.7f),
        )
    }
}
