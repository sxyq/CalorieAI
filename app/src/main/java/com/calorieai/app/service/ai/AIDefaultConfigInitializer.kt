package com.calorieai.app.service.ai

import com.calorieai.app.BuildConfig
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.IconType
import com.calorieai.app.data.repository.AIConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIDefaultConfigInitializer @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    companion object {
        const val DEFAULT_AI_ID = "default_longcat_ai"
        const val DEFAULT_AI_NAME = "GPT-5.6 Luna (默认)"
        val DEFAULT_API_URL: String get() = BuildConfig.DEFAULT_AI_API_URL
        val DEFAULT_MODEL_ID: String get() = BuildConfig.DEFAULT_AI_MODEL_ID
        const val DEFAULT_DAILY_LIMIT = 50
    }

    suspend fun initializeDefaultConfig() {
        aiConfigRepository.migrateStoredApiKeysIfNeeded()
        val configs = aiConfigRepository.getAllConfigsOnce()

        if (configs.isEmpty()) {
            val defaultConfig = AIConfig(
                id = DEFAULT_AI_ID,
                name = DEFAULT_AI_NAME,
                icon = "🐐",
                iconType = IconType.EMOJI,
                protocol = AIProtocol.OPENAI,
                apiUrl = DEFAULT_API_URL,
                apiKey = "",
                modelId = DEFAULT_MODEL_ID,
                isImageUnderstanding = true,
                isDefault = true
            )
            aiConfigRepository.addConfig(defaultConfig)
            return
        }

        configs.forEach { config ->
            if (config.id == DEFAULT_AI_ID && isLegacyDefaultConfig(config)) {
                val migratedConfig = config.copy(
                    apiUrl = DEFAULT_API_URL,
                    apiKey = config.apiKey,
                    modelId = DEFAULT_MODEL_ID,
                    protocol = AIProtocol.OPENAI
                )
                aiConfigRepository.updateConfig(migratedConfig)
                return@forEach
            }

            val needsFix = !config.apiUrl.contains("/chat/completions")
            if (!needsFix) return@forEach

            val fixedUrl = when {
                config.apiUrl.endsWith("/") -> "${config.apiUrl}v1/chat/completions"
                config.apiUrl.contains("/v1") -> "${config.apiUrl}/chat/completions"
                else -> "${config.apiUrl}/v1/chat/completions"
            }
            aiConfigRepository.updateConfig(config.copy(apiUrl = fixedUrl))
        }
    }

    private fun isLegacyDefaultConfig(config: AIConfig): Boolean {
        return config.apiUrl.contains("api.longcat.chat", ignoreCase = true) ||
            config.modelId.startsWith("LongCat-", ignoreCase = true)
    }
}
