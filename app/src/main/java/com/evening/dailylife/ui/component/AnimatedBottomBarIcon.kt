package com.evening.dailylife.ui.component

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.evening.dailylife.ui.navigation.Route
import com.evening.dailylife.ui.navigation.Screen

private enum class AnimationState {
    Selected,
    Deselected
}

@Composable
fun AnimatedBottomBarIcon(
    screen: Screen,
    isSelected: Boolean
) {
    val transition = updateTransition(
        targetState = if (isSelected) AnimationState.Selected else AnimationState.Deselected,
        label = "${screen.route} transition"
    )

    when (screen.route) {
        Route.DETAILS -> {
            // "明细": "心跳" 或 "呼吸感" 动画
            val scale by transition.animateFloat(
                transitionSpec = {
                    when {
                        // 从 "未选中" 到 "选中" 时，播放心跳动画
                        AnimationState.Deselected isTransitioningTo AnimationState.Selected ->
                            keyframes {
                                durationMillis = 300
                                // 在 100ms 时快速放大到 1.3 倍，制造 "冲击感"
                                1.3f at 100
                                // 动画结束时，稳定在 1.15 倍大小
                                1.15f at 300
                            }
                        // 从 "选中" 到 "未选中" 时，平滑恢复
                        else ->
                            tween(durationMillis = 200)
                    }
                },
                label = "details_scale_pulse"
            ) { state ->
                // 定义动画的最终目标状态
                if (state == AnimationState.Selected) 1.15f else 1.0f
            }
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
        Route.CHART -> {
            // "图表": 模拟柱子增长的动画
            val scaleY by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 300) },
                label = "chart_scaleY"
            ) { state ->
                if (state == AnimationState.Selected) 1.1f else 1.0f
            }
            val offsetY by transition.animateDp(
                transitionSpec = { tween(durationMillis = 300) },
                label = "chart_offsetY"
            ) { state ->
                if (state == AnimationState.Selected) (-4).dp else 0.dp
            }
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer(
                    scaleY = scaleY,
                    translationY = offsetY.value
                )
            )
        }
        Route.DISCOVER -> {
            // "发现": 旋转动画
            val rotation by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 400) },
                label = "discover_rotation"
            ) { state ->
                if (state == AnimationState.Selected) 360f else 0f
            }
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
        Route.ME -> {
            // "我的": 突出 "点头" 动画
            val scale by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 200) }, // 使用简单的 tween，不再 spring
                label = "me_scale"
            ) { state ->
                if (state == AnimationState.Selected) 1.1f else 1.0f
            }

            // 加大点头角度，使其更明显
            val rotationX by transition.animateFloat(
                transitionSpec = {
                    when {
                        AnimationState.Deselected isTransitioningTo AnimationState.Selected ->
                            keyframes {
                                durationMillis = 400
                                30f at 150 // 角度加大到 30
                                0f at 350
                            }
                        else ->
                            tween(durationMillis = 150)
                    }
                },
                label = "me_rotationX"
            ) { state ->
                if (state == AnimationState.Selected) 0f else 0f
            }

            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.rotationX = rotationX
                }
            )
        }
        else -> {
            Icon(
                imageVector = screen.icon,
                contentDescription = null
            )
        }
    }
}