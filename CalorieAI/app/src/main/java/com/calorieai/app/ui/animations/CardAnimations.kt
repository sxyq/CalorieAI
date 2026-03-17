package com.calorieai.app.ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 卡片动画时长配置
 * 遵循 Glass 主题规范
 */
object CardAnimationDurations {
    const val PRESS_FEEDBACK = 100
    const val HOVER_ELEVATION = 200
    const val APPEAR_FADE = 250
    const val APPEAR_SCALE = 300
    const val ELEVATION_CHANGE = 200
    const val RELEASE_BOUNCE = 250
}

/**
 * 卡片动画缓动曲线
 */
object CardAnimationEasings {
    // 标准缓动
    val Standard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)

    // 弹跳缓动
    val Bounce = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    // 减速缓动
    val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1f)
}

/**
 * 按压反馈效果（scale 0.98）
 * 用于卡片点击时的缩放反馈
 */
fun Modifier.pressFeedbackScale(
    scale: Float = 0.98f,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pressFeedbackScale"
        properties["scale"] = scale
        properties["enabled"] = enabled
    }
) {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animScale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            animScale.animateTo(
                targetValue = scale,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.PRESS_FEEDBACK,
                    easing = CardAnimationEasings.Standard
                )
            )
        } else {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    this
        .scale(animScale.value)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {}
        )
}

/**
 * 悬停效果（elevation 提升）
 * 用于卡片悬停时的阴影提升效果
 */
@Composable
fun rememberHoverElevation(
    defaultElevation: Dp = 2.dp,
    hoveredElevation: Dp = 8.dp,
    isHovered: Boolean = false
): androidx.compose.runtime.State<Float> {
    val elevation = remember { Animatable(defaultElevation.value) }

    LaunchedEffect(isHovered) {
        elevation.animateTo(
            targetValue = if (isHovered) hoveredElevation.value else defaultElevation.value,
            animationSpec = tween(
                durationMillis = CardAnimationDurations.HOVER_ELEVATION,
                easing = CardAnimationEasings.Standard
            )
        )
    }

    return elevation as State<Float>
}

/**
 * 卡片出现动画（fadeIn + scaleIn）
 * 用于卡片首次显示时的动画效果
 */
@Composable
fun rememberCardAppearAnimation(
    delayMillis: Int = 0,
    onAnimationEnd: (() -> Unit)? = null
): CardAppearState {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.APPEAR_FADE,
                    easing = CardAnimationEasings.Standard
                )
            )
        }

        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        onAnimationEnd?.invoke()
    }

    return CardAppearState(alpha, scale)
}

/**
 * 卡片出现动画状态
 */
class CardAppearState(
    private val alpha: Animatable<Float, *>,
    private val scale: Animatable<Float, *>
) {
    val alphaValue: Float get() = alpha.value
    val scaleValue: Float get() = scale.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val a = alphaValue
        val s = scaleValue
        return modifier.graphicsLayer {
            this.alpha = a
            this.scaleX = s
            this.scaleY = s
        }
    }
}

/**
 * 带动画的卡片容器
 * 集成按压反馈和出现动画
 */
@Composable
fun AnimatedCardContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val appearState = rememberCardAppearAnimation(delayMillis)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            pressScale.animateTo(
                targetValue = 0.98f,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.PRESS_FEEDBACK,
                    easing = CardAnimationEasings.Standard
                )
            )
        } else {
            pressScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = appearState.alphaValue
                scaleX = appearState.scaleValue * pressScale.value
                scaleY = appearState.scaleValue * pressScale.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        content()
    }
}

/**
 * 卡片消失动画
 * 用于卡片删除时的动画效果
 */
@Composable
fun rememberCardDisappearAnimation(
    trigger: Boolean,
    onAnimationEnd: (() -> Unit)? = null
): CardDisappearState {
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = CardAnimationDurations.APPEAR_FADE,
                        easing = CardAnimationEasings.Standard
                    )
                )
            }

            launch {
                scale.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = CardAnimationDurations.APPEAR_SCALE,
                        easing = CardAnimationEasings.Standard
                    )
                )
            }

            launch {
                translationX.animateTo(
                    targetValue = -100f,
                    animationSpec = tween(
                        durationMillis = CardAnimationDurations.APPEAR_SCALE,
                        easing = CardAnimationEasings.Standard
                    )
                )
            }

            onAnimationEnd?.invoke()
        }
    }

    return CardDisappearState(alpha, scale, translationX)
}

/**
 * 卡片消失动画状态
 */
class CardDisappearState(
    private val alpha: Animatable<Float, *>,
    private val scale: Animatable<Float, *>,
    private val translationX: Animatable<Float, *>
) {
    val alphaValue: Float get() = alpha.value
    val scaleValue: Float get() = scale.value
    val translationXValue: Float get() = translationX.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val a = alphaValue
        val s = scaleValue
        val tx = translationXValue
        return modifier.graphicsLayer {
            this.alpha = a
            this.scaleX = s
            this.scaleY = s
            this.translationX = tx
        }
    }
}

