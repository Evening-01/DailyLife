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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.feature.chart.ChartCategoryRank
import com.evening.dailylife.feature.chart.ChartType
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltApi
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@OptIn(UnstableSaltApi::class)
@Composable
internal fun CategoryRankingSection(
    ranks: List<ChartCategoryRank>,
    type: ChartType,
    numberFormatter: DecimalFormat,
    animationKey: Any?,
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

        if (ranks.isEmpty()) {
            Text(
                text = stringResource(id = R.string.chart_rank_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            ranks.forEachIndexed { index, rank ->
                CategoryRankingItem(
                    rank = rank,
                    numberFormatter = numberFormatter,
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

@Composable
private fun CategoryRankingItem(
    rank: ChartCategoryRank,
    numberFormatter: DecimalFormat,
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rank.category,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CategoryRatioBar(
                ratio = rank.ratio,
                animationKey = animationKey
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    id = R.string.chart_rank_amount,
                    numberFormatter.format(rank.amount)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
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
