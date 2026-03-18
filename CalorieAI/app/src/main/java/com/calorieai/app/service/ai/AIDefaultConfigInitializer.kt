package com.calorieai.app.service.ai

import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.IconType
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认AI配置初始化器
 * 应用启动时检查并创建默认AI配置
 */
@Singleton
class AIDefaultConfigInitializer @Inject constructor(
    private val aiConfigDao: AIConfigDao
) {
    companion object {
        const val DEFAULT_AI_ID = "default_longcat_ai"
        const val DEFAULT_AI_NAME = "LongCat AI (默认)"
        const val DEFAULT_API_URL = "https://api.longcat.chat/openai/v1/chat/completions"
        const val DEFAULT_API_KEY = "ak_1qe7Ym0Yp8Hs3qa74O5wt2gy6Rt6I"
        const val DEFAULT_MODEL_ID = "LongCat-Flash-Omni-2603"
        const val DEFAULT_DAILY_LIMIT = 50
    }

    suspend fun initializeDefaultConfig() {
        val configs = aiConfigDao.getAllConfigs().first()
        
        if (configs.isEmpty()) {
            val defaultConfig = AIConfig(
                id = DEFAULT_AI_ID,
                name = DEFAULT_AI_NAME,
                icon = "🐱",
                iconType = IconType.EMOJI,
                protocol = AIProtocol.OPENAI,
                apiUrl = DEFAULT_API_URL,
                apiKey = DEFAULT_API_KEY,
                modelId = DEFAULT_MODEL_ID,
                isImageUnderstanding = true,
                isDefault = true
            )
            aiConfigDao.insertConfig(defaultConfig)
        } else {
            configs.forEach { config ->
                val needsFix = !config.apiUrl.contains("/chat/completions")
                if (needsFix) {
                    val fixedUrl = when {
                        config.apiUrl.endsWith("/") -> "${config.apiUrl}v1/chat/completions"
                        config.apiUrl.contains("/v1") -> "${config.apiUrl}/chat/completions"
                        else -> "${config.apiUrl}/v1/chat/completions"
                    }
                    aiConfigDao.updateConfig(config.copy(apiUrl = fixedUrl))
                }
            }
        }
    }
}
