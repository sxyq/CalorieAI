package com.calorieai.app.ui.navigation

import com.calorieai.app.data.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class FeatureGate @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    fun observeWaterFeaturesEnabled(): Flow<Boolean> {
        return userSettingsRepository.getSettings()
            .map { settings -> settings?.showWaterFeatures ?: true }
            .distinctUntilChanged()
    }
}
