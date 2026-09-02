package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.startup.MainActivityStartupCoordinator
import com.calorieai.app.service.update.AppUpdateDownloadState
import com.calorieai.app.service.update.AppUpdateInfo
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.onboarding.OnboardingFlow
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

            // null 表示启动状态尚未从持久化存储读取完成，避免把未加载误判为未完成。
            var shouldSkipOnboarding by remember { mutableStateOf<Boolean?>(null) }
            val isLoading = shouldSkipOnboarding == null
            var pendingUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
            var updateDownloadId by remember { mutableStateOf<UUID?>(null) }
            var updateDownloadState by remember {
                mutableStateOf<AppUpdateDownloadState>(AppUpdateDownloadState.Idle)
            }
            var installMessage by remember { mutableStateOf<String?>(null) }
            val updateScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                shouldSkipOnboarding = startupCoordinator.resolveShouldSkipOnboarding()
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
                if (isLoading || shouldSkipOnboarding != true) return@LaunchedEffect

                startupCoordinator.syncReminderStateAfterLaunch(currentSettings)
            }

            LaunchedEffect(isLoading, shouldSkipOnboarding) {
                if (isLoading || shouldSkipOnboarding != true) return@LaunchedEffect
                val updateInfo = startupCoordinator.checkForUpdatesAfterLaunch()
                pendingUpdateInfo = updateInfo
                updateInfo?.let { info ->
                    startupCoordinator.findDownloadedUpdate(info.latestVersionCode)?.let { apkPath ->
                        updateDownloadState = AppUpdateDownloadState.Ready(apkPath)
                    }
                }
            }

            LaunchedEffect(updateDownloadId) {
                val workId = updateDownloadId
                if (workId == null) {
                    updateDownloadState = AppUpdateDownloadState.Idle
                    return@LaunchedEffect
                }
                startupCoordinator.observeUpdateDownload(workId).collect { state ->
                    updateDownloadState = state
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
                        modifier = Modifier.fillMaxSize(),
                        color = if (wallpaperEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.background
                        }
                    ) {
                        when {
                            isLoading -> {
                                // 保持启动占位状态，直到完成标记读取完毕。
                            }

                            shouldSkipOnboarding == true -> {
                                val navController = rememberNavController()
                                NavGraph(navController = navController)
                            }

                            else -> {
                                OnboardingFlow(
                                    onComplete = {
                                        shouldSkipOnboarding = true
                                    }
                                )
                            }
                        }
                    }
                }
                pendingUpdateInfo?.let { updateInfo ->
                    AppUpdateDialog(
                        updateInfo = updateInfo,
                        downloadState = updateDownloadState,
                        installMessage = installMessage,
                        onDownload = {
                            installMessage = null
                            updateDownloadId = startupCoordinator.startUpdateDownload(updateInfo)
                        },
                        onInstall = { apkPath ->
                            updateScope.launch {
                                when (val result = startupCoordinator.installDownloadedUpdate(apkPath)) {
                                    is com.calorieai.app.service.update.AppUpdateManager.InstallResult.Started -> {
                                        pendingUpdateInfo = null
                                    }

                                    is com.calorieai.app.service.update.AppUpdateManager.InstallResult.PermissionRequired -> {
                                        installMessage = "请在系统设置中允许本应用安装未知来源应用，然后返回重试。"
                                    }

                                    is com.calorieai.app.service.update.AppUpdateManager.InstallResult.Failed -> {
                                        installMessage = result.message
                                    }
                                }
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
    downloadState: AppUpdateDownloadState,
    installMessage: String?,
    onDownload: () -> Unit,
    onInstall: (String) -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!updateInfo.isMandatory) onLater() },
        title = {
            Text("发现新版本 ${updateInfo.latestVersionName}")
        },
        text = {
            Column {
                Text(updateInfo.releaseNotes)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "版本号 ${updateInfo.latestVersionCode} · ${formatUpdateSize(updateInfo.apkSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                when (downloadState) {
                    AppUpdateDownloadState.Idle -> Unit
                    AppUpdateDownloadState.Queued -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("正在等待网络连接…")
                    }

                    is AppUpdateDownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadState.percent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "正在下载 ${downloadState.percent}%（" +
                                "${formatUpdateSize(downloadState.downloadedBytes)} / " +
                                formatUpdateSize(downloadState.totalBytes) + "）"
                        )
                    }

                    is AppUpdateDownloadState.Ready -> Text("下载完成，可以安装。")
                    is AppUpdateDownloadState.Failed -> Text(
                        downloadState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!installMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(installMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            when (downloadState) {
                is AppUpdateDownloadState.Ready -> TextButton(
                    onClick = { onInstall(downloadState.apkPath) },
                    enabled = downloadState.apkPath.isNotBlank()
                ) {
                    Text("安装更新")
                }

                AppUpdateDownloadState.Queued,
                is AppUpdateDownloadState.Downloading -> TextButton(
                    onClick = {},
                    enabled = false
                ) {
                    Text("下载中")
                }

                AppUpdateDownloadState.Idle,
                is AppUpdateDownloadState.Failed -> TextButton(onClick = onDownload) {
                    Text(if (downloadState is AppUpdateDownloadState.Failed) "重试" else "立即下载")
                }
            }
        },
        dismissButton = if (!updateInfo.isMandatory) {
            {
                TextButton(onClick = onLater) {
                    Text("稍后")
                }
            }
        } else {
            null
        }
    )
}

private fun formatUpdateSize(bytes: Long): String {
    if (bytes <= 0L) return "未知大小"
    val megabytes = bytes / (1024f * 1024f)
    return "%.1f MB".format(java.util.Locale.getDefault(), megabytes)
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
