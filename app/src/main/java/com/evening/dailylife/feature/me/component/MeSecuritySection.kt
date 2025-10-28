package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme

@Composable
fun MeSecuritySection(
    fingerprintLockEnabled: Boolean,
    isFingerprintSupported: Boolean,
    onFingerprintToggle: (Boolean) -> Unit,
    onFingerprintUnsupported: () -> Unit,
    onDataManagementClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(R.string.data_and_security))

        Box {
            ItemSwitcher(
                state = fingerprintLockEnabled,
                onChange = onFingerprintToggle,
                enabled = isFingerprintSupported,
                text = stringResource(R.string.fingerprint_security),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Fingerprint),
                iconColor = SaltTheme.colors.text,
                iconPaddingValues = PaddingValues(all = 1.8.dp),
            )

            if (!isFingerprintSupported) {
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            onFingerprintUnsupported()
                        },
                )
            }
        }

        Item(
            text = stringResource(R.string.data_management),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Dns),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onDataManagementClick,
        )
    }
}
