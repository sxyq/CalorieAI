package com.calorieai.app.data.repository

import com.calorieai.app.data.local.UserSettingsDao
import com.calorieai.app.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) {
    fun getSettings(): Flow<UserSettings?> = userSettingsDao.getSettings()

    suspend fun saveSettings(settings: UserSettings) {
        userSettingsDao.insertOrUpdate(settings)
    }

    suspend fun getSettingsOnce(): UserSettings? {
        return userSettingsDao.getSettingsOnce()
    }
}
