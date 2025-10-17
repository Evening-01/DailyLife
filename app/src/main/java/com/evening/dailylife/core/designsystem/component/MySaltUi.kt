package com.evening.dailylife.core.designsystem.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.core.util.PopupPositionCalculator
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupState


@SuppressLint("ConfigurationScreenWidthHeight")
@UnstableSaltApi
@Composable
fun ItemPopup(
    state: PopupState,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null,
    selectedItem: String = "",
    popupWidth: Int = 160,
    rightSubWeight: Float = 0.5f,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    Box {
        val boxWidth = remember { mutableFloatStateOf(0f) }
        val clickOffsetX = remember { mutableFloatStateOf(0f) }
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            clickOffsetX.floatValue = offset.x
                            state.expend()
                        },
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        })
                }
                .onGloballyPositioned { layoutCoordinates ->
                    boxWidth.floatValue = layoutCoordinates.size.width.toFloat()
                }
                .padding(
                    horizontal = SaltTheme.dimens.innerHorizontalPadding,
                    vertical = SaltTheme.dimens.innerVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            iconPainter?.let {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(iconPaddingValues),
                    painter = iconPainter,
                    contentDescription = null,
                    colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
                )
                Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = text,
                    color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
                )
                if (sub != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sub,
                        style = SaltTheme.textStyles.sub
                    )
                }
            }
            Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
            Text(
                modifier = Modifier
                    .weight(if (selectedItem.isEmpty()) 0.001f else rightSubWeight),
                text = selectedItem,
                color = SaltTheme.colors.subText,
                fontSize = 14.sp,
                textAlign = TextAlign.End
            )
            Icon(
                modifier = Modifier
                    .size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                contentDescription = null,
                tint = SaltTheme.colors.subText
            )
        }
        PopupMenu(
            modifier = Modifier.width(popupWidth.dp),  //TODO 宽度
            expanded = state.expend,
            onDismissRequest = {
                state.dismiss()
            },
            offset = DpOffset(
                PopupPositionCalculator.calculateOffset(
                    density = context.resources.displayMetrics.density,
                    clickOffsetX = clickOffsetX.floatValue,
                    popupWidth = popupWidth,
                    screenWidthDp = LocalConfiguration.current.screenWidthDp
                ).dp,
                0.dp
            ),
        ) {
            content()
        }
    }
}
