package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val saveMutex = Mutex()
    private var latestPersistedSettings: UserSettings? = null

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    fun updateNotificationEnabled(enabled: Boolean) = updateStateAndPersist {
        copy(isNotificationEnabled = enabled)
    }

    fun updateBreakfastTime(time: LocalTime) = updateStateAndPersist {
        copy(breakfastReminderTime = time)
    }

    fun updateLunchTime(time: LocalTime) = updateStateAndPersist {
        copy(lunchReminderTime = time)
    }

    fun updateDinnerTime(time: LocalTime) = updateStateAndPersist {
        copy(dinnerReminderTime = time)
    }

    fun updateGoalReminder(enabled: Boolean) = updateStateAndPersist {
        copy(enableGoalReminder = enabled)
    }

    fun updateStreakReminder(enabled: Boolean) = updateStateAndPersist {
        copy(enableStreakReminder = enabled)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collectLatest { settings ->
                if (settings == null) return@collectLatest
                latestPersistedSettings = settings
                _uiState.value = NotificationSettingsUiState(
                    isLoaded = true,
                    isNotificationEnabled = settings.isNotificationEnabled,
                    breakfastReminderTime = parseTime(settings.breakfastReminderTime),
                    lunchReminderTime = parseTime(settings.lunchReminderTime),
                    dinnerReminderTime = parseTime(settings.dinnerReminderTime),
                    enableGoalReminder = settings.enableGoalReminder,
                    enableStreakReminder = settings.enableStreakReminder
                )
            }
        }
    }

    private fun updateStateAndPersist(transform: NotificationSettingsUiState.() -> NotificationSettingsUiState) {
        _uiState.value = _uiState.value.transform()
        persistCurrentState()
    }

    private fun persistCurrentState() {
        viewModelScope.launch {
            saveMutex.withLock {
                val state = _uiState.value
                if (!state.isLoaded) return@withLock

                val base = latestPersistedSettings ?: userSettingsRepository.getSettingsOnce() ?: UserSettings()
                val updated = base.copy(
                    id = base.id,
                    isNotificationEnabled = state.isNotificationEnabled,
                    breakfastReminderTime = state.breakfastReminderTime.format(formatter),
                    lunchReminderTime = state.lunchReminderTime.format(formatter),
                    dinnerReminderTime = state.dinnerReminderTime.format(formatter),
                    enableGoalReminder = state.enableGoalReminder,
                    enableStreakReminder = state.enableStreakReminder
                )
                userSettingsRepository.saveSettings(updated)
                latestPersistedSettings = updated
                notificationScheduler.syncMealReminders(
                    settings = updated,
                    source = "NotificationSettings.save"
                )
            }
        }
    }

    private fun parseTime(raw: String): LocalTime {
        return runCatching { LocalTime.parse(raw, formatter) }
            .getOrDefault(LocalTime.of(8, 0))
    }
}

data class NotificationSettingsUiState(
    val isLoaded: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val breakfastReminderTime: LocalTime = LocalTime.of(8, 0),
    val lunchReminderTime: LocalTime = LocalTime.of(12, 0),
    val dinnerReminderTime: LocalTime = LocalTime.of(18, 0),
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = false
)
