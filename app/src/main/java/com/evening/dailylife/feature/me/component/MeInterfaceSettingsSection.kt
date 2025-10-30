package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi

@OptIn(UnstableSaltApi::class)
@Composable
fun MeInterfaceSettingsSection(
    onGeneralSettingsClick: () -> Unit,
    onQuickUsageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(R.string.user_interface))

        Item(
            text = stringResource(R.string.general_settings),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Tune),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onGeneralSettingsClick,
        )

        Item(
            text = stringResource(R.string.accounting_preferences),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.AccountBalanceWallet),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onQuickUsageClick,
        )
    }
}
