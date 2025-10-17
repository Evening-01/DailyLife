package com.evening.dailylife.feature.discover

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import java.text.DecimalFormat

@Composable
fun TypeProfileSection(
    profile: TypeProfile,
    numberFormatter: DecimalFormat,
    modifier: Modifier = Modifier
) {
    val total = profile.total
    if (total <= 0.0) {
        Text(
            text = stringResource(id = R.string.chart_type_profile_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    val percentFormatter = remember { DecimalFormat("0%") }
    val expenseAmountText = numberFormatter.format(profile.expenseTotal)
    val incomeAmountText = numberFormatter.format(profile.incomeTotal)
    val expenseCountText = stringResource(
        id = R.string.chart_type_profile_transactions,
        profile.expenseCount
    )
    val incomeCountText = stringResource(
        id = R.string.chart_type_profile_transactions,
        profile.incomeCount
    )
    val expenseRatioText = percentFormatter.format(profile.expenseRatio.toDouble())
    val incomeRatioText = percentFormatter.format(profile.incomeRatio.toDouble())
    val balanceText = stringResource(
        id = R.string.chart_type_profile_balance,
        numberFormatter.format(profile.net)
    )

    val balanceColor = when {
        profile.net > 0.0 -> MaterialTheme.colorScheme.primary
        profile.net < 0.0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeProfileStatCard(
                label = stringResource(id = R.string.chart_type_profile_expense),
                amountText = expenseAmountText,
                countText = expenseCountText,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TypeProfileStatCard(
                label = stringResource(id = R.string.chart_type_profile_income),
                amountText = incomeAmountText,
                countText = incomeCountText,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TypeProfileRatioBar(
            expenseRatio = profile.expenseRatio,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(
                id = R.string.chart_type_profile_ratio,
                expenseRatioText,
                incomeRatioText
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = balanceText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = balanceColor,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun TypeProfileRatioBar(
    expenseRatio: Float,
    modifier: Modifier = Modifier
) {
    val clampedExpenseRatio = expenseRatio.coerceIn(0f, 1f)
    val incomeRatio = 1f - clampedExpenseRatio
    val hasExpense = clampedExpenseRatio > 0f
    val hasIncome = incomeRatio > 0f
    val trackShape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .height(8.dp)
            .clip(trackShape)
    ) {
        if (hasExpense) {
            Box(
                modifier = Modifier
                    .weight(clampedExpenseRatio)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error)
            )
        }
        if (hasIncome) {
            Box(
                modifier = Modifier
                    .weight(incomeRatio)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun TypeProfileStatCard(
    label: String,
    amountText: String,
    countText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
