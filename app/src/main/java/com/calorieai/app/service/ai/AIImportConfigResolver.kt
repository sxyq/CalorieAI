package com.calorieai.app.service.ai

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIConfigPresets
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.AIFunctionType
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AIFunctionConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIImportConfigResolver @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    private val aiFunctionConfigRepository: AIFunctionConfigRepository
) {
    suspend fun resolveTextConfig(): AIConfig? {
        return resolveConfiguredImportConfig(
            functionType = AIFunctionType.FOOD_TEXT_ANALYSIS,
            requireImageUnderstanding = false
        )
    }

    suspend fun resolveImageConfig(): AIConfig? {
        return resolveConfiguredImportConfig(
            functionType = AIFunctionType.FOOD_IMAGE_ANALYSIS,
            requireImageUnderstanding = true
        )
    }

    private suspend fun resolveConfiguredImportConfig(
        functionType: AIFunctionType,
        requireImageUnderstanding: Boolean
    ): AIConfig? {
        val fallbackConfig = resolveFallbackConfig()
        val configuredId = runCatching {
            aiFunctionConfigRepository.getFunctionConfigId(functionType)
        }.getOrNull()

        val configured = configuredId
            ?.let { resolveConfigById(it, fallbackConfig) }
            ?.takeIf { matchesCapability(it, requireImageUnderstanding) }

        return configured ?: fallbackConfig?.takeIf {
            matchesCapability(it, requireImageUnderstanding)
        }
    }

    private suspend fun resolveFallbackConfig(): AIConfig? {
        val defaultConfig = aiConfigRepository.getDefaultConfig().firstOrNull()
        val allConfigs = aiConfigRepository.getAllConfigs().firstOrNull().orEmpty()

        return when {
            defaultConfig?.hasUserProvidedApiKey() == true -> defaultConfig
            else -> allConfigs.firstOrNull { it.hasUserProvidedApiKey() }
                ?: defaultConfig
                ?: allConfigs.firstOrNull()
        }
    }

    private suspend fun resolveConfigById(id: String, fallbackConfig: AIConfig?): AIConfig? {
        aiConfigRepository.getConfigById(id)
            ?.let { stored ->
                return hydrateStoredConfig(stored, fallbackConfig)
            }

        val preset = AIConfigPresets.getById(id) ?: return null
        val apiKey = fallbackConfig?.apiKey?.trim().orEmpty()
        if (apiKey.isBlank()) return null

        val apiUrl = when {
            fallbackConfig?.apiUrl?.contains("longcat.chat", ignoreCase = true) == true -> fallbackConfig.apiUrl
            fallbackConfig?.protocol == AIProtocol.LONGCAT -> fallbackConfig.apiUrl
            else -> preset.apiUrl
        }

        return preset.copy(
            apiUrl = apiUrl,
            apiKey = apiKey,
            protocol = AIProtocol.LONGCAT
        )
    }

    private fun hydrateStoredConfig(config: AIConfig, fallbackConfig: AIConfig?): AIConfig {
        if (config.apiKey.isNotBlank()) return config
        val fallbackKey = fallbackConfig?.apiKey?.trim().orEmpty()
        return if (fallbackKey.isBlank()) {
            config
        } else {
            config.copy(apiKey = fallbackKey)
        }
    }

    private fun matchesCapability(config: AIConfig, requireImageUnderstanding: Boolean): Boolean {
        return !requireImageUnderstanding || config.isImageUnderstanding || isOmniLikeModel(config.modelId)
    }

    private fun isOmniLikeModel(modelId: String): Boolean {
        return modelId.contains("omni", ignoreCase = true) ||
            modelId.contains("o-mini", ignoreCase = true) ||
            modelId.contains("omini", ignoreCase = true)
    }

    private fun AIConfig.hasUserProvidedApiKey(): Boolean {
        return apiKey.trim().isNotBlank()
    }
}
