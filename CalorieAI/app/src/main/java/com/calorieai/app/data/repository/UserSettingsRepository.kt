package com.calorieai.app.data.repository

import android.content.Context
import com.calorieai.app.data.local.UserSettingsDao
import com.calorieai.app.data.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    fun getSettings(): Flow<UserSettings?> = userSettingsDao.getSettings()

    suspend fun saveSettings(settings: UserSettings) {
        userSettingsDao.insertOrUpdate(settings)
        // 同步保存到SharedPreferences用于快速读取
        syncToPreferences(settings)
    }

    suspend fun getSettingsOnce(): UserSettings? {
        return userSettingsDao.getSettingsOnce()
    }

    /**
     * 同步设置到SharedPreferences，用于启动时快速读取
     */
    private fun syncToPreferences(settings: UserSettings) {
        prefs.edit().apply {
            putString("theme_mode", settings.themeMode)
            putBoolean("show_ai_widget", settings.showAIWidget)
            apply()
        }
    }
}
