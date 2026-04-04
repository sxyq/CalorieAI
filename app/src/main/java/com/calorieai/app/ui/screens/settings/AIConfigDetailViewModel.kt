package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIConfigPresets
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.service.ai.common.AIApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIConfigDetailViewModel @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    private val apiCallRecordRepository: APICallRecordRepository,
    private val aiApiClient: AIApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIConfigDetailUiState())
    val uiState: StateFlow<AIConfigDetailUiState> = _uiState.asStateFlow()

    private var configId: String? = null
    private var recordsJob: Job? = null

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
                        isEditing = true,
                        isPreset = config.isPreset
                    )
                    observeCallRecords(id)
                }
            }
        } else {
            recordsJob?.cancel()
            _uiState.value = AIConfigDetailUiState()
        }
    }

    private fun observeCallRecords(id: String) {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            apiCallRecordRepository.getRecordsByConfig(id).collect { records ->
                _uiState.update { it.copy(recentCallRecords = records.take(20)) }
            }
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
            val state = _uiState.value
            if (state.apiUrl.isBlank() || state.apiKey.isBlank() || state.modelId.isBlank()) {
                _uiState.value = state.copy(
                    isTesting = false,
                    testResult = TestResult.Error("请填写完整的API信息")
                )
                return@launch
            }

            val tempConfig = AIConfig(
                id = "connection_test",
                name = state.name.ifBlank { "连接测试" },
                icon = state.selectedIcon,
                protocol = state.protocol,
                apiUrl = state.apiUrl,
                apiKey = state.apiKey,
                modelId = state.modelId,
                isImageUnderstanding = state.isImageUnderstanding,
                isDefault = false,
                isPreset = false
            )

            val result = aiApiClient.testConnection(
                config = tempConfig,
                timeoutSeconds = 5
            )

            _uiState.value = _uiState.value.copy(
                isTesting = false,
                testResult = if (result.success) {
                    TestResult.Success(result.message)
                } else {
                    TestResult.Error(result.message)
                }
            )
        }
    }

    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }

    fun saveConfig(): Boolean {
        val state = _uiState.value

        // 预设配置不能保存
        if (state.isPreset) {
            _uiState.value = state.copy(errorMessage = "预设配置不能修改")
            return false
        }

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
    val isPreset: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val errorMessage: String? = null,
    val recentCallRecords: List<APICallRecord> = emptyList()
)

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}
