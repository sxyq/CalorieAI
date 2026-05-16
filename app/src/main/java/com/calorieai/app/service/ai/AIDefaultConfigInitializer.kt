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
        const val DEFAULT_AI_NAME = "LongCat AI (默认)"
        const val DEFAULT_API_URL = "https://api.longcat.chat/openai/v1/chat/completions"
        const val DEFAULT_MODEL_ID = "LongCat-Flash-Omni-2603"
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
                apiKey = BuildConfig.DEFAULT_LONGCAT_API_KEY.trim(),
                modelId = DEFAULT_MODEL_ID,
                isImageUnderstanding = true,
                isDefault = true
            )
            aiConfigRepository.addConfig(defaultConfig)
            return
        }

        configs.forEach { config ->
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
}
