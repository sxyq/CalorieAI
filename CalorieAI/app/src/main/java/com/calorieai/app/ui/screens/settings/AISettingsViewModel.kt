package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.TokenUsageStats
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfigs()
        loadTokenUsageStats()
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
}

data class AISettingsUiState(
    val configs: List<AIConfig> = emptyList(),
    val defaultConfigId: String? = null,
    val tokenUsageStats: TokenUsageStats? = null,
    val isLoading: Boolean = true
)
