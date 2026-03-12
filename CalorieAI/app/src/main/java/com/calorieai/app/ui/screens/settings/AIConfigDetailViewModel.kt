package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIConfigPresets
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.repository.AIConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIConfigDetailViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIConfigDetailUiState())
    val uiState: StateFlow<AIConfigDetailUiState> = _uiState.asStateFlow()

    private var configId: String? = null

    fun loadConfig(id: String?) {
        configId = id
        if (id != null) {
            viewModelScope.launch {
                aiConfigRepository.getConfigById(id)?.let { config ->
                    _uiState.value = AIConfigDetailUiState(
                        name = config.name,
                        selectedIcon = config.icon,
                        protocol = config.protocol,
                        apiUrl = config.apiUrl,
                        apiKey = config.apiKey,
                        modelId = config.modelId,
                        isImageUnderstanding = config.isImageUnderstanding,
                        isEditing = true
                    )
                }
            }
        } else {
            _uiState.value = AIConfigDetailUiState()
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateIcon(icon: String) {
        _uiState.value = _uiState.value.copy(selectedIcon = icon)
    }

    fun updateProtocol(protocol: AIProtocol) {
        _uiState.value = _uiState.value.copy(protocol = protocol)
    }

    fun updateApiUrl(url: String) {
        _uiState.value = _uiState.value.copy(apiUrl = url)
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun updateModelId(modelId: String) {
        _uiState.value = _uiState.value.copy(modelId = modelId)
    }

    fun updateImageUnderstanding(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isImageUnderstanding = enabled)
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTesting = true,
                testResult = null
            )

            // 模拟测试连接
            kotlinx.coroutines.delay(1500)

            val isSuccess = _uiState.value.apiUrl.isNotBlank() &&
                    _uiState.value.apiKey.isNotBlank() &&
                    _uiState.value.modelId.isNotBlank()

            _uiState.value = _uiState.value.copy(
                isTesting = false,
                testResult = if (isSuccess) {
                    TestResult.Success("连接成功")
                } else {
                    TestResult.Error("请填写完整的API信息")
                }
            )
        }
    }

    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }

    fun saveConfig(): Boolean {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入配置名称")
            return false
        }

        viewModelScope.launch {
            val config = AIConfig(
                id = configId ?: java.util.UUID.randomUUID().toString(),
                name = state.name,
                icon = state.selectedIcon,
                protocol = state.protocol,
                apiUrl = state.apiUrl,
                apiKey = state.apiKey,
                modelId = state.modelId,
                isImageUnderstanding = state.isImageUnderstanding,
                isDefault = false
            )

            if (configId != null) {
                aiConfigRepository.updateConfig(config)
            } else {
                aiConfigRepository.addConfig(config)
            }
        }

        return true
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun applyPreset(preset: AIConfig) {
        _uiState.value = _uiState.value.copy(
            name = preset.name,
            selectedIcon = preset.icon,
            protocol = preset.protocol,
            apiUrl = preset.apiUrl,
            modelId = preset.modelId,
            isImageUnderstanding = preset.isImageUnderstanding
        )
    }
}

data class AIConfigDetailUiState(
    val name: String = "",
    val selectedIcon: String = "🤖",
    val protocol: AIProtocol = AIProtocol.OPENAI,
    val apiUrl: String = "",
    val apiKey: String = "",
    val modelId: String = "",
    val isImageUnderstanding: Boolean = false,
    val isEditing: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val errorMessage: String? = null
)

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}
