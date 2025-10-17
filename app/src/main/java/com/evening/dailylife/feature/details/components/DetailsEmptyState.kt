package com.evening.dailylife.feature.details.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.component.EmptyPlaceholder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong

/**
 * 明细页空状态。
 */
@Composable
fun DetailsEmptyState(modifier: Modifier = Modifier) {
    EmptyPlaceholder(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        contentDescription = stringResource(R.string.details_empty_state_content_description),
        message = stringResource(R.string.details_empty_state_message),
        modifier = modifier
    )
}
