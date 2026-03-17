package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.onboarding.OnboardingFlow
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import com.calorieai.app.ui.theme.GlassAlpha
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var onboardingDataStore: OnboardingDataStore

    private var initialThemeMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initialThemeMode = getInitialThemeSync()

        setContent {
            val settings by userSettingsRepository.getSettings()
                .collectAsState(initial = null)

            val themeMode = settings?.themeMode ?: initialThemeMode ?: ThemeMode.SYSTEM.name
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT.name -> false
                ThemeMode.DARK.name -> true
                else -> isSystemInDarkTheme()
            }

            // 检查是否需要显示引导：已完成引导或有用户数据则跳过
            val hasCompletedOnboarding by onboardingDataStore.isOnboardingCompleted
                .collectAsState(initial = false)
            
            // 有用户数据也视为已完成引导
            val hasUserData = settings?.onboardingCompleted == true || settings?.userWeight != null
            
            val shouldSkipOnboarding = hasCompletedOnboarding || hasUserData
            
            // 等待数据加载完成
            val isDataLoaded = settings != null

            CalorieAITheme(darkTheme = darkTheme) {
                // 设置系统导航栏颜色与底部导航栏一致
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
                    // 数据未加载完成时显示加载状态
                    if (!isDataLoaded) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (shouldSkipOnboarding) {
                        val navController = rememberNavController()
                        NavGraph(navController = navController)
                    } else {
                        OnboardingFlow(
                            onComplete = { }
                        )
                    }
                }
            }
        }
    }

    private fun getInitialThemeSync(): String? {
        return try {
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.getString("theme_mode", null)
        } catch (e: Exception) {
            null
        }
    }
}
