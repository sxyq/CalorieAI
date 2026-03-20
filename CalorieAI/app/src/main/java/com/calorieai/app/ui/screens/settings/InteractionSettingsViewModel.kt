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
class InteractionSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InteractionSettingsUiState())
    val uiState: StateFlow<InteractionSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.value = InteractionSettingsUiState(
                        feedbackType = runCatching { FeedbackType.valueOf(it.feedbackType) }
                            .getOrDefault(FeedbackType.BOTH),
                        enableVibration = it.enableVibration,
                        enableSound = it.enableSound,
                        backgroundBehavior = runCatching { BackgroundBehavior.valueOf(it.backgroundBehavior) }
                            .getOrDefault(BackgroundBehavior.STANDARD),
                        startupPage = runCatching { StartupPage.valueOf(it.startupPage) }
                            .getOrDefault(StartupPage.HOME),
                        enableQuickAdd = it.enableQuickAdd,
                        enableLongPressHomeToAdd = it.enableLongPressHomeToAdd,
                        enableLongPressOverviewToStats = it.enableLongPressOverviewToStats,
                        enableLongPressMyToProfileEdit = it.enableLongPressMyToProfileEdit
                    )
                }
            }
        }
    }

    fun updateFeedbackType(type: FeedbackType) {
        _uiState.value = _uiState.value.copy(feedbackType = type)
        saveSettings()
    }

    fun updateEnableVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableVibration = enabled)
        saveSettings()
    }

    fun updateEnableSound(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableSound = enabled)
        saveSettings()
    }

    fun updateBackgroundBehavior(behavior: BackgroundBehavior) {
        _uiState.value = _uiState.value.copy(backgroundBehavior = behavior)
        saveSettings()
    }

    fun updateStartupPage(page: StartupPage) {
        _uiState.value = _uiState.value.copy(startupPage = page)
        saveSettings()
    }

    fun updateEnableQuickAdd(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableQuickAdd = enabled)
        saveSettings()
    }

    fun updateEnableLongPressHomeToAdd(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableLongPressHomeToAdd = enabled)
        saveSettings()
    }

    fun updateEnableLongPressOverviewToStats(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableLongPressOverviewToStats = enabled)
        saveSettings()
    }

    fun updateEnableLongPressMyToProfileEdit(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableLongPressMyToProfileEdit = enabled)
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val existingSettings = userSettingsRepository.getSettings().firstOrNull()
            val settings = (existingSettings ?: UserSettings()).copy(
                feedbackType = currentState.feedbackType.name,
                enableVibration = currentState.enableVibration,
                enableSound = currentState.enableSound,
                backgroundBehavior = currentState.backgroundBehavior.name,
                startupPage = currentState.startupPage.name,
                enableQuickAdd = currentState.enableQuickAdd,
                enableLongPressHomeToAdd = currentState.enableLongPressHomeToAdd,
                enableLongPressOverviewToStats = currentState.enableLongPressOverviewToStats,
                enableLongPressMyToProfileEdit = currentState.enableLongPressMyToProfileEdit
            )
            userSettingsRepository.saveSettings(settings)
        }
    }
}

data class InteractionSettingsUiState(
    val feedbackType: FeedbackType = FeedbackType.BOTH,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = false,
    val backgroundBehavior: BackgroundBehavior = BackgroundBehavior.STANDARD,
    val startupPage: StartupPage = StartupPage.HOME,
    val enableQuickAdd: Boolean = false,
    val enableLongPressHomeToAdd: Boolean = true,
    val enableLongPressOverviewToStats: Boolean = true,
    val enableLongPressMyToProfileEdit: Boolean = true
)
