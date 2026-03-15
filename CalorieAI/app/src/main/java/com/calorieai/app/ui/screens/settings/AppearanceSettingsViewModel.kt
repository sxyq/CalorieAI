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
                        enableAnimations = it.enableAnimations,
                        wallpaperType = try {
                            WallpaperType.valueOf(it.wallpaperType)
                        } catch (e: Exception) {
                            WallpaperType.GRADIENT
                        },
                        wallpaperColor = it.wallpaperColor,
                        wallpaperGradientStart = it.wallpaperGradientStart,
                        wallpaperGradientEnd = it.wallpaperGradientEnd,
                        wallpaperImageUri = it.wallpaperImageUri,
                        showAIWidget = it.showAIWidget
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
            val currentSettings = userSettingsRepository.getSettingsOnce()
            val currentState = _uiState.value
            val settings = UserSettings(
                id = currentSettings?.id ?: 1,
                dailyCalorieGoal = currentSettings?.dailyCalorieGoal ?: 2000,
                userWeight = currentSettings?.userWeight ?: 70.0f,
                userHeight = currentSettings?.userHeight ?: 170.0f,
                userAge = currentSettings?.userAge ?: 25,
                userGender = currentSettings?.userGender ?: "MALE",
                activityLevel = currentSettings?.activityLevel ?: "MODERATE",
                themeMode = currentState.themeMode.name,
                useDeadlinerStyle = currentState.useDeadlinerStyle,
                hideDividers = currentState.hideDividers,
                fontSize = currentState.fontSize.name,
                enableAnimations = currentState.enableAnimations,
                enableCloudSync = currentSettings?.enableCloudSync ?: false,
                showAIWidget = currentState.showAIWidget,
                wallpaperType = currentState.wallpaperType.name,
                wallpaperColor = currentState.wallpaperColor,
                wallpaperGradientStart = currentState.wallpaperGradientStart,
                wallpaperGradientEnd = currentState.wallpaperGradientEnd,
                wallpaperImageUri = currentState.wallpaperImageUri
            )
            userSettingsRepository.saveSettings(settings)
        }
    }

    fun updateWallpaperType(type: WallpaperType) {
        _uiState.value = _uiState.value.copy(wallpaperType = type)
        saveSettings()
    }

    fun updateWallpaperColor(color: String?) {
        _uiState.value = _uiState.value.copy(wallpaperColor = color)
        saveSettings()
    }

    fun updateWallpaperGradient(startColor: String?, endColor: String?) {
        _uiState.value = _uiState.value.copy(
            wallpaperGradientStart = startColor,
            wallpaperGradientEnd = endColor
        )
        saveSettings()
    }

    fun updateWallpaperImage(uri: String?) {
        _uiState.value = _uiState.value.copy(wallpaperImageUri = uri)
        saveSettings()
    }

    fun updateShowAIWidget(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAIWidget = show)
        saveSettings()
    }
}

data class AppearanceSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val enableAnimations: Boolean = true,
    val wallpaperType: WallpaperType = WallpaperType.GRADIENT,
    val wallpaperColor: String? = null,
    val wallpaperGradientStart: String? = null,
    val wallpaperGradientEnd: String? = null,
    val wallpaperImageUri: String? = null,
    val showAIWidget: Boolean = true
)

enum class WallpaperType {
    GRADIENT, SOLID, IMAGE
}
