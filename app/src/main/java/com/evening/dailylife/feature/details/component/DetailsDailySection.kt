package com.evening.dailylife.feature.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.SuccessGreen
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.model.TransactionCategoryRepository
import kotlin.math.abs

@Composable
fun DailyHeader(
    date: String,
    income: Double,
    expense: Double,
    mood: String,
) {
    val context = LocalContext.current
    val todayLabel = stringResource(R.string.label_today)
    val formattedDate = if (date.startsWith(todayLabel)) {
        todayLabel
    } else {
        val parts = date.split(" ")
        val dateParts = parts[0].split("/")
        stringResource(
            R.string.details_month_day_with_weekday,
            dateParts[0],
            dateParts[1],
            parts.getOrNull(1).orEmpty(),
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))

        if (mood.isNotBlank()) {
            Icon(
                imageVector = MoodRepository.getIcon(context, mood),
                contentDescription = stringResource(R.string.details_mood_content_description),
                modifier = Modifier.size(20.dp),
                tint = MoodRepository.getColor(context, mood),
            )
        }
        Row {
            if (income > 0) {
                Text(
                    text = stringResource(
                        R.string.details_income_amount,
                        "%.2f".format(income),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (expense < 0) {
                Text(
                    text = stringResource(
                        R.string.details_expense_amount,
                        "%.2f".format(abs(expense)),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TransactionCategoryRepository.getIcon(context, transaction.category),
                    contentDescription = transaction.category,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (transaction.description.isNotBlank()) {
                    Column {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Text(
                text = "%.2f".format(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) SuccessGreen else MaterialTheme.colorScheme.error,
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp),
        )
    }
}
