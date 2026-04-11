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

    fun updateWaterReminderEnabled(enabled: Boolean) = updateStateAndPersist {
        copy(enableWaterReminder = enabled)
    }

    fun addWaterReminderTime() = updateStateAndPersist {
        if (waterReminderTimes.size >= MAX_WATER_TIMES) return@updateStateAndPersist this
        val next = waterReminderTimes.lastOrNull()?.plusHours(2) ?: LocalTime.of(10, 0)
        copy(waterReminderTimes = waterReminderTimes + next)
    }

    fun removeWaterReminderTime(index: Int) = updateStateAndPersist {
        if (index !in waterReminderTimes.indices) return@updateStateAndPersist this
        copy(waterReminderTimes = waterReminderTimes.toMutableList().also { it.removeAt(index) })
    }

    fun updateWaterReminderTime(index: Int, time: LocalTime) = updateStateAndPersist {
        if (index !in waterReminderTimes.indices) return@updateStateAndPersist this
        val updated = waterReminderTimes.toMutableList()
        updated[index] = time
        copy(waterReminderTimes = updated)
    }

    fun updateWaterIntervalMinutes(raw: String) = updateStateAndPersist {
        copy(waterReminderIntervalMinutes = raw.filter { it.isDigit() }.take(4))
    }

    fun updateWaterWindowStart(time: LocalTime) = updateStateAndPersist {
        copy(waterReminderWindowStart = time)
    }

    fun updateWaterWindowEnd(time: LocalTime) = updateStateAndPersist {
        copy(waterReminderWindowEnd = time)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collectLatest { settings ->
                if (settings == null) return@collectLatest
                latestPersistedSettings = settings
                _uiState.value = NotificationSettingsUiState(
                    isLoaded = true,
                    isNotificationEnabled = settings.isNotificationEnabled,
                    breakfastReminderTime = parseTime(settings.breakfastReminderTime, LocalTime.of(8, 0)),
                    lunchReminderTime = parseTime(settings.lunchReminderTime, LocalTime.of(12, 0)),
                    dinnerReminderTime = parseTime(settings.dinnerReminderTime, LocalTime.of(18, 0)),
                    enableGoalReminder = settings.enableGoalReminder,
                    enableStreakReminder = settings.enableStreakReminder,
                    showWaterFeatures = settings.showWaterFeatures,
                    enableWaterReminder = settings.enableWaterReminder,
                    waterReminderTimes = parseReminderTimes(settings.waterReminderTimesJson),
                    waterReminderIntervalMinutes = settings.waterReminderIntervalMinutes
                        .takeIf { it > 0 }
                        ?.toString()
                        .orEmpty(),
                    waterReminderWindowStart = parseTime(settings.waterReminderWindowStart, LocalTime.of(9, 0)),
                    waterReminderWindowEnd = parseTime(settings.waterReminderWindowEnd, LocalTime.of(21, 0))
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
                val interval = state.waterReminderIntervalMinutes.toIntOrNull()?.coerceIn(0, 24 * 60) ?: 0
                val times = state.waterReminderTimes
                    .map { normalizeTime(it) }
                    .distinct()
                    .take(MAX_WATER_TIMES)

                val updated = base.copy(
                    id = base.id,
                    isNotificationEnabled = state.isNotificationEnabled,
                    breakfastReminderTime = normalizeTime(state.breakfastReminderTime),
                    lunchReminderTime = normalizeTime(state.lunchReminderTime),
                    dinnerReminderTime = normalizeTime(state.dinnerReminderTime),
                    enableGoalReminder = state.enableGoalReminder,
                    enableStreakReminder = state.enableStreakReminder,
                    enableWaterReminder = state.showWaterFeatures && state.enableWaterReminder,
                    waterReminderTimesJson = encodeReminderTimes(times),
                    waterReminderIntervalMinutes = interval,
                    waterReminderWindowStart = normalizeTime(state.waterReminderWindowStart),
                    waterReminderWindowEnd = normalizeTime(state.waterReminderWindowEnd)
                )

                userSettingsRepository.saveSettings(updated)
                latestPersistedSettings = updated
                notificationScheduler.syncReminders(
                    settings = updated,
                    source = "NotificationSettings.save"
                )
            }
        }
    }

    private fun parseTime(raw: String, fallback: LocalTime): LocalTime {
        return runCatching { LocalTime.parse(raw, formatter) }
            .getOrElse { fallback }
    }

    private fun normalizeTime(time: LocalTime): String = time.format(formatter)

    private fun parseReminderTimes(raw: String?): List<LocalTime> {
        if (raw.isNullOrBlank()) return listOf(LocalTime.of(10, 0))
        val parsed = Regex("\\b([01]\\d|2[0-3]):([0-5]\\d)\\b")
            .findAll(raw)
            .mapNotNull { match ->
                runCatching { LocalTime.parse(match.value, formatter) }.getOrNull()
            }
            .distinct()
            .take(MAX_WATER_TIMES)
            .toList()
        return if (parsed.isEmpty()) listOf(LocalTime.of(10, 0)) else parsed
    }

    private fun encodeReminderTimes(times: List<String>): String {
        if (times.isEmpty()) return "[]"
        return times.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
    }

    companion object {
        private const val MAX_WATER_TIMES = 8
    }
}

data class NotificationSettingsUiState(
    val isLoaded: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val breakfastReminderTime: LocalTime = LocalTime.of(8, 0),
    val lunchReminderTime: LocalTime = LocalTime.of(12, 0),
    val dinnerReminderTime: LocalTime = LocalTime.of(18, 0),
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = false,
    val showWaterFeatures: Boolean = true,
    val enableWaterReminder: Boolean = false,
    val waterReminderTimes: List<LocalTime> = listOf(LocalTime.of(10, 0)),
    val waterReminderIntervalMinutes: String = "",
    val waterReminderWindowStart: LocalTime = LocalTime.of(9, 0),
    val waterReminderWindowEnd: LocalTime = LocalTime.of(21, 0)
)
