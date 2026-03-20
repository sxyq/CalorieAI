package com.calorieai.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

// ============================================
// Glass 主题颜色方案
// ============================================

/**
 * Glass 浅色主题配色方案 - 毛玻璃风格
 * 使用紫色系主色调，配合毛玻璃效果
 */
private val GlassLightColorScheme = lightColorScheme(
    // 主色
    primary = GlassLightColors.Primary,
    onPrimary = GlassLightColors.OnPrimary,
    primaryContainer = GlassLightColors.PrimaryContainer,
    onPrimaryContainer = GlassLightColors.OnPrimaryContainer,

    // 次要色
    secondary = GlassLightColors.Secondary,
    onSecondary = GlassLightColors.OnSecondary,
    secondaryContainer = GlassLightColors.SecondaryContainer,
    onSecondaryContainer = GlassLightColors.OnSecondaryContainer,

    // 第三色
    tertiary = GlassLightColors.Tertiary,
    onTertiary = GlassLightColors.OnTertiary,
    tertiaryContainer = GlassLightColors.TertiaryContainer,
    onTertiaryContainer = GlassLightColors.OnTertiaryContainer,

    // 错误色
    error = GlassLightColors.Error,
    onError = GlassLightColors.OnError,
    errorContainer = GlassLightColors.ErrorContainer,
    onErrorContainer = GlassLightColors.OnErrorContainer,

    // 背景色
    background = GlassLightColors.Background,
    onBackground = GlassLightColors.OnBackground,

    // 表面色
    surface = GlassLightColors.Surface,
    onSurface = GlassLightColors.OnSurface,
    surfaceVariant = GlassLightColors.SurfaceVariant,
    onSurfaceVariant = GlassLightColors.OnSurfaceVariant,

    // 表面容器色（Material3新增）
    surfaceContainerLowest = GlassLightColors.SurfaceContainerLowest,
    surfaceContainerLow = GlassLightColors.SurfaceContainerLow,
    surfaceContainer = GlassLightColors.SurfaceContainer,
    surfaceContainerHigh = GlassLightColors.SurfaceContainerHigh,
    surfaceContainerHighest = GlassLightColors.SurfaceContainerHighest,

    // 轮廓色
    outline = GlassLightColors.Outline,
    outlineVariant = GlassLightColors.OutlineVariant,

    // 反色
    inverseSurface = GlassLightColors.InverseSurface,
    inverseOnSurface = GlassLightColors.InverseOnSurface,
    inversePrimary = GlassLightColors.InversePrimary,

    // 遮罩色
    scrim = GlassLightColors.Scrim
)

/**
 * Glass 深色主题配色方案 - 毛玻璃风格
 */
private val GlassDarkColorScheme = darkColorScheme(
    // 主色
    primary = GlassDarkColors.Primary,
    onPrimary = GlassDarkColors.OnPrimary,
    primaryContainer = GlassDarkColors.PrimaryContainer,
    onPrimaryContainer = GlassDarkColors.OnPrimaryContainer,

    // 次要色
    secondary = GlassDarkColors.Secondary,
    onSecondary = GlassDarkColors.OnSecondary,
    secondaryContainer = GlassDarkColors.SecondaryContainer,
    onSecondaryContainer = GlassDarkColors.OnSecondaryContainer,

    // 第三色
    tertiary = GlassDarkColors.Tertiary,
    onTertiary = GlassDarkColors.OnTertiary,
    tertiaryContainer = GlassDarkColors.TertiaryContainer,
    onTertiaryContainer = GlassDarkColors.OnTertiaryContainer,

    // 错误色
    error = GlassDarkColors.Error,
    onError = GlassDarkColors.OnError,
    errorContainer = GlassDarkColors.ErrorContainer,
    onErrorContainer = GlassDarkColors.OnErrorContainer,

    // 背景色
    background = GlassDarkColors.Background,
    onBackground = GlassDarkColors.OnBackground,

    // 表面色
    surface = GlassDarkColors.Surface,
    onSurface = GlassDarkColors.OnSurface,
    surfaceVariant = GlassDarkColors.SurfaceVariant,
    onSurfaceVariant = GlassDarkColors.OnSurfaceVariant,

    // 表面容器色
    surfaceContainerLowest = GlassDarkColors.SurfaceContainerLowest,
    surfaceContainerLow = GlassDarkColors.SurfaceContainerLow,
    surfaceContainer = GlassDarkColors.SurfaceContainer,
    surfaceContainerHigh = GlassDarkColors.SurfaceContainerHigh,
    surfaceContainerHighest = GlassDarkColors.SurfaceContainerHighest,

    // 轮廓色
    outline = GlassDarkColors.Outline,
    outlineVariant = GlassDarkColors.OutlineVariant,

    // 反色
    inverseSurface = GlassDarkColors.InverseSurface,
    inverseOnSurface = GlassDarkColors.InverseOnSurface,
    inversePrimary = GlassDarkColors.InversePrimary,

    // 遮罩色
    scrim = GlassDarkColors.Scrim
)

