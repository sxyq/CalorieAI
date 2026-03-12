package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.TokenUsageStats
import com.calorieai.app.data.repository.AIConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
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
            // TODO: 从数据库加载真实的Token使用记录
            // 这里使用模拟数据演示
            val mockStats = TokenUsageStats(
                totalTokens = 15000,
                promptTokens = 10000,
                completionTokens = 5000,
                totalCost = 0.045,
                requestCount = 25,
                todayTokens = 1200,
                todayCost = 0.0036,
                monthTokens = 8000,
                monthCost = 0.024
            )
            _uiState.value = _uiState.value.copy(tokenUsageStats = mockStats)
        }
    }

    fun setDefaultConfig(configId: String) {
        viewModelScope.launch {
            aiConfigRepository.setDefaultConfig(configId)
        }
    }

    fun deleteConfig(config: AIConfig) {
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
