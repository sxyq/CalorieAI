package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.TokenUsageStats
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()
    init {
        loadConfigs()
        loadTokenUsageStats()
        loadPersonalizationSettings()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                aiConfigRepository.getAllConfigs(),
                aiConfigRepository.getDefaultConfig()
            ) { configs, defaultConfig ->
                configs to defaultConfig?.id
            }.collectLatest { (configs, defaultId) ->
                _uiState.update {
                    it.copy(
                        configs = configs,
                        defaultConfigId = defaultId,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadTokenUsageStats() {
        viewModelScope.launch {
            aiTokenUsageRepository.getTokenUsageStats().collectLatest { stats ->
                _uiState.update { it.copy(tokenUsageStats = stats) }
            }
        }
    }

    private fun loadPersonalizationSettings() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collectLatest { settings ->
                settings ?: return@collectLatest
                _uiState.update {
                    it.copy(
                        dietaryAllergens = settings.dietaryAllergens.orEmpty(),
                        flavorPreferences = settings.flavorPreferences.orEmpty(),
                        budgetPreference = settings.budgetPreference.orEmpty(),
                        maxCookingMinutes = settings.maxCookingMinutes?.toString().orEmpty(),
                        specialPopulationMode = settings.specialPopulationMode,
                        weeklyRecordGoalDays = settings.weeklyRecordGoalDays.toString()
                    )
                }
            }
        }
    }

    fun setDefaultConfig(configId: String) {
        viewModelScope.launch {
            aiConfigRepository.setDefaultConfig(configId)
        }
    }

    fun deleteConfig(config: AIConfig) {
        if (config.isPreset) return
        viewModelScope.launch {
            aiConfigRepository.deleteConfig(config)
        }
    }

    fun onDietaryAllergensChange(value: String) {
        _uiState.update { it.copy(dietaryAllergens = value) }
    }

    fun onFlavorPreferencesChange(value: String) {
        _uiState.update { it.copy(flavorPreferences = value) }
    }

    fun onBudgetPreferenceChange(value: String) {
        _uiState.update { it.copy(budgetPreference = value) }
    }

    fun onMaxCookingMinutesChange(value: String) {
        _uiState.update { it.copy(maxCookingMinutes = value.filter(Char::isDigit)) }
    }

    fun onSpecialPopulationModeChange(value: String) {
        _uiState.update { it.copy(specialPopulationMode = value) }
    }

    fun onWeeklyRecordGoalDaysChange(value: String) {
        _uiState.update { it.copy(weeklyRecordGoalDays = value.filter(Char::isDigit)) }
    }

    fun savePersonalizationSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val maxCooking = state.maxCookingMinutes.toIntOrNull()
            val weeklyGoal = state.weeklyRecordGoalDays.toIntOrNull() ?: 5

            userSettingsRepository.updateAIPersonalization(
                dietaryAllergens = state.dietaryAllergens,
                flavorPreferences = state.flavorPreferences,
                budgetPreference = state.budgetPreference,
                maxCookingMinutes = maxCooking,
                specialPopulationMode = state.specialPopulationMode,
                weeklyRecordGoalDays = weeklyGoal
            )

            _uiState.update {
                it.copy(saveMessage = "个性化偏好已保存")
            }
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }

    fun showTransientMessage(message: String) {
        _uiState.update { it.copy(saveMessage = message) }
    }

}

data class AISettingsUiState(
    val configs: List<AIConfig> = emptyList(),
    val defaultConfigId: String? = null,
    val tokenUsageStats: TokenUsageStats? = null,
    val isLoading: Boolean = true,
    val dietaryAllergens: String = "",
    val flavorPreferences: String = "",
    val budgetPreference: String = "",
    val maxCookingMinutes: String = "",
    val specialPopulationMode: String = "GENERAL",
    val weeklyRecordGoalDays: String = "5",
    val saveMessage: String? = null
)
