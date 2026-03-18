package com.calorieai.app.ui.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry
import com.calorieai.app.ui.animation.AnimationEasing
import com.calorieai.app.ui.animation.AnimationSpecs

/**
 * Glass 主题导航动画时长和缓动曲线
 */
object NavigationDurations {
    const val INDICATOR_APPEAR = 300
    const val INDICATOR_SCALE = 250
    const val ICON_TEXT_COLOR = 200
    const val RIPPLE_EXPAND = 400
    const val ICON_BOUNCE = 150
    const val PAGE_TRANSITION = 300
    const val GESTURE_RETURN = 250
}

/**
 * 页面切换滑动动画 - 水平滑动
 * 用于导航页面之间的切换
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        ),
        initialOffsetX = { it }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        ),
        targetOffsetX = { -it / 3 }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromLeft(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        ),
        initialOffsetX = { -it }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToRight(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        ),
        targetOffsetX = { it }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = NavigationDurations.PAGE_TRANSITION,
            easing = AnimationEasing.EaseOutCubic
        )
    )
}

/**
 * 共享元素过渡配置
 */
object SharedElementTransitions {
    const val BOUNDS_KEY = "bounds"
    const val IMAGE_KEY = "image"
    const val TEXT_KEY = "text"
    const val CARD_KEY = "card"
}

/**
 * 手势返回支持配置
 */
object GestureReturnConfig {
    // 手势返回阈值
    const val GESTURE_THRESHOLD = 0.3f

    // 手势返回速度阈值
    const val VELOCITY_THRESHOLD = 100f

    /**
     * 手势返回动画规格
     */
    fun <T> gestureAnimationSpec() = AnimationSpecs.SpringBouncy
}

/**
 * 导航指示器动画
 * 用于底部导航栏指示器的出现和消失
 */
@Composable
fun rememberIndicatorAnimation(
    selected: Boolean,
    onAnimationEnd: (() -> Unit)? = null
): androidx.compose.animation.core.Animatable<Float, *> {
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = NavigationDurations.INDICATOR_SCALE,
                    easing = AnimationEasing.EaseOutBack
                )
            )
        } else {
            scale.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = NavigationDurations.INDICATOR_APPEAR,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }
        onAnimationEnd?.invoke()
    }

    return scale
}

/**
 * 图标弹跳动画
 * 用于导航图标点击时的弹跳效果
 */
@Composable
fun rememberIconBounceAnimation(
    trigger: Boolean,
    onAnimationEnd: (() -> Unit)? = null
): androidx.compose.animation.core.Animatable<Float, *> {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            scale.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(
                    durationMillis = NavigationDurations.ICON_BOUNCE / 2,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = NavigationDurations.ICON_BOUNCE,
                    easing = AnimationEasing.EaseOutBack
                )
            )
            onAnimationEnd?.invoke()
        }
    }

    return scale
}

/**
 * 涟漪扩散动画修饰符
 */
fun Modifier.rippleAnimation(
    progress: Float,
    maxRadius: Float
): Modifier = this.graphicsLayer {
    alpha = 1f - progress
    scaleX = 1f + progress * 0.5f
    scaleY = 1f + progress * 0.5f
}

/**
 * 页面过渡动画组合
 * 用于 NavHost 的 enterTransition 和 exitTransition
 */
object PageTransitions {
    val enterFromRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInFromRight()
    }

    val exitToLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutToLeft()
    }

    val enterFromLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInFromLeft()
    }

    val exitToRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutToRight()
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInFromLeft()
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutToRight()
    }
}

/**
 * 淡入淡出过渡
 * 用于不需要滑动效果的页面切换
 */
object FadeTransitions {
    val fadeIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(
            animationSpec = AnimationSpecs.Normal
        ) + scaleIn(
            animationSpec = AnimationSpecs.Normal,
            initialScale = 0.95f
        )
    }

    val fadeOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(
            animationSpec = AnimationSpecs.Normal
        ) + scaleOut(
            animationSpec = AnimationSpecs.Normal,
            targetScale = 1.05f
        )
    }
}
