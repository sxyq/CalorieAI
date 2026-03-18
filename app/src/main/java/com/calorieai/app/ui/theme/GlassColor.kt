package com.calorieai.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// Glass UI 毛玻璃主题颜色系统
// ============================================

// 毛玻璃透明度常量
object GlassAlpha {
    const val NAVIGATION_BAR = 0.95f      // 导航栏背景透明度
    const val CARD_BACKGROUND = 0.90f     // 卡片背景透明度
    const val OVERLAY_LIGHT = 0.08f       // 浅色遮罩
    const val OVERLAY_MEDIUM = 0.12f      // 中等遮罩
    const val OVERLAY_HEAVY = 0.24f       // 厚重遮罩
    const val BORDER_SUBTLE = 0.20f       // 边框透明度
}

// ============================================
// 浅色模式颜色 - Glass 风格
// ============================================

object GlassLightColors {
    // 导航栏背景 #F3EDF7 (95%透明)
    val NavigationBarBackground = Color(0xFFF3EDF7)

    // 指示器背景 #E8DEF8
    val IndicatorBackground = Color(0xFFE8DEF8)

    // 未选中图标/文字 #49454F
    val UnselectedIcon = Color(0xFF49454F)
    val UnselectedText = Color(0xFF49454F)

    // 选中图标/文字 #1D192B
    val SelectedIcon = Color(0xFF1D192B)
    val SelectedText = Color(0xFF1D192B)

    // 按下状态 #1D1B20
    val PressedState = Color(0xFF1D1B20)

    // 卡片背景 #FFFFFF (90%透明)
    val CardBackground = Color(0xFFFFFFFF)

    // 主色调 #6750A4
    val Primary = Color(0xFF6750A4)

    // 次色调 #625B71
    val Secondary = Color(0xFF625B71)

    // 派生颜色
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFEADDFF)
    val OnPrimaryContainer = Color(0xFF21005D)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondaryContainer = Color(0xFF1D192B)

    // 背景色
    val Background = Color(0xFFFEF7FF)
    val OnBackground = Color(0xFF1D1B20)

    // 表面色
    val Surface = Color(0xFFFEF7FF)
    val OnSurface = Color(0xFF1D1B20)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454F)

    // 表面容器色
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF7F2FA)
    val SurfaceContainer = Color(0xFFF3EDF7)
    val SurfaceContainerHigh = Color(0xFFECE6F0)
    val SurfaceContainerHighest = Color(0xFFE6E0E9)

    // 轮廓色
    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)

    // 第三色
    val Tertiary = Color(0xFF7D5260)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFFFD8E4)
    val OnTertiaryContainer = Color(0xFF31111D)

    // 错误色
    val Error = Color(0xFFB3261E)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)

    // 反色
    val InverseSurface = Color(0xFF322F35)
    val InverseOnSurface = Color(0xFFF5EFF7)
    val InversePrimary = Color(0xFFD0BCFF)

    // 遮罩色
    val Scrim = Color(0xFF000000)
}

// ============================================
// 深色模式颜色 - Glass 风格
// ============================================

object GlassDarkColors {
    // 导航栏背景 #211F26 (95%透明)
    val NavigationBarBackground = Color(0xFF211F26)

    // 指示器背景 #4A4458
    val IndicatorBackground = Color(0xFF4A4458)

    // 未选中图标/文字 #CAC4D0
    val UnselectedIcon = Color(0xFFCAC4D0)
    val UnselectedText = Color(0xFFCAC4D0)

    // 选中图标/文字 #E8DEF8
    val SelectedIcon = Color(0xFFE8DEF8)
    val SelectedText = Color(0xFFE8DEF8)

    // 按下状态 #E6E0E9
    val PressedState = Color(0xFFE6E0E9)

    // 卡片背景 #1C1B1F (90%透明)
    val CardBackground = Color(0xFF1C1B1F)

    // 主色调 #D0BCFF
    val Primary = Color(0xFFD0BCFF)

    // 次色调 #CCC2DC
    val Secondary = Color(0xFFCCC2DC)

    // 派生颜色
    val OnPrimary = Color(0xFF381E72)
    val PrimaryContainer = Color(0xFF4F378B)
    val OnPrimaryContainer = Color(0xFFEADDFF)
    val OnSecondary = Color(0xFF332D41)
    val SecondaryContainer = Color(0xFF4A4458)
    val OnSecondaryContainer = Color(0xFFE8DEF8)

    // 背景色
    val Background = Color(0xFF141218)
    val OnBackground = Color(0xFFE6E0E9)

    // 表面色
    val Surface = Color(0xFF141218)
    val OnSurface = Color(0xFFE6E0E9)
    val SurfaceVariant = Color(0xFF49454F)
    val OnSurfaceVariant = Color(0xFFCAC4D0)

    // 表面容器色
    val SurfaceContainerLowest = Color(0xFF0F0D13)
    val SurfaceContainerLow = Color(0xFF1D1B20)
    val SurfaceContainer = Color(0xFF211F26)
    val SurfaceContainerHigh = Color(0xFF2B2930)
    val SurfaceContainerHighest = Color(0xFF36343B)

    // 轮廓色
    val Outline = Color(0xFF938F99)
    val OutlineVariant = Color(0xFF49454F)

    // 第三色
    val Tertiary = Color(0xFFEFB8C8)
    val OnTertiary = Color(0xFF492532)
    val TertiaryContainer = Color(0xFF633B48)
    val OnTertiaryContainer = Color(0xFFFFD8E4)

    // 错误色
    val Error = Color(0xFFF2B8B5)
    val OnError = Color(0xFF601410)
    val ErrorContainer = Color(0xFF8C1D18)
    val OnErrorContainer = Color(0xFFF9DEDC)

    // 反色
    val InverseSurface = Color(0xFFE6E0E9)
    val InverseOnSurface = Color(0xFF322F35)
    val InversePrimary = Color(0xFF6750A4)

    // 遮罩色
    val Scrim = Color(0xFF000000)
}

// ============================================
// Glass 颜色扩展函数
// ============================================

fun Color.withGlassAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

// 获取带透明度的导航栏背景色
fun glassNavigationBarBackground(isDark: Boolean): Color {
    return if (isDark) {
        GlassDarkColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR)
    } else {
        GlassLightColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR)
    }
}

// 获取带透明度的卡片背景色
fun glassCardBackground(isDark: Boolean): Color {
    return if (isDark) {
        GlassDarkColors.CardBackground.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    } else {
        GlassLightColors.CardBackground.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    }
}
