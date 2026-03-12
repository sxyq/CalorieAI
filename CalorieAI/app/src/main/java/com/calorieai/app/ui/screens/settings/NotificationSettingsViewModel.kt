package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.value = NotificationSettingsUiState(
                        isNotificationEnabled = it.isNotificationEnabled,
                        breakfastReminderTime = parseTime(it.breakfastReminderTime),
                        lunchReminderTime = parseTime(it.lunchReminderTime),
                        dinnerReminderTime = parseTime(it.dinnerReminderTime),
                        enableGoalReminder = it.enableGoalReminder,
                        enableStreakReminder = it.enableStreakReminder
                    )
                }
            }
        }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationEnabled = enabled)
        saveSettings()
    }

    fun updateBreakfastTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(breakfastReminderTime = time)
        saveSettings()
    }

    fun updateLunchTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(lunchReminderTime = time)
        saveSettings()
    }

    fun updateDinnerTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(dinnerReminderTime = time)
        saveSettings()
    }

    fun updateGoalReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableGoalReminder = enabled)
        saveSettings()
    }

    fun updateStreakReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableStreakReminder = enabled)
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val settings = UserSettings(
                isNotificationEnabled = currentState.isNotificationEnabled,
                breakfastReminderTime = currentState.breakfastReminderTime.format(formatter),
                lunchReminderTime = currentState.lunchReminderTime.format(formatter),
                dinnerReminderTime = currentState.dinnerReminderTime.format(formatter),
                enableGoalReminder = currentState.enableGoalReminder,
                enableStreakReminder = currentState.enableStreakReminder
            )
            userSettingsRepository.saveSettings(settings)
        }
    }

    private fun parseTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.of(8, 0)
        }
    }
}

data class NotificationSettingsUiState(
    val isNotificationEnabled: Boolean = true,
    val breakfastReminderTime: LocalTime = LocalTime.of(8, 0),
    val lunchReminderTime: LocalTime = LocalTime.of(12, 0),
    val dinnerReminderTime: LocalTime = LocalTime.of(18, 0),
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = false
)
