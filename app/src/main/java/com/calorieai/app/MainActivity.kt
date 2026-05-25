package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.startup.MainActivityStartupCoordinator
import com.calorieai.app.service.update.AppUpdateInfo
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.onboarding.OnboardingFlow
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var startupCoordinator: MainActivityStartupCoordinator

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
            val wallpaperType = settings?.wallpaperType ?: "SOLID"
            val wallpaperColor = settings?.wallpaperColor
            val hasGradientWallpaper = wallpaperType == "GRADIENT" &&
                !settings?.wallpaperGradientStart.isNullOrBlank() &&
                !settings?.wallpaperGradientEnd.isNullOrBlank()
            val hasImageWallpaper = wallpaperType == "IMAGE" && !settings?.wallpaperImageUri.isNullOrBlank()
            val isDefaultSolidWallpaper = wallpaperType == "SOLID" && (
                wallpaperColor.isNullOrBlank() ||
                    wallpaperColor.equals("#FFFFFF", ignoreCase = true)
            )
            val wallpaperEnabled = hasGradientWallpaper || hasImageWallpaper || !isDefaultSolidWallpaper
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
                            // 娓愬彉澹佺焊鐢卞叏灞€鑳屾櫙灞傜洿鎺ョ粯鍒讹紝涓嶅啀鐢ㄤ腑闂磋壊瑕嗙洊涓婚锛?
                            // 閬垮厤琚富棰樿〃闈㈣壊鈥滄姽骞斥€濅负绾壊瑙傛劅銆?
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
            var pendingUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

            LaunchedEffect(settings?.onboardingCompleted) {
                val completed = startupCoordinator.resolveShouldSkipOnboarding(settings)
                shouldSkipOnboarding = completed
                isLoading = false
            }

            LaunchedEffect(
                isLoading,
                shouldSkipOnboarding,
                settings?.isNotificationEnabled,
                settings?.breakfastReminderTime,
                settings?.lunchReminderTime,
                settings?.dinnerReminderTime,
                settings?.showWaterFeatures,
                settings?.enableWaterReminder,
                settings?.waterReminderTimesJson,
                settings?.waterReminderIntervalMinutes,
                settings?.waterReminderWindowStart,
                settings?.waterReminderWindowEnd
            ) {
                val currentSettings = settings ?: return@LaunchedEffect
                if (isLoading || !shouldSkipOnboarding) return@LaunchedEffect

                startupCoordinator.syncReminderStateAfterLaunch(currentSettings)
            }

            LaunchedEffect(isLoading, shouldSkipOnboarding) {
                if (isLoading || !shouldSkipOnboarding) return@LaunchedEffect
                pendingUpdateInfo = startupCoordinator.checkForUpdatesAfterLaunch()
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
                        modifier = Modifier.fillMaxSize(),
                        color = if (wallpaperEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.background
                        }
                    ) {
                        if (isLoading) {
                            // 鏄剧ず澹佺焊鑳屾櫙锛岄伩鍏嶉棯鐑?
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
                pendingUpdateInfo?.let { updateInfo ->
                        AppUpdateDialog(
                        updateInfo = updateInfo,
                        onDownload = {
                            val opened = startupCoordinator.openDownloadPage(updateInfo)
                            if (opened) {
                                pendingUpdateInfo = null
                            }
                        },
                        onLater = {
                            pendingUpdateInfo = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDownload: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Text("发现新版本 ${updateInfo.latestVersionName}")
        },
        text = {
            Text(updateInfo.changelog)
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text("立即下载")
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text("稍后")
            }
        }
    )
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DefaultSolidWallpaperColor)
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
