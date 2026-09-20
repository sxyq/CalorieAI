package com.calorieai.app.ui.navigation

import com.calorieai.app.data.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class FeatureGate @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    fun observeWaterFeaturesEnabled(): Flow<Boolean> {
        return combine(
            userSettingsRepository.getSettings(),
            userSettingsRepository.observeSimplifiedMode()
        ) { settings, simplifiedMode ->
            (settings?.showWaterFeatures ?: true) && UiProfile(simplifiedMode).allowsWater
        }
            .distinctUntilChanged()
    }
}
