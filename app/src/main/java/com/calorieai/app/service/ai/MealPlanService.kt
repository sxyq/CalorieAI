package com.calorieai.app.service.ai

import android.content.Context
import com.calorieai.app.data.model.MealPlan
import com.calorieai.app.data.model.MealPlanResponse
import com.calorieai.app.data.model.MealSuggestion
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 菜谱规划服务
 * 负责生成和缓存菜谱推荐
 */
@Singleton
class MealPlanService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository,
    private val apiCallRecordRepository: APICallRecordRepository,
    private val foodRecordRepository: FoodRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiContextService: AIContextService
) {
    private val gson = Gson()
    private val cacheDir = File(context.cacheDir, "meal_plans")

    private data class ParsedUsage(
        val promptTokens: Int,
        val completionTokens: Int,
        val cost: Double
    )
    
    companion object {
        private const val CACHE_DURATION_HOURS = 24L
        private const val CACHE_FILE_NAME = "current_meal_plan.json"
        
        private const val MEAL_PLAN_PROMPT = """
你是一位专业的营养师。根据用户最近一周的饮食数据，为用户规划今天的健康菜谱。

请返回JSON格式的菜谱计划，格式如下：
{
  "breakfast": {
    "name": "餐食名称",
    "description": "简短描述",
    "ingredients": [
      {"name": "食材名", "amount": "用量", "calories": 100}
    ],
    "calories": 400,
    "protein": 15.0,
    "carbs": 50.0,
    "fat": 12.0,
    "cookingTime": 15,
    "difficulty": "简单",
    "tips": "烹饪小贴士"
  },
  "lunch": { ... },
  "dinner": { ... },
  "snacks": [
    { ... }
  ],
  "totalCalories": 1800,
  "totalProtein": 80.0,
  "totalCarbs": 200.0,
  "totalFat": 60.0,
  "nutritionTips": ["营养建议1", "营养建议2"]
}

要求：
1. 根据用户最近的饮食习惯和营养摄入情况，提供个性化的菜谱建议
2. 确保营养均衡，补充用户可能缺乏的营养素
3. 考虑用户的口味偏好（从历史记录推断）
4. 提供实用的烹饪建议
5. 热量目标应该接近用户的每日需求
"""
    }
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * 获取菜谱推荐
     * 如果缓存有效则返回缓存，否则生成新的
     */
    suspend fun getMealPlan(): Result<MealPlanResponse> {
        // 检查缓存
        val cachedPlan = getCachedMealPlan()
        if (cachedPlan != null) {
            return Result.success(cachedPlan)
        }
        
        // 生成新的菜谱
        return generateNewMealPlan()
    }
    
    /**
     * 获取实时推荐
     * 根据当前时间和用户用餐习惯推荐
     */
    suspend fun getRealTimeRecommendation(): Result<MealSuggestion> {
        val cachedPlan = getCachedMealPlan()
        
        if (cachedPlan != null) {
            // 根据当前时间返回对应的餐食
            val hour = java.time.LocalTime.now().hour
            return when {
                hour in 6..10 -> Result.success(cachedPlan.plan.breakfast)
                hour in 11..14 -> Result.success(cachedPlan.plan.lunch)
                hour in 17..20 -> Result.success(cachedPlan.plan.dinner)
                else -> {
                    // 返回加餐或最近的餐食
                    cachedPlan.plan.snacks.firstOrNull()?.let {
                        Result.success(it)
                    } ?: Result.success(cachedPlan.plan.breakfast)
                }
            }
        }
        
        // 没有缓存，生成新的
        val result = generateNewMealPlan()
        return result.map { response ->
            val hour = java.time.LocalTime.now().hour
            when {
                hour in 6..10 -> response.plan.breakfast
                hour in 11..14 -> response.plan.lunch
                hour in 17..20 -> response.plan.dinner
                else -> response.plan.snacks.firstOrNull() ?: response.plan.breakfast
            }
        }
    }
    
    /**
     * 强制刷新菜谱缓存
     */
    suspend fun refreshMealPlan(): Result<MealPlanResponse> {
        clearCache()
        return generateNewMealPlan()
    }
    
    /**
     * 生成新的菜谱计划
     */
    private suspend fun generateNewMealPlan(): Result<MealPlanResponse> {
        return try {
            // 获取用户数据上下文
            val dataContext = aiContextService.getHealthAssessmentContext()
            
            // 获取用户设置
            val settings = userSettingsRepository.getSettings().firstOrNull()
            val calorieTarget = settings?.dailyCalorieGoal ?: 2000
            
            // 获取AI配置
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return Result.failure(Exception("未配置AI服务"))
            
            // 构建完整提示
            val fullPrompt = """
$MEAL_PLAN_PROMPT

用户每日热量目标：$calorieTarget kcal

用户最近一周的数据：
$dataContext

请根据以上数据生成个性化的菜谱计划。只返回JSON，不要有其他文字。
"""

            val startTime = System.currentTimeMillis()
            // 调用AI
            val (content, rawResponse) = try {
                aiApiClient.chatRaw(
                    config = config,
                    systemPrompt = "你是一位专业的营养师，擅长根据用户的饮食习惯规划健康菜谱。请只返回JSON格式的数据，不要有其他文字。",
                    userMessage = fullPrompt,
                    temperature = 0.7,
                    maxTokens = 2000
                )
            } catch (e: Exception) {
                recordApiCall(
                    configId = config.id,
                    configName = config.name,
                    modelId = config.modelId,
                    inputText = fullPrompt,
                    outputText = "",
                    rawResponse = null,
                    protocol = config.protocol.name,
                    duration = System.currentTimeMillis() - startTime,
                    isSuccess = false,
                    errorMessage = e.message
                )
                throw e
            }

            recordApiCall(
                configId = config.id,
                configName = config.name,
                modelId = config.modelId,
                inputText = fullPrompt,
                outputText = content,
                rawResponse = rawResponse,
                protocol = config.protocol.name,
                duration = System.currentTimeMillis() - startTime,
                isSuccess = true
            )
            
            // 解析响应
            val mealPlan = parseMealPlan(content)
                ?: return Result.failure(Exception("解析菜谱失败"))
            
            // 创建响应
            val response = MealPlanResponse(
                plan = mealPlan,
                personalizedTips = generatePersonalizedTips(dataContext, mealPlan),
                alternatives = emptyList()
            )
            
            // 缓存结果
            saveCache(response, dataContext)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
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
    
    /**
     * 解析菜谱JSON
     */
    private fun parseMealPlan(jsonContent: String): MealPlan? {
        return try {
            // 提取JSON部分
            val json = extractJson(jsonContent)
            gson.fromJson(json, MealPlan::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从响应中提取JSON
     */
    private fun extractJson(content: String): String {
        // 尝试提取代码块中的JSON
        val jsonBlockRegex = """```(?:json)?\s*([\s\S]*?)```""".toRegex()
        val match = jsonBlockRegex.find(content)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        
        // 尝试直接解析
        val startIndex = content.indexOf('{')
        val endIndex = content.lastIndexOf('}')
        if (startIndex >= 0 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1)
        }
        
        return content
    }
    
    /**
     * 生成个性化建议
     */
    private fun generatePersonalizedTips(context: String, plan: MealPlan): List<String> {
        val tips = mutableListOf<String>()
        
        // 根据热量分析
        if (plan.totalCalories < 1500) {
            tips.add("今日菜谱热量偏低，可以适当增加一些健康零食")
        } else if (plan.totalCalories > 2500) {
            tips.add("今日菜谱热量较高，建议适当减少份量或增加运动")
        }
        
        // 根据营养均衡
        val proteinRatio = plan.totalProtein * 4 / plan.totalCalories
        if (proteinRatio < 0.15) {
            tips.add("蛋白质摄入比例较低，建议增加优质蛋白来源")
        }
        
        tips.add("建议每天喝够8杯水，保持身体水分充足")
        tips.add("细嚼慢咽有助于消化和控制食量")
        
        return tips
    }
    
    /**
     * 获取缓存的菜谱
     */
    private fun getCachedMealPlan(): MealPlanResponse? {
        return try {
            val cacheFile = File(cacheDir, CACHE_FILE_NAME)
            if (!cacheFile.exists()) return null
            
            val cacheContent = cacheFile.readText()
            val cacheData = gson.fromJson(cacheContent, MealPlanCacheData::class.java)
            
            // 检查是否过期
            if (System.currentTimeMillis() > cacheData.expiresAt) {
                cacheFile.delete()
                return null
            }
            
            cacheData.response
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 保存缓存
     */
    private fun saveCache(response: MealPlanResponse, context: String) {
        try {
            val cacheFile = File(cacheDir, CACHE_FILE_NAME)
            val expiresAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS)
            
            val cacheData = MealPlanCacheData(
                createdAt = System.currentTimeMillis(),
                expiresAt = expiresAt,
                weekDataSummary = context.take(500),
                response = response
            )
            
            cacheFile.writeText(gson.toJson(cacheData))
        } catch (e: Exception) {
            // 忽略缓存保存错误
        }
    }
    
    /**
     * 清除缓存
     */
    private fun clearCache() {
        try {
            val cacheFile = File(cacheDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        } catch (e: Exception) {
            // 忽略
        }
    }
    
    /**
     * 检查缓存是否有效
     */
    fun isCacheValid(): Boolean {
        return try {
            val cacheFile = File(cacheDir, CACHE_FILE_NAME)
            if (!cacheFile.exists()) return false
            
            val cacheContent = cacheFile.readText()
            val cacheData = gson.fromJson(cacheContent, MealPlanCacheData::class.java)
            
            System.currentTimeMillis() <= cacheData.expiresAt
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 缓存数据结构
 */
private data class MealPlanCacheData(
    val createdAt: Long,
    val expiresAt: Long,
    val weekDataSummary: String,
    val response: MealPlanResponse
)
