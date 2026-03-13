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
        // 默认AI配置常量
        const val DEFAULT_AI_ID = "default_longcat_ai"
        const val DEFAULT_AI_NAME = "LongCat AI (默认)"
        const val DEFAULT_API_URL = "https://api.longcat.chat/openai"
        const val DEFAULT_API_KEY = "ak_1qe7Ym0Yp8Hs3qa74O5wt2gy6Rt6I"
        const val DEFAULT_MODEL_ID = "LongCat-Flash-Omni-2603"
        const val DEFAULT_DAILY_LIMIT = 50  // 每天调用限制
    }

    /**
     * 初始化默认AI配置
     * 如果没有配置，则创建默认配置
     */
    suspend fun initializeDefaultConfig() {
        val configs = aiConfigDao.getAllConfigs().first()
        
        // 如果没有配置，创建默认配置
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
        }
    }
}
