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
 * 应用液态玻璃效果：高斯模糊 + 半透明着色 + 边缘高光反射
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color.White.copy(alpha = 0.25f),
    blurRadius: Float = 40f,
    borderAlpha: Float = 0.4f
): Modifier = composed {
    this
        .graphicsLayer {
            clip = true
            this.shape = shape
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                renderEffect = RenderEffect.createBlurEffect(
                    blurRadius, 
                    blurRadius, 
                    Shader.TileMode.DECAL
                ).asComposeRenderEffect()
            }
        }
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
 */
fun Modifier.interactiveScale(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_scale"
    )

    this.scale(scale)
}

/**
 * Gooey 容器，融合内部的玻璃组件
 */
@Composable
fun GlassGooeyContainer(
    modifier: Modifier = Modifier,
    blurRadius: Float = 30f,
    alphaThreshold: Float = 18f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blur = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.DECAL)
                val colorMatrix = ColorMatrix(
                    floatArrayOf(
                        1f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, alphaThreshold, -255f * 7f 
                    )
                )
                val colorMatrixEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(colorMatrix))
                renderEffect = RenderEffect.createChainEffect(colorMatrixEffect, blur).asComposeRenderEffect()
            }
        },
        content = content
    )
}
