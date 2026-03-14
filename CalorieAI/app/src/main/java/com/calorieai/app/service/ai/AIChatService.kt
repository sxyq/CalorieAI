package com.calorieai.app.service.ai

import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 聊天服务
 * 使用统一的 AIApiClient 发送请求
 */
@Singleton
class AIChatService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository,
    private val aiRateLimiter: AIRateLimiter,
    private val aiDefaultConfigInitializer: AIDefaultConfigInitializer
) {

    companion object {
        const val DEFAULT_DAILY_LIMIT = 50

        private const val SYSTEM_PROMPT = "你是一位专业的营养师和健康顾问。请根据用户的问题提供准确、实用的营养和健康建议。回答要简洁明了，适合普通用户理解。提供具体的建议和数据支持。如果不确定，建议用户咨询专业医生。保持友好、鼓励的语气。"
    }

    suspend fun sendMessage(message: String): String {
        // 确保默认配置已初始化
        var config = aiConfigRepository.getDefaultConfig().firstOrNull()
        if (config == null) {
            aiDefaultConfigInitializer.initializeDefaultConfig()
            config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: throw Exception("无法初始化AI服务配置")
        }

        val (canCall, _) = aiRateLimiter.canMakeCall(config.id, DEFAULT_DAILY_LIMIT)
        if (!canCall) {
            throw Exception("今日API调用次数已用完（限制：${DEFAULT_DAILY_LIMIT}次/天），请明天再试")
        }

        try {
            val (content, rawResponse) = aiApiClient.chatRaw(
                config = config,
                systemPrompt = SYSTEM_PROMPT,
                userMessage = message,
                temperature = 0.7,
                maxTokens = 1000
            )

            aiRateLimiter.recordCall(config.id)
            recordTokenUsage(config.id, config.name, rawResponse, config.protocol.name, config.modelId)

            return content
        } catch (e: AIApiException) {
            throw Exception("API调用失败: ${e.message}")
        }
    }

    suspend fun getRemainingCalls(): Int {
        val config = aiConfigRepository.getDefaultConfig().firstOrNull()
            ?: return 0
        return aiRateLimiter.getRemainingCalls(config.id, DEFAULT_DAILY_LIMIT)
    }

    suspend fun getTodayCallCount(): Int {
        val config = aiConfigRepository.getDefaultConfig().firstOrNull()
            ?: return 0
        return aiRateLimiter.getTodayCallCount(config.id)
    }

    private suspend fun recordTokenUsage(
        configId: String,
        configName: String,
        rawResponse: String,
        protocol: String,
        modelId: String
    ) {
        try {
            val usage = when (protocol) {
                "CLAUDE" -> {
                    val u = aiApiClient.extractClaudeUsage(rawResponse)
                    if (u != null) (u.promptTokens ?: 0) to (u.completionTokens ?: 0) else null
                }
                else -> {
                    val u = aiApiClient.extractOpenAIUsage(rawResponse)
                    if (u != null) (u.promptTokens ?: 0) to (u.completionTokens ?: 0) else null
                }
            }

            if (usage != null) {
                val (promptTokens, completionTokens) = usage
                val cost = calculateCost(promptTokens, completionTokens, protocol, modelId)
                aiTokenUsageRepository.recordTokenUsage(
                    configId = configId,
                    configName = configName,
                    promptTokens = promptTokens,
                    completionTokens = completionTokens,
                    cost = cost
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateCost(promptTokens: Int, completionTokens: Int, protocol: String, modelId: String): Double {
        val rates = when (protocol) {
            "OPENAI" -> when {
                modelId.contains("gpt-4") -> 0.03 to 0.06
                modelId.contains("gpt-3.5") -> 0.0015 to 0.002
                else -> 0.001 to 0.002
            }
            "CLAUDE" -> 0.008 to 0.024
            "KIMI" -> 0.006 to 0.006
            else -> 0.001 to 0.002
        }

        val (inputRate, outputRate) = rates
        return (promptTokens * inputRate + completionTokens * outputRate) / 1000.0
    }
}
