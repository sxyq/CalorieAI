package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.TokenUsageStats
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.voice.VoiceModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val voiceModelManager: VoiceModelManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfigs()
        loadTokenUsageStats()
        loadPersonalizationSettings()
        refreshVoiceModelState()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                aiConfigRepository.getAllConfigs(),
                aiConfigRepository.getDefaultConfig()
            ) { configs, defaultConfig ->
                _uiState.value.copy(
                    configs = configs,
                    defaultConfigId = defaultConfig?.id,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun loadTokenUsageStats() {
        viewModelScope.launch {
            aiTokenUsageRepository.getTokenUsageStats().collect { stats ->
                _uiState.value = _uiState.value.copy(tokenUsageStats = stats)
            }
        }
    }

    private fun loadPersonalizationSettings() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                if (settings != null) {
                    _uiState.value = _uiState.value.copy(
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
        // 预设配置不能删除
        if (config.isPreset) {
            return
        }
        viewModelScope.launch {
            aiConfigRepository.deleteConfig(config)
        }
    }

    fun onDietaryAllergensChange(value: String) {
        _uiState.value = _uiState.value.copy(dietaryAllergens = value)
    }

    fun onFlavorPreferencesChange(value: String) {
        _uiState.value = _uiState.value.copy(flavorPreferences = value)
    }

    fun onBudgetPreferenceChange(value: String) {
        _uiState.value = _uiState.value.copy(budgetPreference = value)
    }

    fun onMaxCookingMinutesChange(value: String) {
        _uiState.value = _uiState.value.copy(maxCookingMinutes = value.filter { it.isDigit() })
    }

    fun onSpecialPopulationModeChange(value: String) {
        _uiState.value = _uiState.value.copy(specialPopulationMode = value)
    }

    fun onWeeklyRecordGoalDaysChange(value: String) {
        _uiState.value = _uiState.value.copy(weeklyRecordGoalDays = value.filter { it.isDigit() })
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

            _uiState.value = _uiState.value.copy(
                saveMessage = "个性化忌口与偏好已保存"
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    fun refreshVoiceModelState() {
        val installedPackage = voiceModelManager.getInstalledPackage()
        _uiState.value = _uiState.value.copy(
            isVoiceModelInstalled = installedPackage != null,
            installedVoiceModelLabel = installedPackage?.let { "${it.displayName}（${it.sizeHint}）" }
        )
    }

    fun downloadVoiceModel(pkg: VoiceModelManager.VoiceModelPackage) {
        viewModelScope.launch {
            if (_uiState.value.isVoiceModelDownloading) return@launch
            _uiState.value = _uiState.value.copy(isVoiceModelDownloading = true)
            val result = voiceModelManager.downloadAndInstallModel(pkg)
            val installedPackage = voiceModelManager.getInstalledPackage()
            val errorMessage = result.exceptionOrNull()?.toReadableErrorMessage()
            _uiState.value = _uiState.value.copy(
                isVoiceModelDownloading = false,
                isVoiceModelInstalled = installedPackage != null,
                installedVoiceModelLabel = installedPackage?.let { "${it.displayName}（${it.sizeHint}）" },
                saveMessage = if (result.isSuccess) {
                    "语音模型下载完成：${pkg.displayName}，可离线语音输入"
                } else {
                    "语音模型下载失败：$errorMessage"
                }
            )
        }
    }

    private fun Throwable.toReadableErrorMessage(): String {
        val root = generateSequence(this) { it.cause }.last()
        val message = root.message?.trim().orEmpty()
        return if (message.isNotEmpty()) {
            "${root.javaClass.simpleName}: $message"
        } else {
            root.javaClass.simpleName
        }
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
    val isVoiceModelInstalled: Boolean = false,
    val installedVoiceModelLabel: String? = null,
    val isVoiceModelDownloading: Boolean = false,
    val saveMessage: String? = null
)
