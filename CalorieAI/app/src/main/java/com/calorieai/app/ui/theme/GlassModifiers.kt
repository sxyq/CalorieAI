package com.calorieai.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.GlassUtils.BlurRadius
import com.calorieai.app.ui.theme.GlassUtils.BorderWidth

// ============================================
// 统一毛玻璃背景修饰符
// ============================================

/**
 * 统一的毛玻璃背景修饰符
 * @param backgroundColor 背景颜色
 * @param blurRadius 模糊半径
 * @param cornerRadius 圆角半径
 * @param borderAlpha 边框透明度
 * @param backgroundAlpha 背景透明度
 */
@Composable
fun Modifier.glass(
    backgroundColor: Color,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    borderAlpha: Float = GlassAlpha.BORDER_SUBTLE,
    backgroundAlpha: Float = GlassAlpha.CARD_BACKGROUND
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val shape = RoundedCornerShape(cornerRadius)
    val actualBackgroundColor = backgroundColor.copy(alpha = backgroundAlpha)
    val actualBorderColor = Color.White.copy(alpha = borderAlpha)

    if (supportsBlur && !isLowEnd) {
        this
            .background(actualBackgroundColor, shape)
            .glassBlur(blurRadius)
            .border(BorderWidth.THIN, actualBorderColor, shape)
    } else {
        this
            .background(actualBackgroundColor, shape)
            .border(BorderWidth.THIN, actualBorderColor.copy(alpha = borderAlpha * 0.5f), shape)
    }
}

/**
 * 主题感知的毛玻璃背景
 * @param isDark 是否为深色主题
 * @param blurRadius 模糊半径
 * @param cornerRadius 圆角半径
 * @param backgroundAlpha 背景透明度
 */
@Composable
fun Modifier.glassThemed(
    isDark: Boolean,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    backgroundAlpha: Float = GlassAlpha.CARD_BACKGROUND
): Modifier {
    val backgroundColor = if (isDark) GlassDarkColors.CardBackground else GlassLightColors.CardBackground
    return glass(
        backgroundColor = backgroundColor,
        blurRadius = blurRadius,
        cornerRadius = cornerRadius,
        backgroundAlpha = backgroundAlpha
    )
}

// ============================================
// 导航栏专用修饰符
// ============================================

/**
 * 导航栏专用毛玻璃修饰符
 * @param isDark 是否为深色主题
 * @param blurRadius 模糊半径
 * @param cornerRadius 圆角半径
 * @param addTopHighlight 是否添加顶部高光
 */
@Composable
fun Modifier.glassNavBar(
    isDark: Boolean,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.XLARGE,
    addTopHighlight: Boolean = true
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.NavigationBarBackground
    } else {
        GlassLightColors.NavigationBarBackground
    }

    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val actualBackgroundColor = backgroundColor.copy(alpha = GlassAlpha.NAVIGATION_BAR)

    var modifier = this

    if (supportsBlur && !isLowEnd) {
        modifier = modifier
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(actualBackgroundColor)
            .glassBlur(blurRadius)
    } else {
        modifier = modifier
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(actualBackgroundColor)
    }

    if (addTopHighlight) {
        modifier = modifier.topHighlight(height = 1.dp)
    }

    modifier
}

/**
 * 底部导航栏专用修饰符（带 navigationBarsPadding）
 */
@Composable
fun Modifier.glassBottomNavBar(
    isDark: Boolean,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.XLARGE
): Modifier = this
    .glassNavBar(isDark = isDark, blurRadius = blurRadius, cornerRadius = cornerRadius)
    .navigationBarsPadding()

/**
 * 顶部导航栏专用修饰符
 */
@Composable
fun Modifier.glassTopBar(
    isDark: Boolean,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.ZERO
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.NavigationBarBackground
    } else {
        GlassLightColors.NavigationBarBackground
    }

    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val actualBackgroundColor = backgroundColor.copy(alpha = GlassAlpha.NAVIGATION_BAR)

    var modifier = this

    if (supportsBlur && !isLowEnd) {
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
            .background(actualBackgroundColor)
            .glassBlur(blurRadius)
    } else {
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
            .background(actualBackgroundColor)
    }

    // 顶部高光
    val density = LocalDensity.current
    modifier = modifier.drawBehind {
        val heightPx = with(density) { 1.dp.toPx() }
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = heightPx
            )
        )
    }

    modifier
}

// ============================================
// 卡片专用修饰符
// ============================================

/**
 * 卡片专用毛玻璃修饰符
 * @param isDark 是否为深色主题
 * @param blurRadius 模糊半径 (默认15dp)
 * @param cornerRadius 圆角半径
 * @param padding 内边距
 * @param addTopHighlight 是否添加顶部高光
 */
