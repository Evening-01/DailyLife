package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.evening.dailylife.R
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme

@Composable
fun MeSecuritySection(
    fingerprintLockEnabled: Boolean,
    onFingerprintToggle: (Boolean) -> Unit,
    onDataManagementClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(R.string.data_and_security))

        ItemSwitcher(
            state = fingerprintLockEnabled,
            onChange = onFingerprintToggle,
            text = stringResource(R.string.fingerprint_security),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Fingerprint),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
        )

        Item(
            text = stringResource(R.string.data_management),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Dns),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onDataManagementClick,
        )
    }
}
