package com.calorieai.app.service.ai

import com.calorieai.app.data.model.MealPlanResponse
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import kotlinx.coroutines.flow.Flow
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
    private val aiDefaultConfigInitializer: AIDefaultConfigInitializer,
    private val aiContextService: AIContextService,
    private val mealPlanService: MealPlanService
) {

    companion object {
        const val DEFAULT_DAILY_LIMIT = 50

        private const val SYSTEM_PROMPT = """你是一位专业的营养师和健康顾问。请遵循以下规则：

1. 回答简洁明了，控制在200字以内
2. 使用Markdown格式增强可读性：
   - 用**粗体**强调重点
   - 用列表展示多条建议
   - 用>引用重要提示
3. 结构清晰：先给结论，再列要点
4. 避免冗长的开场白和客套话
5. 提供具体数据和建议
6. 不确定时建议咨询专业医生"""
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

    /**
     * 流式发送消息 - 返回Flow<String>实现打字机效果
     */
    fun sendMessageStream(message: String): Flow<String> {
        return kotlinx.coroutines.flow.flow {
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

            // 记录调用
            aiRateLimiter.recordCall(config.id)

            // 使用流式API
            aiApiClient.chatStream(
                config = config,
                systemPrompt = SYSTEM_PROMPT,
                userMessage = message,
                temperature = 0.7,
                maxTokens = 1000
            ).collect { char ->
                emit(char)
            }
        }
    }

    /**
     * 发送带上下文的消息
     */
    suspend fun sendMessageWithContext(message: String, context: String): String {
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

        // 构建带上下文的完整消息
        val fullMessage = """
            以下是用户的健康数据上下文：
            
            $context
            
            用户问题：$message
            
            请基于以上数据回答用户的问题，提供具体的分析和建议。如果数据不足，请说明需要更多记录才能提供准确分析。
        """.trimIndent()

        try {
            val (content, rawResponse) = aiApiClient.chatRaw(
                config = config,
                systemPrompt = SYSTEM_PROMPT,
                userMessage = fullMessage,
                temperature = 0.7,
                maxTokens = 1500
            )

            aiRateLimiter.recordCall(config.id)
            recordTokenUsage(config.id, config.name, rawResponse, config.protocol.name, config.modelId)

            return content
        } catch (e: AIApiException) {
            throw Exception("API调用失败: ${e.message}")
        }
    }

    /**
     * 评估热量消耗是否合理
     */
    suspend fun assessCalorieIntake(): String {
        val hasData = aiContextService.hasEnoughDataForAnalysis()
        if (!hasData) {
            return "您最近一周没有饮食记录，无法进行热量评估。请先记录几天的饮食数据，我就能帮您分析热量摄入是否合理了！\n\n建议：每天记录三餐和零食，这样我可以更准确地评估您的饮食习惯。"
        }
        
        val context = aiContextService.getHealthAssessmentContext()
        return sendMessageWithContext(
            message = "请评估我最近一周的热量摄入是否合理，并给出改进建议。",
            context = context
        )
    }

    /**
     * 规划健康菜谱
     * 使用缓存机制，避免重复调用AI
     */
    suspend fun planHealthyMeals(): String {
        val result = mealPlanService.getMealPlan()
        
        return result.fold(
            onSuccess = { response ->
                formatMealPlanResponse(response)
            },
            onFailure = { error ->
                // 如果缓存失败，回退到原来的方式
                val context = aiContextService.getWeeklyFoodContext()
                sendMessageWithContext(
                    message = "请根据我最近的饮食习惯，为我规划今天的健康菜谱，包括早餐、午餐、晚餐和加餐。",
                    context = context
                )
            }
        )
    }
    
    /**
     * 格式化菜谱响应为可读文本
     */
    private fun formatMealPlanResponse(response: MealPlanResponse): String {
        val plan = response.plan
        val sb = StringBuilder()
        
        sb.append("🍽️ 今日健康菜谱推荐\n\n")
        
        // 早餐
        sb.append("☀️ 早餐：${plan.breakfast.name}\n")
        sb.append("   ${plan.breakfast.description}\n")
        sb.append("   热量：${plan.breakfast.calories}kcal | 蛋白质：${plan.breakfast.protein.toInt()}g\n")
        if (plan.breakfast.tips.isNotBlank()) {
            sb.append("   💡 ${plan.breakfast.tips}\n")
        }
        sb.append("\n")
        
        // 午餐
        sb.append("🌤️ 午餐：${plan.lunch.name}\n")
        sb.append("   ${plan.lunch.description}\n")
        sb.append("   热量：${plan.lunch.calories}kcal | 蛋白质：${plan.lunch.protein.toInt()}g\n")
        if (plan.lunch.tips.isNotBlank()) {
            sb.append("   💡 ${plan.lunch.tips}\n")
        }
        sb.append("\n")
        
        // 晚餐
        sb.append("🌙 晚餐：${plan.dinner.name}\n")
        sb.append("   ${plan.dinner.description}\n")
        sb.append("   热量：${plan.dinner.calories}kcal | 蛋白质：${plan.dinner.protein.toInt()}g\n")
        if (plan.dinner.tips.isNotBlank()) {
            sb.append("   💡 ${plan.dinner.tips}\n")
        }
        sb.append("\n")
        
        // 加餐
        if (plan.snacks.isNotEmpty()) {
            sb.append("🍎 加餐：\n")
            plan.snacks.forEach { snack ->
                sb.append("   • ${snack.name} (${snack.calories}kcal)\n")
            }
            sb.append("\n")
        }
        
        // 总计
        sb.append("📊 今日营养总计：\n")
        sb.append("   总热量：${plan.totalCalories}kcal\n")
        sb.append("   蛋白质：${plan.totalProtein.toInt()}g\n")
        sb.append("   碳水化合物：${plan.totalCarbs.toInt()}g\n")
        sb.append("   脂肪：${plan.totalFat.toInt()}g\n\n")
        
        // 个性化建议
        if (response.personalizedTips.isNotEmpty()) {
            sb.append("💡 营养建议：\n")
            response.personalizedTips.forEach { tip ->
                sb.append("   • $tip\n")
            }
        }
        
        return sb.toString()
    }

    /**
     * 健康咨询
     */
    suspend fun healthConsult(): String {
        val hasData = aiContextService.hasEnoughDataForAnalysis()
        val context = if (hasData) {
            aiContextService.getHealthAssessmentContext()
        } else {
            "用户暂无健康数据记录。"
        }
        
        return sendMessageWithContext(
            message = "请根据我的健康数据，提供一些营养健康建议和改善方向。",
            context = context
        )
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
