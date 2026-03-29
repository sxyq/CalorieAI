package com.calorieai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.notification.NotificationScheduler
import com.calorieai.app.service.update.AppUpdateInfo
import com.calorieai.app.service.update.AppUpdateManager
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

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

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
            var pendingUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
            var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) {
                hasRequestedNotificationPermission = true
            }

            LaunchedEffect(Unit) {
                val completed = withContext(Dispatchers.IO) {
                    onboardingDataStore.isOnboardingCompleted.first()
                }
                shouldSkipOnboarding = completed
                isLoading = false
            }

            LaunchedEffect(isLoading, shouldSkipOnboarding, settings?.isNotificationEnabled) {
                if (isLoading || !shouldSkipOnboarding) return@LaunchedEffect
                if (settings?.isNotificationEnabled != true) return@LaunchedEffect
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
                if (hasRequestedNotificationPermission) return@LaunchedEffect

                val granted = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    hasRequestedNotificationPermission = true
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
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
                    notificationScheduler.syncMealReminders(
                        settings = currentSettings,
                        source = "MainActivity.launch"
                    )
                }
            }

            LaunchedEffect(isLoading, shouldSkipOnboarding) {
                if (isLoading || !shouldSkipOnboarding) return@LaunchedEffect
                delay(1300)
                pendingUpdateInfo = appUpdateManager.checkForUpdate()
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
                pendingUpdateInfo?.let { updateInfo ->
                    AppUpdateDialog(
                        updateInfo = updateInfo,
                        onDownload = {
                            val opened = appUpdateManager.openDownloadPage(updateInfo)
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