@Composable
fun Modifier.glassCardModifier(
    isDark: Boolean,
    blurRadius: Dp = BlurRadius.CARD,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    padding: PaddingValues = PaddingValues(16.dp),
    addTopHighlight: Boolean = true
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.CardBackground
    } else {
        GlassLightColors.CardBackground
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val shape = RoundedCornerShape(cornerRadius)
    val actualBackgroundColor = backgroundColor.copy(alpha = GlassAlpha.CARD_BACKGROUND)

    var modifier = this

    if (supportsBlur && !isLowEnd) {
        modifier = modifier
            .background(actualBackgroundColor, shape)
            .glassBlur(blurRadius)
    } else {
        modifier = modifier.background(actualBackgroundColor, shape)
    }

    modifier = modifier
        .border(BorderWidth.THIN, borderColor, shape)
        .padding(padding)

    if (addTopHighlight) {
        modifier = modifier.topHighlight()
    }

    modifier
}

/**
 * 大卡片毛玻璃修饰符（20dp模糊）
 */
@Composable
fun Modifier.glassLargeCard(
    isDark: Boolean,
    cornerRadius: Dp = GlassUtils.CornerRadius.LARGE,
    padding: PaddingValues = PaddingValues(16.dp)
): Modifier = glassCardModifier(
    isDark = isDark,
    blurRadius = BlurRadius.CARD_LARGE,
    cornerRadius = cornerRadius,
    padding = padding
)

/**
 * 全宽毛玻璃卡片
 */
@Composable
fun Modifier.glassFullWidthCard(
    isDark: Boolean,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    padding: PaddingValues = PaddingValues(16.dp)
): Modifier = glassCardModifier(
    isDark = isDark,
    cornerRadius = cornerRadius,
    padding = padding
).fillMaxWidth()

// ============================================
// 通用毛玻璃容器
// ============================================

/**
 * 通用毛玻璃容器 Box
 * @param isDark 是否为深色主题
 * @param blurRadius 模糊半径
 * @param cornerRadius 圆角半径
 * @param backgroundAlpha 背景透明度
 */
@Composable
fun GlassBox(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    blurRadius: Dp = BlurRadius.MEDIUM,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    backgroundAlpha: Float = GlassAlpha.OVERLAY_MEDIUM,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (isDark) GlassDarkColors.CardBackground else GlassLightColors.CardBackground

    Box(
        modifier = modifier.glass(
            backgroundColor = backgroundColor,
            blurRadius = blurRadius,
            cornerRadius = cornerRadius,
            backgroundAlpha = backgroundAlpha
        )
    ) {
        content()
    }
}

// ============================================
// 特殊效果修饰符
// ============================================

/**
 * 悬浮效果（带微弱模糊和阴影）
 */
@Composable
fun Modifier.glassFloating(
    isDark: Boolean,
    cornerRadius: Dp = GlassUtils.CornerRadius.LARGE
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.CardBackground
    } else {
        GlassLightColors.CardBackground
    }

    this
        .background(
            color = backgroundColor.copy(alpha = 0.95f),
            shape = RoundedCornerShape(cornerRadius)
        )
        .border(
            width = BorderWidth.THIN,
            color = Color.White.copy(alpha = if (isDark) 0.1f else 0.3f),
            shape = RoundedCornerShape(cornerRadius)
        )
}

/**
 * 遮罩层效果
 */
@Composable
fun Modifier.glassOverlay(
    blurRadius: Dp = BlurRadius.SMALL
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    if (supportsBlur && !isLowEnd) {
        this.glassBlur(blurRadius)
    } else {
        this
    }
}

/**
 * 弹窗/对话框毛玻璃背景
 */
@Composable
fun Modifier.glassDialog(
    isDark: Boolean,
    cornerRadius: Dp = GlassUtils.CornerRadius.XLARGE
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh
    } else {
        GlassLightColors.SurfaceContainerHigh
    }

    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val shape = RoundedCornerShape(cornerRadius)
    val actualBackgroundColor = backgroundColor.copy(alpha = 0.95f)

    if (supportsBlur && !isLowEnd) {
        this
            .background(actualBackgroundColor, shape)
            .glassBlur(BlurRadius.LARGE)
            .border(BorderWidth.THIN, Color.White.copy(alpha = 0.15f), shape)
    } else {
        this
            .background(actualBackgroundColor, shape)
            .border(BorderWidth.THIN, Color.White.copy(alpha = 0.1f), shape)
    }
}
