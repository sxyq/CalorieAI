package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 列表项入场动画（参考Deadliner风格）
 * 从下方滑入 + 淡入效果
 */
@Composable
fun AnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index * 50L) // 阶梯式延迟
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = EaseOutCubic
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            translationY = offsetY.value
            this.alpha = alpha.value
        }
    ) {
        content()
    }
}

/**
 * 页面内容切换动画
 */
@Composable
fun <T> AnimatedContentSwitch(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) + slideInVertically(
                animationSpec = tween(400, easing = EaseOutCubic),
                initialOffsetY = { it / 10 }
            ) togetherWith
            fadeOut(
                animationSpec = tween(200)
            ) + slideOutVertically(
                animationSpec = tween(300, easing = EaseInCubic),
                targetOffsetY = { -it / 10 }
            )
        },
        label = "ContentSwitch"
    ) { state ->
        content(state)
    }
}

/**
 * 卡片点击缩放动画
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable {
                // 点击时先缩小再恢复
                scope.launch {
                    scale.animateTo(
                        targetValue = 0.95f,
                        animationSpec = tween(100)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    onClick()
                }
            }
    ) {
        content()
    }
}

/**
 * 数字变化动画
 */
@Composable
fun AnimatedNumber(
    targetNumber: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetNumber) {
        animatedValue.animateTo(
            targetValue = targetNumber.toFloat(),
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutCubic
            )
        )
    }

    content(animatedValue.value.toInt())
}

/**
 * 进度条动画
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutCubic
            )
        )
    }

    LinearProgressIndicator(
        progress = { animatedProgress.value },
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * 顶部渐隐效果（用于列表）
 */
fun Modifier.fadingTopEdge(height: Dp = 32.dp): Modifier = this
    .graphicsLayer(compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val h = height.toPx().coerceAtLeast(1f)
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    androidx.compose.ui.graphics.Color.Transparent,
                    androidx.compose.ui.graphics.Color.Black
                ),
                startY = 0f,
                endY = h
            ),
            size = size.copy(height = h),
            blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
        )
    }
