package com.calorieai.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 浅色主题配色方案 - 蓝色主色调
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
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // 状态栏使用表面色（白色/黑色）而非主题色，参考Deadliner风格
            window.statusBarColor = colorScheme.surface.toArgb()
            
            // 导航栏使用表面色
            window.navigationBarColor = colorScheme.surface.toArgb()
            
            // 根据主题设置状态栏文字颜色
            // 浅色主题：黑色文字
            // 深色主题：白色文字
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
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
