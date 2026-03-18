package com.calorieai.app.ui.animation

import androidx.compose.animation.core.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

object AnimationEasing {
    val EaseOutCubic: Easing = CubicBezierEasing(0.33f, 0f, 0.2f, 1f)
    val EaseInCubic: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    val EaseInOutCubic: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
    
    val EaseOutQuart: Easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val EaseInQuart: Easing = CubicBezierEasing(0.5f, 0f, 1f, 1f)
    val EaseInOutQuart: Easing = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
    
    val EaseOutExpo: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseInExpo: Easing = CubicBezierEasing(0.7f, 0f, 0.84f, 0f)
    
    val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    val EaseInBack: Easing = CubicBezierEasing(0.6f, -0.28f, 0.735f, 0.045f)
    
    val EaseOutElastic: Easing = Easing { fraction ->
        val c4 = (2 * PI) / 3
        when {
            fraction == 0f -> 0f
            fraction == 1f -> 1f
            else -> {
                val value = 2.0.pow(10.0 * fraction - 10.0) * sin((fraction * 10.0 - 10.75) * c4)
                value.toFloat()
            }
        }
    }
    
    val EaseOutBounce: Easing = Easing { fraction ->
        val n1 = 7.5625f
        val d1 = 2.75f
        when {
            fraction < 1f / d1 -> n1 * fraction * fraction
            fraction < 2f / d1 -> {
                val t = fraction - 1.5f / d1
                n1 * t * t + 0.75f
            }
            fraction < 2.5f / d1 -> {
                val t = fraction - 2.25f / d1
                n1 * t * t + 0.9375f
            }
            else -> {
                val t = fraction - 2.625f / d1
                n1 * t * t + 0.984375f
            }
        }
    }
}

object AnimationSpecs {
    val Fast = tween<Float>(150, easing = AnimationEasing.EaseOutCubic)
    val Normal = tween<Float>(300, easing = AnimationEasing.EaseOutCubic)
    val Slow = tween<Float>(500, easing = AnimationEasing.EaseOutCubic)
    
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SpringStiff = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val SpringSoft = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessVeryLow
    )
    
    val Infinite = infiniteRepeatable<Float>(
        animation = tween(1000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
    
    val InfiniteReverse = infiniteRepeatable<Float>(
        animation = tween(1000, easing = AnimationEasing.EaseInOutCubic),
        repeatMode = RepeatMode.Reverse
    )
}

fun <T> tweenSpec(durationMs: Int = 300, easing: Easing = AnimationEasing.EaseOutCubic): TweenSpec<T> {
    return tween(durationMs, easing = easing)
}

fun <T> springSpec(dampingRatio: Float = Spring.DampingRatioNoBouncy, stiffness: Float = Spring.StiffnessMedium): SpringSpec<T> {
    return spring(dampingRatio = dampingRatio, stiffness = stiffness)
}
