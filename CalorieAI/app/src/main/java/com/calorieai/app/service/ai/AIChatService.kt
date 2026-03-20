package com.calorieai.app.service.ai

import com.calorieai.app.data.model.MealPlanResponse
import com.calorieai.app.data.repository.APICallRecordRepository
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
    private val apiCallRecordRepository: APICallRecordRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository,
    private val aiRateLimiter: AIRateLimiter,
    private val aiDefaultConfigInitializer: AIDefaultConfigInitializer,
    private val aiContextService: AIContextService,
    private val mealPlanService: MealPlanService
) {

    companion object {
        const val DEFAULT_DAILY_LIMIT = 50

        private const val SYSTEM_PROMPT = """你是一位专业的营养师和健康顾问。请严格按以下格式回答：

1. 先给结论，后给步骤，避免寒暄和空话
2. 使用Markdown并保持结构化，优先使用以下标题：
   ### 总结
   ### 执行步骤
   ### 注意事项
3. 每个标题下尽量用列表，单条不超过2句
4. 关键数字、阈值、时间点必须用**粗体**
5. 语气要直接、可执行，给出可落地动作
6. 不确定或高风险场景要明确提示“请咨询医生/专业人士”
7. 默认控制在300字内；如果用户要求详细，再展开
8. 涉及菜谱与健康建议时，必须结合：
   - 最近7天和30天的营养缺口分析
   - 食材缺失时的替代建议，并重算热量与三大营养素
   - 用户特定人群模式（如控糖/痛风/孕期/儿童/健身）
   - 用户忌口、口味、预算、烹饪时长约束"""
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

        val startTime = System.currentTimeMillis()
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
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = message,
                outputText = content,
                rawResponse = rawResponse,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = true
            )

            return content
        } catch (e: AIApiException) {
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = message,
                outputText = "",
                rawResponse = null,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = false,
                errorMessage = e.message
            )
            throw Exception("API调用失败: ${e.message}")
        } catch (e: Exception) {
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = message,
                outputText = "",
                rawResponse = null,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = false,
                errorMessage = e.message
            )
            throw e
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
            val startTime = System.currentTimeMillis()
            val outputBuilder = StringBuilder()

            try {
                // 使用流式API
                aiApiClient.chatStream(
                    config = config,
                    systemPrompt = SYSTEM_PROMPT,
                    userMessage = message,
                    temperature = 0.7,
                    maxTokens = 1000
                ).collect { char ->
                    outputBuilder.append(char)
                    emit(char)
                }

                recordApiCall(
                    configId = config.id,
                    configName = config.name,
                    modelId = config.modelId,
                    inputText = message,
                    outputText = outputBuilder.toString(),
                    rawResponse = null,
                    protocol = config.protocol.name,
                    duration = System.currentTimeMillis() - startTime,
                    isSuccess = true
                )
            } catch (e: AIApiException) {
                recordApiCall(
                    configId = config.id,
                    configName = config.name,
                    modelId = config.modelId,
                    inputText = message,
                    outputText = outputBuilder.toString(),
                    rawResponse = null,
                    protocol = config.protocol.name,
                    duration = System.currentTimeMillis() - startTime,
                    isSuccess = false,
                    errorMessage = e.message
                )
                throw Exception("API调用失败: ${e.message}")
            } catch (e: Exception) {
                recordApiCall(
                    configId = config.id,
                    configName = config.name,
                    modelId = config.modelId,
                    inputText = message,
                    outputText = outputBuilder.toString(),
                    rawResponse = null,
                    protocol = config.protocol.name,
                    duration = System.currentTimeMillis() - startTime,
                    isSuccess = false,
                    errorMessage = e.message
                )
                throw e
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

        val startTime = System.currentTimeMillis()
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
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = fullMessage,
                outputText = content,
                rawResponse = rawResponse,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = true
            )

            return content
        } catch (e: AIApiException) {
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = fullMessage,
                outputText = "",
                rawResponse = null,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = false,
                errorMessage = e.message
            )
            throw Exception("API调用失败: ${e.message}")
        } catch (e: Exception) {
            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = fullMessage,
                outputText = "",
                rawResponse = null,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = false,
                errorMessage = e.message
            )
            throw e
        }
    }

    /**
     * 评估热量消耗是否合理
     */
    suspend fun assessCalorieIntake(): String {
        val hasData = aiContextService.hasEnoughFoodDataForCalorieAssessment()
        if (!hasData) {
            return "近期可用于热量评估的饮食数据不足（建议至少记录3天且包含主餐）。请先补充近期饮食记录后再评估。\n\n建议：连续记录早餐/午餐/晚餐和加餐，我会基于本地近期数据做更准确分析。"
        }

        val context = aiContextService.getQuickActionContext(
            action = "热量评估（校验近期热量收支与摄入结构）",
            recentDays = 14
        )
        return sendMessageWithContext(
            message = "请基于本地近期数据，评估我近期热量摄入是否合理，并给出可执行的改进建议。",
            context = context
        )
    }

    /**
     * 规划健康菜谱
     * 使用缓存机制，避免重复调用AI
     */
    suspend fun planHealthyMeals(): String {
        val localContext = aiContextService.getAdvancedDietGuidanceContext(
            action = "菜谱规划（基于近期饮食/运动/体重/饮水与目标生成当日方案）",
            recentDays = 14
        )
        val result = mealPlanService.getMealPlan()
        
        return result.fold(
            onSuccess = { response ->
                formatMealPlanResponse(response, localContext)
            },
            onFailure = { _ ->
                // 如果缓存失败，回退到原来的方式
                sendMessageWithContext(
                    message = "请基于本地近期数据和个性化约束，为我规划今天的健康菜谱（早餐/午餐/晚餐/加餐），并说明每餐设计理由。需要附带营养缺口补齐策略和食材替代方案。",
                    context = localContext
                )
            }
        )
    }

    /**
     * 规划未来7天周菜谱
     */
    suspend fun planWeeklyMeals(): String {
        val context = aiContextService.getAdvancedDietGuidanceContext(
            action = "周菜谱周计划（未来7天）",
            recentDays = 30
        )
        return sendMessageWithContext(
            message = """
请给我制定未来7天的菜谱周计划，要求：
1) 每天包含早餐/午餐/晚餐/可选加餐
2) 给出每餐热量与三大营养素估算
3) 先指出最近7天与30天的营养缺口，再说明本周如何补齐
4) 结合我的忌口、口味、预算、烹饪时长与特定人群模式
5) 对每一天给出一个“食材缺失替代方案”，并重算热量与三大营养素
6) 用Markdown表格输出，便于我直接执行
            """.trimIndent(),
            context = context
        )
    }

    /**
     * 下一餐智能推荐
     */
    suspend fun recommendNextMeal(): String {
        val context = aiContextService.getAdvancedDietGuidanceContext(
            action = "下一餐智能推荐（根据今日已摄入与剩余目标）",
            recentDays = 14
        )
        return sendMessageWithContext(
            message = """
请基于我今天已摄入情况推荐“下一餐”：
1) 给出主推荐（菜名+份量+预计热量与三大营养素）
2) 给出两个可替代方案（并重算热量与三大营养素）
3) 说明为什么这样推荐（对应我的营养缺口和目标）
4) 严格考虑忌口/口味/预算/烹饪时长/特定人群模式
5) 用可执行清单输出（买什么、做什么、吃多少）
            """.trimIndent(),
            context = context
        )
    }

    /**
     * 基于用户库存食材推荐可做菜谱
     */
    suspend fun recommendRecipesWithPantry(pantrySummary: String): String {
        val context = aiContextService.getAdvancedDietGuidanceContext(
            action = "库存食材可做菜谱推荐（优先消耗临期食材）",
            recentDays = 14
        )
        return sendMessageWithContext(
            message = """
请基于我提供的已知信息推荐可做菜谱，并满足：
1) 如果存在库存食材，优先使用即将过期的食材；如果库存为空，直接给出可执行菜谱并附可选采购清单
2) 严格结合我的近期健康数据和目标，给出做法与份量
3) 每道菜输出：食材及克数、步骤、难度、时长、份量、营养信息、厨具要求
4) 如果某食材不足，给出替代方案并重算营养
5) 用Markdown分节清晰输出，便于直接下厨

【我的已知信息（可能包含库存与偏好）】
$pantrySummary
            """.trimIndent(),
            context = context
        )
    }

    /**
     * 生成1~N天菜单计划（可按库存和健康数据动态生成）
     */
    suspend fun generatePantryMealPlan(pantrySummary: String, days: Int): String {
        val safeDays = days.coerceIn(1, 14)
        val context = aiContextService.getAdvancedDietGuidanceContext(
            action = "菜单计划生成（按天/多天）",
            recentDays = 30
        )
        return sendMessageWithContext(
            message = """
请生成一个${safeDays}天的菜单计划，要求：
1) 每天包含早餐/午餐/晚餐（可选加餐）
2) 若有库存食材则优先使用；若库存为空则按我的健康目标与偏好直接规划并附采购建议
3) 输出每天的菜名、份量、预计热量与三大营养素
4) 给出每餐简要做法和关键火候提醒
5) 缺失食材提供替代建议并重算营养
6) 使用Markdown表格+小节展示，便于执行

【我的已知信息（可能包含库存与偏好）】
$pantrySummary
            """.trimIndent(),
            context = context
        )
    }
    
    /**
     * 格式化菜谱响应为可读文本
     */
    private fun formatMealPlanResponse(response: MealPlanResponse, localContext: String): String {
        val plan = response.plan
        val sb = StringBuilder()
        
        sb.append("🍽️ 今日健康菜谱推荐（已读取本地近期数据）\n\n")
        sb.append("### 近期数据摘要\n")
        sb.append(localContext.lines().take(14).joinToString("\n"))
        sb.append("\n\n")
        
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
            aiContextService.getAdvancedDietGuidanceContext(
                action = "健康咨询（结合近期多维健康数据给出改善建议）",
                recentDays = 14
            )
        } else {
            "用户暂无可用的本地近期健康记录。请先补充饮食、运动、体重或饮水记录。"
        }
        
        return sendMessageWithContext(
            message = "请基于本地近期数据给出健康咨询建议，包含优先级、执行步骤、风险提醒，并明确最近7/30天营养缺口与饮食替代策略。",
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

    private data class ParsedUsage(
        val promptTokens: Int,
        val completionTokens: Int,
        val cost: Double
    )

    private suspend fun recordTokenUsage(
        configId: String,
        configName: String,
        rawResponse: String,
        protocol: String,
        modelId: String
    ) {
        try {
            val parsedUsage = parseUsage(rawResponse, protocol, modelId)
            if (parsedUsage.promptTokens > 0 || parsedUsage.completionTokens > 0) {
                aiTokenUsageRepository.recordTokenUsage(
                    configId = configId,
                    configName = configName,
                    promptTokens = parsedUsage.promptTokens,
                    completionTokens = parsedUsage.completionTokens,
                    cost = parsedUsage.cost
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun recordApiCall(
        configId: String,
        configName: String,
        modelId: String,
        inputText: String,
        outputText: String,
        rawResponse: String?,
        protocol: String,
        duration: Long,
        isSuccess: Boolean,
        errorMessage: String? = null
    ) {
        try {
            val parsedUsage = rawResponse?.let { parseUsage(it, protocol, modelId) }
                ?: ParsedUsage(promptTokens = 0, completionTokens = 0, cost = 0.0)
            apiCallRecordRepository.recordCall(
                configId = configId,
                configName = configName,
                modelId = modelId,
                inputText = inputText,
                outputText = outputText,
                promptTokens = parsedUsage.promptTokens,
                completionTokens = parsedUsage.completionTokens,
                cost = parsedUsage.cost,
                duration = duration,
                isSuccess = isSuccess,
                errorMessage = errorMessage
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseUsage(rawResponse: String, protocol: String, modelId: String): ParsedUsage {
        val usage = when (protocol) {
            "CLAUDE" -> aiApiClient.extractClaudeUsage(rawResponse)
            else -> aiApiClient.extractOpenAIUsage(rawResponse)
        }
        val promptTokens = usage?.promptTokens ?: 0
        val completionTokens = usage?.completionTokens ?: 0
        val cost = if (usage != null) {
            calculateCost(promptTokens, completionTokens, protocol, modelId)
        } else {
            0.0
        }
        return ParsedUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            cost = cost
        )
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
