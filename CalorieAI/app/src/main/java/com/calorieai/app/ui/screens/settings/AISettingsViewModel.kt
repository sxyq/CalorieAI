package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.repository.AIConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                aiConfigRepository.getAllConfigs(),
                aiConfigRepository.getDefaultConfig()
            ) { configs, defaultConfig ->
                AISettingsUiState(
                    configs = configs,
                    defaultConfigId = defaultConfig?.id,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
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
    val isLoading: Boolean = true
)
