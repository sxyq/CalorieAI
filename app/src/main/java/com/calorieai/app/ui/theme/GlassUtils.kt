package com.calorieai.app.ui.theme

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================
// 设备性能检测工具
// ============================================

object GlassDeviceUtils {

    private const val LOW_END_MEMORY_THRESHOLD = 2048

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("sdk") ||
            brand.startsWith("generic") ||
            device.startsWith("generic") ||
            product.contains("sdk")
    }

    fun isLowEndDevice(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem / (1024 * 1024)
        return isEmulator() ||
                totalMemory < LOW_END_MEMORY_THRESHOLD ||
                activityManager.isLowRamDevice ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    }

    fun supportsBlur(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}

// ============================================
// 毛玻璃效果工具类
// ============================================

object GlassUtils {

    object BlurRadius {
        val SMALL = 10.dp
        val MEDIUM = 20.dp
        val LARGE = 30.dp
        val CARD = 15.dp
        val CARD_LARGE = 20.dp
    }

    object CornerRadius {
        val SMALL = 8.dp
        val MEDIUM = 12.dp
        val LARGE = 16.dp
        val XLARGE = 24.dp
        val ZERO = 0.dp
    }

    object BorderWidth {
        val THIN = 0.5.dp
        val NORMAL = 1.dp
        val THICK = 2.dp
    }
}

// ============================================
// Modifier 扩展函数 - 模糊效果
// ============================================

fun Modifier.glassBlur(
    radius: Dp = GlassUtils.BlurRadius.MEDIUM,
    enabled: Boolean = true
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    if (enabled && supportsBlur && !isLowEnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.then(
            blur(radius)
        )
    } else {
        this
    }
}

fun Modifier.glassCard(
    backgroundColor: Color = Color.White,
    blurRadius: Dp = GlassUtils.BlurRadius.CARD,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = GlassUtils.BorderWidth.THIN,
    alpha: Float = GlassAlpha.CARD_BACKGROUND
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val shape = RoundedCornerShape(cornerRadius)
    val actualBackgroundColor = backgroundColor.copy(alpha = alpha)

    this.then(
        if (supportsBlur && !isLowEnd) {
            Modifier
                .background(actualBackgroundColor, shape)
                .glassBlur(blurRadius)
                .border(borderWidth, borderColor, shape)
        } else {
            Modifier
                .background(actualBackgroundColor, shape)
                .border(borderWidth, borderColor.copy(alpha = 0.1f), shape)
        }
    )
}

fun Modifier.topHighlight(
    highlightColor: Color = Color.White.copy(alpha = 0.2f),
    height: Dp = 1.dp
): Modifier = composed {
    val density = LocalDensity.current
    val heightPx = with(density) { height.toPx() }
    this.then(
        Modifier.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(highlightColor, Color.Transparent),
                    startY = 0f,
                    endY = heightPx
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, heightPx)
            )
        }
    )
}

fun Modifier.glassBackground(
    backgroundColor: Color = Color.White,
    blurRadius: Dp = GlassUtils.BlurRadius.MEDIUM,
    alpha: Float = GlassAlpha.OVERLAY_MEDIUM
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val actualBackgroundColor = backgroundColor.copy(alpha = alpha)

    this.then(
        if (supportsBlur && !isLowEnd) {
            Modifier
                .background(actualBackgroundColor)
                .glassBlur(blurRadius)
        } else {
            Modifier.background(actualBackgroundColor)
        }
    )
}

@Composable
fun Modifier.glassCardThemed(
    isDark: Boolean = false,
    blurRadius: Dp = GlassUtils.BlurRadius.CARD,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    useTopHighlight: Boolean = true
): Modifier = composed {
    val backgroundColor = if (isDark) {
        GlassDarkColors.CardBackground
    } else {
        GlassLightColors.CardBackground
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.3f)
    }

    var modifier = this.glassCard(
        backgroundColor = backgroundColor,
        blurRadius = blurRadius,
        cornerRadius = cornerRadius,
        borderColor = borderColor
    )

    if (useTopHighlight) {
        modifier = modifier.topHighlight()
    }

    modifier
}

@Composable
fun Modifier.glassNavigationBar(
    isDark: Boolean = false,
    blurRadius: Dp = GlassUtils.BlurRadius.MEDIUM
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

    var modifier: Modifier = this

    modifier = if (supportsBlur && !isLowEnd) {
        modifier
            .background(actualBackgroundColor)
            .glassBlur(blurRadius)
    } else {
        modifier.background(actualBackgroundColor)
    }

    modifier.topHighlight()
}

fun Modifier.glassEffect(
    backgroundColor: Color = Color.White,
    blurRadius: Dp = GlassUtils.BlurRadius.CARD,
    cornerRadius: Dp = GlassUtils.CornerRadius.MEDIUM,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = GlassUtils.BorderWidth.THIN,
    alpha: Float = GlassAlpha.CARD_BACKGROUND,
    useTopHighlight: Boolean = true
): Modifier = composed {
    var modifier = this.glassCard(
        backgroundColor = backgroundColor,
        blurRadius = blurRadius,
        cornerRadius = cornerRadius,
        borderColor = borderColor,
        borderWidth = borderWidth,
        alpha = alpha
    )

    if (useTopHighlight) {
        modifier = modifier.topHighlight()
    }

    modifier
}

fun Modifier.glassLight(
    backgroundColor: Color = Color.White,
    alpha: Float = GlassAlpha.OVERLAY_LIGHT
): Modifier = composed {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }

    if (isLowEnd) {
        this.background(backgroundColor.copy(alpha = alpha))
    } else {
        this
            .background(backgroundColor.copy(alpha = alpha))
            .glassBlur(GlassUtils.BlurRadius.SMALL)
    }
}
