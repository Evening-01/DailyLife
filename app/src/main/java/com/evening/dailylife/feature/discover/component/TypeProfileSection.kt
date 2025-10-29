package com.evening.dailylife.feature.discover.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.SuccessGreen
import com.evening.dailylife.feature.discover.model.TypeProfile
import java.text.DecimalFormat

@Composable
fun TypeProfileSection(
    profile: TypeProfile,
    numberFormatter: DecimalFormat,
    modifier: Modifier = Modifier,
    animationKey: Any? = null,
) {
    val total = profile.total
    val percentFormatter = remember { DecimalFormat("0%") }
    val expenseAmountText = numberFormatter.format(profile.expenseTotal)
    val incomeAmountText = numberFormatter.format(profile.incomeTotal)
    val balanceAmountText = numberFormatter.format(profile.net)
    val expenseRatio = profile.expenseRatio.coerceIn(0f, 1f)
    val expenseRatioText = percentFormatter.format(expenseRatio.toDouble())

    val expenseColor = MaterialTheme.colorScheme.error
    val incomeColor = SuccessGreen
    val neutralBalanceColor = MaterialTheme.colorScheme.onSurface
    val defaultValueColor = MaterialTheme.colorScheme.onSurface
    val progressColor = MaterialTheme.colorScheme.primary

    val expenseProgress = remember { Animatable(0f) }
    val animationSpec = remember {
        tween<Float>(
            durationMillis = 800,
            easing = FastOutSlowInEasing,
        )
    }
    LaunchedEffect(animationKey, expenseRatio) {
        expenseProgress.snapTo(0f)
        expenseProgress.animateTo(expenseRatio, animationSpec = animationSpec)
    }

    Row(
        modifier = modifier
            .heightIn(min = 132.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TypeProfileExpenseProgress(
            progress = expenseProgress.value,
            ratioText = expenseRatioText,
            progressColor = progressColor,
            modifier = Modifier.size(140.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TypeProfileBalanceRow(
                balanceLabel = stringResource(id = R.string.discover_type_profile_balance_label),
                balanceText = balanceAmountText,
                valueColor = when {
                    profile.net > 0.0 -> incomeColor
                    profile.net < 0.0 -> expenseColor
                    else -> neutralBalanceColor
                },
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp,
            )

            TypeProfileDetailRow(
                label = stringResource(id = R.string.discover_type_profile_month_expense),
                value = expenseAmountText,
                valueColor = defaultValueColor,
            )
            TypeProfileDetailRow(
                label = stringResource(id = R.string.discover_type_profile_month_income),
                value = incomeAmountText,
                valueColor = defaultValueColor,
            )
        }
    }
}

@Composable
private fun TypeProfileExpenseProgress(
    progress: Float,
    ratioText: String,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress },
                color = progressColor,
                trackColor = trackColor,
                strokeWidth = 10.dp,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = ratioText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.discover_type_profile_expense_ratio_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TypeProfileBalanceRow(
    balanceLabel: String,
    balanceText: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = balanceLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = balanceText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

@Composable
private fun TypeProfileDetailRow(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 3,
            overflow = TextOverflow.Clip,
            softWrap = true,
        )
    }
}
