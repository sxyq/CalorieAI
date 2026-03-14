package com.calorieai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.screens.settings.AppearanceSettingsViewModel
import com.calorieai.app.ui.screens.settings.WallpaperType

/**
 * 壁纸背景组件
 * 根据用户设置显示不同的背景（渐变、纯色、图片）
 */
@Composable
fun WallpaperBackground(
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundModifier = when (uiState.wallpaperType) {
        WallpaperType.GRADIENT -> {
            val startColor = uiState.wallpaperGradientStart?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            }
            val endColor = uiState.wallpaperGradientEnd?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            }

            if (startColor != null && endColor != null) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
            } else {
                // 默认渐变
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                )
            }
        }
        WallpaperType.SOLID -> {
            val color = uiState.wallpaperColor?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            } ?: Color(0xFF667eea)

            Modifier.background(color)
        }
        WallpaperType.IMAGE -> {
            // 图片壁纸暂不支持，使用默认渐变
            Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(backgroundModifier)
    ) {
        content()
    }
}

/**
 * 获取当前壁纸背景修饰符（用于非Compose函数）
 */
@Composable
fun getWallpaperBackgroundModifier(
    viewModel: AppearanceSettingsViewModel = hiltViewModel()
): Modifier {
    val uiState by viewModel.uiState.collectAsState()

    return when (uiState.wallpaperType) {
        WallpaperType.GRADIENT -> {
            val startColor = uiState.wallpaperGradientStart?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            }
            val endColor = uiState.wallpaperGradientEnd?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            }

            if (startColor != null && endColor != null) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
            } else {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                )
            }
        }
        WallpaperType.SOLID -> {
            val color = uiState.wallpaperColor?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    null
                }
            } ?: Color(0xFF667eea)

            Modifier.background(color)
        }
        WallpaperType.IMAGE -> {
            Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
        }
    }
}