/**
 * 传统浅色主题配色方案 - 蓝色主色调
 * 参考Deadliner风格，使用Material3 Expressive设计
 */
private val LightColorScheme = lightColorScheme(
    // 主色
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,

    // 次要色
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,

    // 第三色
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,

    // 错误色
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    // 背景色
    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    // 表面色
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,

    // 表面容器色（Material3新增）
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,

    // 轮廓色
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,

    // 反色
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,

    // 遮罩色
    scrim = Color(0xFF000000)
)

/**
 * 深色主题配色方案 - 蓝色主色调
 */
private val DarkColorScheme = darkColorScheme(
    // 主色
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,

    // 次要色
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,

    // 第三色
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,

    // 错误色
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    // 背景色
    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    // 表面色
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    // 表面容器色
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,

    // 轮廓色
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,

    // 反色
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,

    // 遮罩色
    scrim = Color(0xFF000000)
)

// ============================================
// Glass 主题扩展数据类
// ============================================

/**
 * Glass 主题专用颜色
 * 包含毛玻璃效果相关的额外颜色定义
 */
data class GlassColors(
    // 导航栏背景（带透明度）
    val navigationBarBackground: Color,
    // 指示器背景
    val indicatorBackground: Color,
    // 未选中状态
    val unselectedIcon: Color,
    val unselectedText: Color,
    // 选中状态
    val selectedIcon: Color,
    val selectedText: Color,
    // 按下状态
    val pressedState: Color,
    // 卡片背景（带透明度）
    val cardBackground: Color
)

/**
 * CompositionLocal 用于提供 Glass 主题颜色
 */
val LocalGlassColors = staticCompositionLocalOf {
    GlassColors(
        navigationBarBackground = GlassLightColors.NavigationBarBackground,
        indicatorBackground = GlassLightColors.IndicatorBackground,
        unselectedIcon = GlassLightColors.UnselectedIcon,
        unselectedText = GlassLightColors.UnselectedText,
        selectedIcon = GlassLightColors.SelectedIcon,
        selectedText = GlassLightColors.SelectedText,
        pressedState = GlassLightColors.PressedState,
        cardBackground = GlassLightColors.CardBackground
    )
}

/**
 * 获取当前主题的 Glass 颜色
 */
object GlassThemeColors {
    val current: GlassColors
        @Composable
        get() = LocalGlassColors.current
}

/**
 * CalorieAI主题
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否使用动态颜色（Android 12+），默认关闭以保持品牌色
 * @param content 内容 composable
 */
