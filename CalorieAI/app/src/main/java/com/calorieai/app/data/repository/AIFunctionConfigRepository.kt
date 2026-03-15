package com.calorieai.app.data.repository

import com.calorieai.app.data.model.AIFunctionConfig
import com.calorieai.app.data.model.AIFunctionConfigDefaults
import com.calorieai.app.data.model.AIFunctionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI功能配置Repository
 * 管理不同功能使用的AI配置
 */
@Singleton
class AIFunctionConfigRepository @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    /**
     * 获取所有功能配置
     */
    fun getAllFunctionConfigs(): Flow<List<AIFunctionConfig>> = flow {
        // 从本地存储获取，如果没有则使用默认值
        val configs = AIFunctionType.entries.map { type ->
            getFunctionConfig(type)
        }
        emit(configs)
    }

    /**
     * 获取指定功能的配置
     */
    suspend fun getFunctionConfig(type: AIFunctionType): AIFunctionConfig {
        // TODO: 从本地存储读取，如果没有则返回默认值
        return AIFunctionConfig(
            functionType = type,
            configId = getDefaultConfigId(type)
        )
    }

    /**
     * 获取指定功能的配置ID
     */
    suspend fun getFunctionConfigId(type: AIFunctionType): String {
        return getFunctionConfig(type).configId
    }

    /**
     * 设置指定功能的配置
     */
    suspend fun setFunctionConfig(type: AIFunctionType, configId: String) {
        // TODO: 保存到本地存储
        val config = AIFunctionConfig(
            functionType = type,
            configId = configId
        )
        // 保存到数据库或SharedPreferences
    }

    /**
     * 获取默认配置ID
     */
    private fun getDefaultConfigId(type: AIFunctionType): String {
        return when (type) {
            AIFunctionType.FOOD_IMAGE_ANALYSIS -> AIFunctionConfigDefaults.DEFAULT_IMAGE_ANALYSIS_CONFIG_ID
            AIFunctionType.FOOD_TEXT_ANALYSIS -> AIFunctionConfigDefaults.DEFAULT_TEXT_ANALYSIS_CONFIG_ID
            AIFunctionType.AI_CHAT -> AIFunctionConfigDefaults.DEFAULT_CHAT_CONFIG_ID
        }
    }
}
