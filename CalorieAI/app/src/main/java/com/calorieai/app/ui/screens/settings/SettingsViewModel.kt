package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    userSettings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun updateDailyCalorieGoal(goal: Int) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.userSettings ?: UserSettings()
            userSettingsRepository.saveSettings(currentSettings.copy(dailyCalorieGoal = goal))
        }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.userSettings ?: UserSettings()
            userSettingsRepository.saveSettings(currentSettings.copy(isNotificationEnabled = enabled))
        }
    }

    fun updateReminderTime(mealType: MealReminderType, time: String) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.userSettings ?: UserSettings()
            val updatedSettings = when (mealType) {
                MealReminderType.BREAKFAST -> currentSettings.copy(breakfastReminderTime = time)
                MealReminderType.LUNCH -> currentSettings.copy(lunchReminderTime = time)
                MealReminderType.DINNER -> currentSettings.copy(dinnerReminderTime = time)
            }
            userSettingsRepository.saveSettings(updatedSettings)
        }
    }
}

data class SettingsUiState(
    val userSettings: UserSettings? = null,
    val isLoading: Boolean = true
)

enum class MealReminderType {
    BREAKFAST,
    LUNCH,
    DINNER
}
