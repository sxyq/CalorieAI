package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.notification.NotificationScheduler
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.onboarding.OnboardingFlow
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var onboardingDataStore: OnboardingDataStore

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by userSettingsRepository.getSettings()
                .collectAsState(initial = null)

            val themeMode = settings?.themeMode ?: ThemeMode.SYSTEM.name
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT.name -> false
                ThemeMode.DARK.name -> true
                else -> isSystemInDarkTheme()
            }
            val wallpaperEnabled = themeMode == ThemeMode.SYSTEM.name
            val backgroundOverride = remember(
                wallpaperEnabled,
                settings?.wallpaperType,
                settings?.wallpaperColor,
                settings?.wallpaperGradientStart,
                settings?.wallpaperGradientEnd
            ) {
                if (!wallpaperEnabled) {
                    null
                } else {
                    when (settings?.wallpaperType) {
                        "SOLID" -> parseHexColor(settings?.wallpaperColor)
                        "GRADIENT" -> {
                            // 渐变壁纸由全局背景层直接绘制，不再用中间色覆盖主题，
                            // 避免被主题表面色“抹平”为纯色观感。
                            null
                        }

                        else -> null
                    }
                }
            }
            val appFontScale = remember(settings?.fontSize) {
                when (settings?.fontSize) {
                    "SMALL" -> 0.92f
                    "LARGE" -> 1.1f
                    else -> 1f
                }
            }

            var shouldSkipOnboarding by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                val completed = withContext(Dispatchers.IO) {
                    onboardingDataStore.isOnboardingCompleted.first()
                }
                shouldSkipOnboarding = completed
                isLoading = false
            }

            LaunchedEffect(
                isLoading,
                shouldSkipOnboarding,
                settings?.isNotificationEnabled,
                settings?.breakfastReminderTime,
                settings?.lunchReminderTime,
                settings?.dinnerReminderTime
            ) {
                val currentSettings = settings ?: return@LaunchedEffect
                if (isLoading || !shouldSkipOnboarding) return@LaunchedEffect

                // 避免在首屏渲染关键路径触发 WorkManager 初始化，降低冷启动卡顿。
                delay(900)
                withContext(Dispatchers.IO) {
                    notificationScheduler.syncMealReminders(currentSettings)
                }
            }

            CalorieAITheme(
                darkTheme = darkTheme,
                backgroundOverride = backgroundOverride,
                wallpaperEnabled = wallpaperEnabled,
                fontScale = appFontScale
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (wallpaperEnabled) {
                        AppWallpaperLayer(
                            wallpaperType = settings?.wallpaperType,
                            wallpaperColor = settings?.wallpaperColor,
                            wallpaperGradientStart = settings?.wallpaperGradientStart,
                            wallpaperGradientEnd = settings?.wallpaperGradientEnd,
                            wallpaperImageUri = settings?.wallpaperImageUri
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        color = if (wallpaperEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.background
                        }
                    ) {
                        if (isLoading) {
                            // 显示壁纸背景，避免闪烁
                        } else if (shouldSkipOnboarding) {
                            val navController = rememberNavController()
                            NavGraph(navController = navController)
                        } else {
                            OnboardingFlow(
                                onComplete = {
                                    shouldSkipOnboarding = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppWallpaperLayer(
    wallpaperType: String?,
    wallpaperColor: String?,
    wallpaperGradientStart: String?,
    wallpaperGradientEnd: String?,
    wallpaperImageUri: String?
) {
    when (wallpaperType) {
        "SOLID" -> {
            val color = parseHexColor(wallpaperColor) ?: DefaultSolidWallpaperColor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            )
        }

        "IMAGE" -> {
            if (!wallpaperImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = wallpaperImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.12f))
                )
            } else {
                GradientWallpaperLayer(
                    startColor = wallpaperGradientStart,
                    endColor = wallpaperGradientEnd
                )
            }
        }

        else -> {
            GradientWallpaperLayer(
                startColor = wallpaperGradientStart,
                endColor = wallpaperGradientEnd
            )
        }
    }
}

@Composable
private fun GradientWallpaperLayer(
    startColor: String?,
    endColor: String?
) {
    val start = parseHexColor(startColor) ?: DefaultGradientStartColor
    val end = parseHexColor(endColor) ?: DefaultGradientEndColor
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(start, end)
                )
            )
    )
}

private val DefaultSolidWallpaperColor = Color(0xFFFFFFFF)
private val DefaultGradientStartColor = Color(0xFF667EEA)
private val DefaultGradientEndColor = Color(0xFF764BA2)

private fun parseHexColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (_: IllegalArgumentException) {
        null
    }
}
