package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {
    companion object {
        const val DEFAULT_GRADIENT_START = "#667eea"
        const val DEFAULT_GRADIENT_END = "#764ba2"
        const val DEFAULT_LIGHT_WALLPAPER_COLOR = "#FFFFFF"
        const val DEFAULT_DARK_WALLPAPER_COLOR = "#000000"
    }

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
                            WallpaperType.SOLID
                        },
                        wallpaperColor = it.wallpaperColor,
                        wallpaperGradientStart = it.wallpaperGradientStart,
                        wallpaperGradientEnd = it.wallpaperGradientEnd,
                        wallpaperImageUri = it.wallpaperImageUri,
                        showAIWidget = it.showAIWidget,
                        showWaterFeatures = it.showWaterFeatures
                    )
                }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        _uiState.value = when (mode) {
            ThemeMode.LIGHT -> _uiState.value.copy(
                themeMode = mode,
                wallpaperType = WallpaperType.SOLID,
                wallpaperColor = DEFAULT_LIGHT_WALLPAPER_COLOR,
                wallpaperGradientStart = null,
                wallpaperGradientEnd = null,
                wallpaperImageUri = null
            )

            ThemeMode.DARK -> _uiState.value.copy(
                themeMode = mode,
                wallpaperType = WallpaperType.SOLID,
                wallpaperColor = DEFAULT_DARK_WALLPAPER_COLOR,
                wallpaperGradientStart = null,
                wallpaperGradientEnd = null,
                wallpaperImageUri = null
            )

            ThemeMode.SYSTEM -> _uiState.value.copy(themeMode = mode)
        }
        saveSettings()
    }

    fun updateFontSize(size: FontSize) {
        _uiState.value = _uiState.value.copy(fontSize = size)
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            userSettingsRepository.updateSettings { settings ->
                settings.copy(
                    themeMode = currentState.themeMode.name,
                    useDeadlinerStyle = currentState.useDeadlinerStyle,
                    hideDividers = currentState.hideDividers,
                    fontSize = currentState.fontSize.name,
                    enableAnimations = currentState.enableAnimations,
                    showAIWidget = currentState.showAIWidget,
                    showWaterFeatures = currentState.showWaterFeatures,
                    wallpaperType = currentState.wallpaperType.name,
                    wallpaperColor = currentState.wallpaperColor,
                    wallpaperGradientStart = currentState.wallpaperGradientStart,
                    wallpaperGradientEnd = currentState.wallpaperGradientEnd,
                    wallpaperImageUri = currentState.wallpaperImageUri
                )
            }
        }
    }

    fun updateShowAIWidget(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAIWidget = show)
        saveSettings()
    }

    fun updateShowWaterFeatures(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWaterFeatures = show)
        saveSettings()
    }
}

data class AppearanceSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val enableAnimations: Boolean = true,
    val wallpaperType: WallpaperType = WallpaperType.SOLID,
    val wallpaperColor: String? = AppearanceSettingsViewModel.DEFAULT_LIGHT_WALLPAPER_COLOR,
    val wallpaperGradientStart: String? = null,
    val wallpaperGradientEnd: String? = null,
    val wallpaperImageUri: String? = null,
    val showAIWidget: Boolean = true,
    val showWaterFeatures: Boolean = true
)

enum class WallpaperType {
    GRADIENT, SOLID, IMAGE
}
