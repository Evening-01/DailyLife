package com.evening.dailylife.feature.transaction.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.model.TransactionCategory
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.rememberPopupState

/**
 * 心情选择器，提供表情弹窗供用户快速标记。
 */
@OptIn(UnstableSaltApi::class)
@Composable
fun MoodSelector(
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val popupState = rememberPopupState()
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                popupState.expend()
            }
        )
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selectedMood.isNotEmpty()) {
                        MoodRepository.getIcon(context, selectedMood)
                    } else {
                        Icons.Outlined.EmojiEmotions
                    },
                    contentDescription = stringResource(R.string.editor_mood_select),
                    modifier = Modifier.size(24.dp),
                    tint = if (selectedMood.isNotEmpty()) {
                        MoodRepository.getColor(context, selectedMood)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        PopupMenu(
            expanded = popupState.expend,
            onDismissRequest = { popupState.dismiss() },
            offset = DpOffset((-10).dp, (-100).dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoodRepository.moods.forEach { mood ->
                    val moodName = stringResource(mood.nameRes)
                    Icon(
                        imageVector = mood.icon,
                        contentDescription = moodName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                onMoodSelected(moodName)
                                popupState.dismiss()
                            }
                            .background(
                                if (selectedMood == moodName) {
                                    MoodRepository.getColor(context, moodName).copy(alpha = 0.2f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(6.dp),
                        tint = MoodRepository.getColor(context, moodName)
                    )
                }
            }
        }
    }
}

/**
 * 分类入口卡片，统一点击与选中状态表现。
 */
@Composable
fun CategoryItem(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
