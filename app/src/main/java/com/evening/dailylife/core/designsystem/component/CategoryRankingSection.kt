package com.evening.dailylife.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.feature.chart.ChartContentStatus
import com.evening.dailylife.feature.chart.ChartCategoryRank
import com.evening.dailylife.feature.chart.ChartType
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltApi
import java.text.NumberFormat
import java.util.Locale

@OptIn(UnstableSaltApi::class)
@Composable
internal fun CategoryRankingSection(
    ranks: List<ChartCategoryRank>,
    type: ChartType,
    amountFormatter: (Double) -> String,
    animationKey: Any?,
    contentStatus: ChartContentStatus,
    modifier: Modifier = Modifier
) {
    val titleRes = if (type == ChartType.Expense) {
        R.string.chart_rank_title_expense
    } else {
        R.string.chart_rank_title_income
    }
    val percentFormatter = remember {
        NumberFormat.getPercentInstance(Locale.CHINA).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }
    }

    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(id = titleRes))

        when (contentStatus) {
            ChartContentStatus.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            ChartContentStatus.Empty -> {
                CategoryRankingEmptyState()
            }
            ChartContentStatus.Content -> {
                if (ranks.isEmpty()) {
                    CategoryRankingEmptyState()
                } else {
                    ranks.forEachIndexed { index, rank ->
                        CategoryRankingItem(
                            rank = rank,
                            amountFormatter = amountFormatter,
                            percentFormatter = percentFormatter,
                            animationKey = animationKey,
                        )

                        if (index != ranks.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRankingEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.chart_rank_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryRankingItem(
    rank: ChartCategoryRank,
    amountFormatter: (Double) -> String,
    percentFormatter: NumberFormat,
    animationKey: Any?
) {
    val context = LocalContext.current
    val icon = remember(rank.category) { TransactionCategoryRepository.getIcon(context, rank.category) }
    val percentText = remember(rank.ratio) { percentFormatter.format(rank.ratio.toDouble()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = rank.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = amountFormatter(rank.amount),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CategoryRatioBar(
                ratio = rank.ratio,
                animationKey = animationKey
            )
        }
    }
}


@Composable
private fun CategoryRatioBar(
    ratio: Float,
    animationKey: Any?,
    modifier: Modifier = Modifier
) {
    val target = ratio.coerceIn(0f, 1f)
    val progress = remember { Animatable(0f) }
    val animationSpec = remember { tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing) }

    LaunchedEffect(animationKey, target) {
        progress.snapTo(0f)
        progress.animateTo(target, animationSpec)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