@Composable
fun CalorieAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // 默认关闭动态颜色，保持蓝色品牌色
    backgroundOverride: Color? = null,
    wallpaperEnabled: Boolean = false,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        // 动态颜色仅在Android 12+且用户开启时生效
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val themedColorScheme = if (backgroundOverride != null) {
        baseColorScheme.copy(
            background = backgroundOverride,
            surface = lerp(baseColorScheme.surface, backgroundOverride, 0.18f),
            surfaceVariant = lerp(baseColorScheme.surfaceVariant, backgroundOverride, 0.1f)
        )
    } else {
        baseColorScheme
    }

    val colorScheme = if (wallpaperEnabled) {
        val surfaceAlpha = if (darkTheme) 0.72f else 0.78f
        val containerAlpha = if (darkTheme) 0.68f else 0.74f
        themedColorScheme.copy(
            background = Color.Transparent,
            surface = themedColorScheme.surface.copy(alpha = surfaceAlpha),
            surfaceVariant = themedColorScheme.surfaceVariant.copy(alpha = containerAlpha),
            surfaceContainerLowest = themedColorScheme.surfaceContainerLowest.copy(alpha = containerAlpha),
            surfaceContainerLow = themedColorScheme.surfaceContainerLow.copy(alpha = containerAlpha),
            surfaceContainer = themedColorScheme.surfaceContainer.copy(alpha = containerAlpha),
            surfaceContainerHigh = themedColorScheme.surfaceContainerHigh.copy(alpha = containerAlpha),
            surfaceContainerHighest = themedColorScheme.surfaceContainerHighest.copy(alpha = containerAlpha)
        )
    } else {
        themedColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 浅色主题固定白色系统栏，深色主题使用深色表面层级，提升可读性与一致性。
            val systemBarColor = if (darkTheme) {
                Color.Black.toArgb()
            } else {
                Color.White.toArgb()
            }
            window.statusBarColor = systemBarColor

            // 导航栏使用与状态栏一致的表面色
            window.navigationBarColor = systemBarColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 关闭系统自动对比度着色，避免浅色模式下导航栏被系统改成灰色。
                window.isNavigationBarContrastEnforced = false
            }

            // 根据主题设置状态栏文字颜色
            // 浅色主题：黑色文字
            // 深色主题：白色文字
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val baseDensity = LocalDensity.current
    val appliedFontScale = fontScale.coerceIn(0.85f, 1.25f)
    val scaledDensity = remember(baseDensity, appliedFontScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * appliedFontScale
        )
    }

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Glass 毛玻璃主题
 * 提供毛玻璃效果的配色方案
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否使用动态颜色（Android 12+），默认关闭
 * @param content 内容 composable
 */
@Composable
fun GlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        darkTheme -> GlassDarkColorScheme
        else -> GlassLightColorScheme
    }

    val glassColors = if (darkTheme) {
        GlassColors(
            navigationBarBackground = GlassDarkColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR),
            indicatorBackground = GlassDarkColors.IndicatorBackground,
            unselectedIcon = GlassDarkColors.UnselectedIcon,
            unselectedText = GlassDarkColors.UnselectedText,
            selectedIcon = GlassDarkColors.SelectedIcon,
            selectedText = GlassDarkColors.SelectedText,
            pressedState = GlassDarkColors.PressedState,
            cardBackground = GlassDarkColors.CardBackground.copy(alpha = GlassAlpha.CARD_BACKGROUND)
        )
    } else {
        GlassColors(
            navigationBarBackground = GlassLightColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR),
            indicatorBackground = GlassLightColors.IndicatorBackground,
            unselectedIcon = GlassLightColors.UnselectedIcon,
            unselectedText = GlassLightColors.UnselectedText,
            selectedIcon = GlassLightColors.SelectedIcon,
            selectedText = GlassLightColors.SelectedText,
            pressedState = GlassLightColors.PressedState,
            cardBackground = GlassLightColors.CardBackground.copy(alpha = GlassAlpha.CARD_BACKGROUND)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 状态栏使用表面色
            window.statusBarColor = colorScheme.surface.toArgb()

            // 导航栏使用Glass导航栏背景色
            window.navigationBarColor = glassColors.navigationBarBackground.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            // 根据主题设置状态栏文字颜色
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * 扩展属性：获取当前主题的图表颜色
 */
object ChartColors {
    val success @Composable get() = ChartGreen
    val warning @Composable get() = ChartOrange
    val error @Composable get() = ChartRed
    val info @Composable get() = ChartBlue
    val special @Composable get() = ChartPurple
}
