package com.calorieai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.ui.navigation.NavGraph
import com.calorieai.app.ui.screens.settings.ThemeMode
import com.calorieai.app.ui.theme.CalorieAITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    // 缓存初始主题设置，避免启动时等待数据库
    private var initialThemeMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 同步获取初始主题设置，避免等待Flow
        initialThemeMode = getInitialThemeSync()

        setContent {
            // 收集主题设置（用于动态切换）
            val settings by userSettingsRepository.getSettings()
                .collectAsState(initial = null)

            // 优先使用实时设置，否则使用初始设置，最后使用系统默认
            val themeMode = settings?.themeMode ?: initialThemeMode ?: ThemeMode.SYSTEM.name
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT.name -> false
                ThemeMode.DARK.name -> true
                else -> isSystemInDarkTheme()
            }

            CalorieAITheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    /**
     * 同步获取初始主题设置，避免启动时等待数据库
     */
    private fun getInitialThemeSync(): String? {
        return try {
            // 使用SharedPreferences快速读取主题设置
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.getString("theme_mode", null)
        } catch (e: Exception) {
            null
        }
    }
}