/**
 * 卡片抖动动画
 * 用于错误提示或需要引起注意的场景
 */
@Composable
fun rememberCardShakeAnimation(
    trigger: Boolean,
    onAnimationEnd: (() -> Unit)? = null
): Animatable<Float, *> {
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            val shakeKeyframes = listOf(
                0f to 0f,
                0.1f to -10f,
                0.2f to 10f,
                0.3f to -10f,
                0.4f to 10f,
                0.5f to -5f,
                0.6f to 5f,
                0.7f to 0f
            )

            shakeKeyframes.forEach { (progress, offset) ->
                translationX.animateTo(
                    targetValue = offset,
                    animationSpec = tween(
                        durationMillis = 50,
                        easing = CardAnimationEasings.Standard
                    )
                )
            }

            onAnimationEnd?.invoke()
        }
    }

    return translationX
}

/**
 * 卡片翻转动画
 * 用于卡片正反面切换
 */
@Composable
fun rememberCardFlipAnimation(
    isFlipped: Boolean,
    durationMillis: Int = 400
): CardFlipState {
    val rotationY = remember { Animatable(0f) }
    val cameraDistance = remember { androidx.compose.runtime.mutableFloatStateOf(8f) }

    LaunchedEffect(isFlipped) {
        val targetRotation = if (isFlipped) 180f else 0f

        rotationY.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = CardAnimationEasings.Standard
            )
        )
    }

    return CardFlipState(rotationY, cameraDistance)
}

/**
 * 卡片翻转动画状态
 */
class CardFlipState(
    private val rotationY: Animatable<Float, *>,
    val cameraDistance: androidx.compose.runtime.MutableFloatState
) {
    val rotationYValue: Float get() = rotationY.value
    val isBackVisible: Boolean get() = rotationY.value > 90f

    fun applyToModifier(modifier: Modifier): Modifier {
        val ry = rotationYValue
        val cd = cameraDistance.value
        return modifier.graphicsLayer {
            this.rotationY = ry
            this.cameraDistance = cd
        }
    }
}

/**
 * 卡片堆叠动画
 * 用于卡片堆叠效果
 */
@Composable
fun rememberCardStackAnimation(
    index: Int,
    totalCards: Int
): CardStackState {
    val translationY = remember { Animatable(index * -8f) }
    val scale = remember { Animatable(1f - index * 0.05f) }
    val alpha = remember { Animatable(1f - index * 0.15f) }

    LaunchedEffect(index) {
        launch {
            translationY.animateTo(
                targetValue = index * -8f,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.ELEVATION_CHANGE,
                    easing = CardAnimationEasings.Standard
                )
            )
        }

        launch {
            scale.animateTo(
                targetValue = 1f - index * 0.05f,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.ELEVATION_CHANGE,
                    easing = CardAnimationEasings.Standard
                )
            )
        }

        launch {
            alpha.animateTo(
                targetValue = 1f - index * 0.15f,
                animationSpec = tween(
                    durationMillis = CardAnimationDurations.ELEVATION_CHANGE,
                    easing = CardAnimationEasings.Standard
                )
            )
        }
    }

    return CardStackState(translationY, scale, alpha)
}

/**
 * 卡片堆叠动画状态
 */
class CardStackState(
    private val translationY: Animatable<Float, *>,
    private val scale: Animatable<Float, *>,
    private val alpha: Animatable<Float, *>
) {
    val translationYValue: Float get() = translationY.value
    val scaleValue: Float get() = scale.value
    val alphaValue: Float get() = alpha.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val ty = translationYValue
        val s = scaleValue
        val a = alphaValue
        return modifier.graphicsLayer {
            this.translationY = ty
            this.scaleX = s
            this.scaleY = s
            this.alpha = a
        }
    }
}

/**
 * 卡片展开/收起动画
 * 用于可展开卡片
 */
@Composable
fun rememberCardExpandAnimation(
    isExpanded: Boolean,
    durationMillis: Int = 300
): CardExpandState {
    val expandProgress = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isExpanded) {
        launch {
            expandProgress.animateTo(
                targetValue = if (isExpanded) 1f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = CardAnimationEasings.Standard
                )
            )
        }

        launch {
            rotation.animateTo(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = CardAnimationEasings.Standard
                )
            )
        }
    }

    return CardExpandState(expandProgress, rotation)
}

/**
 * 卡片展开动画状态
 */
class CardExpandState(
    private val expandProgress: Animatable<Float, *>,
    private val rotation: Animatable<Float, *>
) {
    val expandProgressValue: Float get() = expandProgress.value
    val rotationValue: Float get() = rotation.value
    val isExpanded: Boolean get() = expandProgress.value > 0.5f
}
