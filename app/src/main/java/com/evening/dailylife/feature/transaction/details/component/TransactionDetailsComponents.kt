package com.evening.dailylife.feature.transaction.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.model.MoodRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailsContent(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    iconVector: ImageVector,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TransactionSummaryCard(
            transaction = transaction,
            iconVector = iconVector,
        )
        Spacer(modifier = Modifier.height(24.dp))
        TransactionDetailsList(transaction)
        Spacer(modifier = Modifier.weight(1f))
        ActionButtons(
            onDelete = onDelete,
            onEdit = onEdit,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TransactionSummaryCard(
    transaction: TransactionEntity,
    iconVector: ImageVector,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = String.format(Locale.CHINA, "%+.2f", transaction.amount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                val glassReflectionBrush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.5f),
                    ),
                )

                Icon(
                    imageVector = iconVector,
                    contentDescription = transaction.category,
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(
                                    brush = glassReflectionBrush,
                                    blendMode = BlendMode.SrcIn,
                                )
                            }
                        },
                )
            }
        }
    }
}

@Composable
fun TransactionDetailsList(transaction: TransactionEntity) {
    val datePattern = stringResource(R.string.transaction_details_date_pattern)
    val sdf = remember(datePattern) { SimpleDateFormat(datePattern, Locale.CHINESE) }
    val dateString = sdf.format(Date(transaction.date))

    Column(modifier = Modifier.fillMaxWidth()) {
        DetailItem(
            label = stringResource(R.string.transaction_detail_category),
            value = transaction.category,
        )
        Divider()
        DetailItem(
            label = stringResource(R.string.transaction_detail_time),
            value = dateString,
        )
        Divider()
        DetailItem(
            label = stringResource(R.string.transaction_detail_source),
            value = transaction.source.ifBlank {
                stringResource(R.string.transaction_detail_source_default)
            },
        )
        Divider()

        if (transaction.mood != null) {
            MoodRepository.getMoodByScore(transaction.mood)?.let { mood ->
                val moodName = stringResource(mood.nameRes)
                MoodDetailItem(
                    label = stringResource(R.string.transaction_detail_mood),
                    mood = moodName,
                    icon = {
                        Icon(
                            imageVector = mood.icon,
                            contentDescription = moodName,
                            modifier = Modifier.size(24.dp),
                            tint = mood.color,
                        )
                    },
                )
                Divider()
            }
        }

        DetailItem(
            label = stringResource(R.string.transaction_detail_note),
            value = transaction.description.ifBlank {
                stringResource(R.string.transaction_detail_note_empty)
            },
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MoodDetailItem(
    label: String,
    mood: String,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mood,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        icon()
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
    )
}

@Composable
private fun ActionButtons(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(onClick = onDelete) {
            Text(text = stringResource(R.string.common_delete))
        }
        OutlinedButton(onClick = onEdit) {
            Text(text = stringResource(R.string.common_edit))
        }
    }
}
