package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
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

@Composable
fun MeOtherSection(
    onAboutAuthorClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(id = R.string.me_other_section_title))

        Item(
            text = stringResource(id = R.string.me_about_author),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.StarOutline),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onAboutAuthorClick,
        )

        Item(
            text = stringResource(id = R.string.me_share_app),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Share),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onShareAppClick,
        )

        Item(
            text = stringResource(id = R.string.me_more_info),
            iconPainter = rememberVectorPainter(image = Icons.Outlined.Info),
            iconColor = SaltTheme.colors.text,
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            onClick = onMoreInfoClick,
        )
    }
}
