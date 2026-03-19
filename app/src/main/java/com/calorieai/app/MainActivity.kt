package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.notification.NotificationScheduler
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.onboarding.OnboardingFlow
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import com.calorieai.app.ui.theme.GlassAlpha
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
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
            val backgroundOverride = remember(
                settings?.wallpaperType,
                settings?.wallpaperColor,
                settings?.wallpaperGradientStart,
                settings?.wallpaperGradientEnd
            ) {
                when (settings?.wallpaperType) {
                    "SOLID" -> parseHexColor(settings?.wallpaperColor)
                    "GRADIENT" -> {
                        val start = parseHexColor(settings?.wallpaperGradientStart)
                        val end = parseHexColor(settings?.wallpaperGradientEnd)
                        when {
                            start != null && end != null -> lerp(start, end, 0.5f)
                            start != null -> start
                            else -> end
                        }
                    }
                    else -> null
                }
            }

            var shouldSkipOnboarding by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                val completed = onboardingDataStore.isOnboardingCompleted.first()
                shouldSkipOnboarding = completed
                isLoading = false
            }

            LaunchedEffect(
                settings?.isNotificationEnabled,
                settings?.breakfastReminderTime,
                settings?.lunchReminderTime,
                settings?.dinnerReminderTime
            ) {
                settings?.let { notificationScheduler.syncMealReminders(it) }
            }

            CalorieAITheme(
                darkTheme = darkTheme,
                backgroundOverride = backgroundOverride
            ) {
                val navigationBarColor = if (darkTheme) {
                    GlassDarkColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR)
                } else {
                    GlassLightColors.NavigationBarBackground.copy(alpha = GlassAlpha.NAVIGATION_BAR)
                }
                
                SideEffect {
                    window.navigationBarColor = navigationBarColor.toArgb()
                }
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading) {
                        // 显示空白背景，避免闪烁
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

private fun parseHexColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (_: IllegalArgumentException) {
        null
    }
}
