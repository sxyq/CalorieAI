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
class AppearanceSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppearanceSettingsUiState())
    val uiState: StateFlow<AppearanceSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.value = AppearanceSettingsUiState(
                        themeMode = ThemeMode.valueOf(it.themeMode),
                        useDeadlinerStyle = it.useDeadlinerStyle,
                        hideDividers = it.hideDividers,
                        fontSize = FontSize.valueOf(it.fontSize),
                        enableAnimations = it.enableAnimations
                    )
                }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        saveSettings()
    }

    fun updateDeadlinerStyle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useDeadlinerStyle = enabled)
        saveSettings()
    }

    fun updateHideDividers(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hideDividers = enabled)
        saveSettings()
    }

    fun updateFontSize(size: FontSize) {
        _uiState.value = _uiState.value.copy(fontSize = size)
        saveSettings()
    }

    fun updateEnableAnimations(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableAnimations = enabled)
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val settings = UserSettings(
                themeMode = currentState.themeMode.name,
                useDeadlinerStyle = currentState.useDeadlinerStyle,
                hideDividers = currentState.hideDividers,
                fontSize = currentState.fontSize.name,
                enableAnimations = currentState.enableAnimations
            )
            userSettingsRepository.saveSettings(settings)
        }
    }
}

data class AppearanceSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val enableAnimations: Boolean = true
)
