package com.evening.dailylife.feature.me.about

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.evening.dailylife.BuildConfig
import com.evening.dailylife.R
import com.evening.dailylife.core.navigation.safePopBackStack
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import java.text.DateFormat
import java.util.Date
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun AboutAppScreen(navController: NavHostController) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (error: PackageManager.NameNotFoundException) {
            null
        }
    }

    val installDate = packageInfo?.firstInstallTime?.takeIf { it > 0L }
    val updateDate = packageInfo?.lastUpdateTime?.takeIf { it > 0L }
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    val highlightChips = listOf(
        stringResource(id = R.string.me_about_app_highlight_1),
        stringResource(id = R.string.me_about_app_highlight_2),
        stringResource(id = R.string.me_about_app_highlight_3),
        stringResource(id = R.string.me_about_app_highlight_4),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.me_more_info),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            HeaderCard()

            SectionCard {
                Text(
                    text = stringResource(id = R.string.me_about_app_details_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(
                    icon = Icons.Outlined.Update,
                    label = stringResource(id = R.string.me_about_app_version_label),
                    value = BuildConfig.VERSION_NAME,
                )
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = stringResource(id = R.string.me_about_app_build_label),
                    value = stringResource(
                        id = R.string.me_about_app_build_value,
                        BuildConfig.VERSION_CODE
                    ),
                )
                InfoRow(
                    icon = Icons.Outlined.Lock,
                    label = stringResource(id = R.string.me_about_app_package_label),
                    value = context.packageName,
                )
                if (updateDate != null) {
                    InfoRow(
                        icon = Icons.Outlined.Update,
                        label = stringResource(id = R.string.me_about_app_last_update_label),
                        value = dateFormatter.format(Date(max(updateDate, installDate ?: updateDate))),
                    )
                }
            }

            SectionCard {
                Text(
                    text = stringResource(id = R.string.me_about_app_highlights_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowChipGroup(highlightChips)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.me_about_app_highlights_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard {
                Text(
                    text = stringResource(id = R.string.me_about_app_open_source_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy( alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.me_about_app_open_source_summary),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.me_about_app_open_source_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard {
                Text(
                    text = stringResource(id = R.string.me_about_app_support_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.me_about_app_support_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.me_about_app_support_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Surface(
                    modifier = Modifier.size(112.dp),
                    shape = CircleShape,
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.45f
                                scaleY = 1.45f
                            },
                        contentScale = ContentScale.Fit,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.me_about_app_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChipGroup(entries: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEach { entry ->
            AssistChip(
                onClick = {},
                label = { Text(text = entry) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    labelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
