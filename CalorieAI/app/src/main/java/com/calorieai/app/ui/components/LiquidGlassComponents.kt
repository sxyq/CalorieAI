package com.calorieai.app.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android LiquidGlass Modifier 
 * 简化版：仅保留背景和边框，移除模糊效果
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color.White.copy(alpha = 0.15f),
    blurRadius: Float = 20f,
    borderAlpha: Float = 0.3f
): Modifier = composed {
    this
        .background(tint, shape)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    Color.White.copy(alpha = 0.0f),
                    Color.White.copy(alpha = borderAlpha * 0.5f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
}

/**
 * Interactive Scale 动效：缩放响应
 * @param pressedScale 按下时的缩放比例，默认 0.92f
 */
fun Modifier.interactiveScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.92f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_scale"
    )

    this.scale(scale)
}

/**
 * Gooey 容器，简化版：移除模糊效果
 */
@Composable
fun GlassGooeyContainer(
    modifier: Modifier = Modifier,
    blurRadius: Float = 30f,
    alphaThreshold: Float = 18f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        content = content
    )
}
