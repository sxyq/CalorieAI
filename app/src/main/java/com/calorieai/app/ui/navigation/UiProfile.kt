package com.calorieai.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class MainTab {
    HOME,
    RECIPES,
    OVERVIEW,
    MY
}

data class UiProfile(
    val isSimplified: Boolean = false
) {
    /**
     * Presentation capabilities owned by the current profile. Feature screens
     * stay registered in the navigation graph; these flags only control the
     * visible surface of the existing App Shell.
     */
    val allowsRecipes: Boolean
        get() = !isSimplified

    val allowsOverview: Boolean
        get() = !isSimplified

    val allowsWater: Boolean
        get() = !isSimplified

    val allowsExercise: Boolean
        get() = !isSimplified

    val allowsAi: Boolean
        get() = !isSimplified

    val allowsNutritionOcr: Boolean
        get() = !isSimplified

    val showsSimplifiedStats: Boolean
        get() = isSimplified

    val hidesWaterSettings: Boolean
        get() = !allowsWater

    val visibleTabs: List<MainTab>
        get() = if (isSimplified) {
            listOf(MainTab.HOME, MainTab.MY)
        } else {
            MainTab.entries.toList()
        }
}

@HiltViewModel
class UiProfileViewModel @Inject constructor(
    userSettingsRepository: UserSettingsRepository
) : ViewModel() {
    val uiState: StateFlow<UiProfile> = userSettingsRepository.observeSimplifiedMode()
        .map(::UiProfile)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiProfile()
        )
}
