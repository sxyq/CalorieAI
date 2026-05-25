package com.calorieai.app.service.startup

import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.notification.ReminderResyncCoordinator
import com.calorieai.app.service.update.AppUpdateInfo
import com.calorieai.app.service.update.AppUpdateManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
class MainActivityStartupCoordinator @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore,
    private val userSettingsRepository: UserSettingsRepository,
    private val reminderResyncCoordinator: ReminderResyncCoordinator,
    private val appUpdateManager: AppUpdateManager
) {
    suspend fun resolveShouldSkipOnboarding(settings: UserSettings?): Boolean = withContext(Dispatchers.IO) {
        if (settings?.onboardingCompleted == true) {
            onboardingDataStore.clearOnboardingState()
            return@withContext true
        }

        val legacyCompleted = onboardingDataStore.isOnboardingCompleted.first()
        if (legacyCompleted) {
            userSettingsRepository.updateOnboardingCompleted(true)
            onboardingDataStore.clearOnboardingState()
        }
        legacyCompleted
    }

    suspend fun syncReminderStateAfterLaunch(settings: UserSettings) {
        delay(900)
        withContext(Dispatchers.IO) {
            reminderResyncCoordinator.sync(
                settings = settings,
                source = "MainActivity.launch"
            )
        }
    }

    suspend fun checkForUpdatesAfterLaunch(): AppUpdateInfo? {
        delay(1300)
        return appUpdateManager.checkForUpdate()
    }

    fun openDownloadPage(updateInfo: AppUpdateInfo): Boolean {
        return appUpdateManager.openDownloadPage(updateInfo)
    }
}
