package com.calorieai.app.ui.screens.settings

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.TokenUsageStats
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.voice.VoiceModelManager
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
    private val userSettingsRepository: UserSettingsRepository,
    private val voiceModelManager: VoiceModelManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()
    private val voiceStateMachine = VoiceModelStateMachine()

    init {
        loadConfigs()
        loadTokenUsageStats()
        loadPersonalizationSettings()
        refreshVoiceModelState()
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

    fun refreshVoiceModelState() {
        val installedPackage = voiceModelManager.getInstalledPackage()
        voiceStateMachine.reset()
        _uiState.update {
            it.copy(
                isVoiceModelInstalled = installedPackage != null,
                installedVoiceModelLabel = installedPackage?.let { pkg ->
                    "${pkg.displayName}（${pkg.sizeHint}）"
                },
                voiceModelStage = VoiceModelManager.OperationStage.IDLE,
                voiceModelProgressPercent = 0,
                voiceModelProgressMessage = null
            )
        }
    }

    fun downloadVoiceModel(pkg: VoiceModelManager.VoiceModelPackage) {
        viewModelScope.launch {
            if (_uiState.value.isVoiceModelDownloading || _uiState.value.isVoiceModelRemoving) return@launch
            voiceStateMachine.reset()

            _uiState.update {
                it.copy(
                    isVoiceModelDownloading = true,
                    voiceModelStage = VoiceModelManager.OperationStage.DOWNLOADING,
                    voiceModelProgressPercent = 0,
                    voiceModelProgressMessage = "准备下载 ${pkg.displayName}..."
                )
            }

            val result = voiceModelManager.downloadAndInstallModel(pkg) { progress ->
                dispatchVoiceProgress(progress)
            }

            val installedPackage = voiceModelManager.getInstalledPackage()
            val errorMessage = result.exceptionOrNull()?.toReadableErrorMessage()

            _uiState.update {
                it.copy(
                    isVoiceModelDownloading = false,
                    isVoiceModelInstalled = installedPackage != null,
                    installedVoiceModelLabel = installedPackage?.let { target ->
                        "${target.displayName}（${target.sizeHint}）"
                    },
                    voiceModelStage = if (result.isSuccess) {
                        VoiceModelManager.OperationStage.COMPLETED
                    } else {
                        VoiceModelManager.OperationStage.FAILED
                    },
                    voiceModelProgressPercent = if (result.isSuccess) 100 else it.voiceModelProgressPercent,
                    voiceModelProgressMessage = if (result.isSuccess) {
                        "${pkg.displayName} 下载并安装完成"
                    } else {
                        "下载失败：$errorMessage"
                    },
                    saveMessage = if (result.isSuccess) {
                        "语音模型已就绪，可离线语音输入"
                    } else {
                        "语音模型下载失败：$errorMessage"
                    }
                )
            }
        }
    }

    fun uninstallVoiceModel() {
        viewModelScope.launch {
            if (_uiState.value.isVoiceModelDownloading || _uiState.value.isVoiceModelRemoving) return@launch
            voiceStateMachine.reset()

            _uiState.update {
                it.copy(
                    isVoiceModelRemoving = true,
                    voiceModelStage = VoiceModelManager.OperationStage.REMOVING,
                    voiceModelProgressPercent = 0,
                    voiceModelProgressMessage = "正在删除本地语音模型..."
                )
            }

            val result = voiceModelManager.uninstallModel { progress ->
                dispatchVoiceProgress(progress)
            }

            val errorMessage = result.exceptionOrNull()?.toReadableErrorMessage()
            val installedPackage = voiceModelManager.getInstalledPackage()

            _uiState.update {
                it.copy(
                    isVoiceModelRemoving = false,
                    isVoiceModelInstalled = installedPackage != null,
                    installedVoiceModelLabel = installedPackage?.let { target ->
                        "${target.displayName}（${target.sizeHint}）"
                    },
                    voiceModelStage = if (result.isSuccess) {
                        VoiceModelManager.OperationStage.COMPLETED
                    } else {
                        VoiceModelManager.OperationStage.FAILED
                    },
                    voiceModelProgressPercent = if (result.isSuccess) 100 else it.voiceModelProgressPercent,
                    voiceModelProgressMessage = if (result.isSuccess) {
                        "本地语音模型已删除"
                    } else {
                        "删除失败：$errorMessage"
                    },
                    saveMessage = if (result.isSuccess) {
                        "本地语音模型已删除"
                    } else {
                        "删除失败：$errorMessage"
                    }
                )
            }
        }
    }

    private fun dispatchVoiceProgress(progress: VoiceModelManager.OperationProgress) {
        val reduced = voiceStateMachine.reduce(
            progress = progress,
            nowElapsedMillis = SystemClock.elapsedRealtime()
        ) ?: return

        _uiState.update {
            it.copy(
                voiceModelStage = reduced.stage,
                voiceModelProgressPercent = reduced.percent,
                voiceModelProgressMessage = reduced.message
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
    val isVoiceModelRemoving: Boolean = false,
    val voiceModelStage: VoiceModelManager.OperationStage = VoiceModelManager.OperationStage.IDLE,
    val voiceModelProgressPercent: Int = 0,
    val voiceModelProgressMessage: String? = null,
    val saveMessage: String? = null
)
