package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.model.FoodBatchAnalysisResult
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.calorieai.app.utils.SecureLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodTextAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository,
    private val apiCallRecordRepository: APICallRecordRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "FoodTextAnalysis"
        
        private const val SYSTEM_PROMPT = """你是专业营养师。用户可能一次输入多种食物，你必须按“每种食物一条记录”返回，不要合并。

【严格要求 - 必须遵守】
1. 返回JSON对象，且根字段必须是 items 数组
2. 数组每个元素代表一种食物，不能把多种食物合并成一条
3. foodName 字段必须使用中文名称（由你命名）
2. 必须严格使用英文标点符号（逗号、引号、冒号）
3. 所有数值必须是纯数字，不能带引号，不能使用字符串
4. 所有营养素字段必须返回，不能省略
5. 如果无法准确估算，使用合理的估计值（不能全部填0）

【13种必需营养素字段】
- 基础营养素（3种）：protein, carbs, fat
- 扩展营养素（10种）：fiber, sugar, saturatedFat, cholesterol, sodium, potassium, calcium, iron, vitaminA, vitaminC

【JSON格式示例】
{"items":[{"foodName":"空气炸锅大虾","estimatedWeight":200,"calories":198,"protein":35.0,"carbs":2.0,"fat":5.0,"fiber":0.0,"sugar":0.5,"saturatedFat":1.0,"cholesterol":180.0,"sodium":320.0,"potassium":280.0,"calcium":90.0,"iron":1.8,"vitaminA":40.0,"vitaminC":0.0},{"foodName":"小番茄","estimatedWeight":100,"calories":22,"protein":1.1,"carbs":4.8,"fat":0.2,"fiber":1.4,"sugar":3.2,"saturatedFat":0.0,"cholesterol":0.0,"sodium":5.0,"potassium":237.0,"calcium":10.0,"iron":0.3,"vitaminA":42.0,"vitaminC":14.0}]}

【格式检查清单】
✓ 所有字段名使用英文
✓ 所有数值不带引号（如：13.4 而不是 "13.4"）
✓ 使用英文逗号分隔
✓ 使用英文引号包裹字符串值
✓ 使用英文冒号分隔键值
✓ 返回完整的13种营养素数据

【禁止事项】
✗ 不要使用中文字段名
✗ 不要使用中文标点符号
✗ 不要将数字用引号包裹
✗ 不要省略任何营养素字段
✗ 不要返回说明文字，只返回JSON对象"""
    }

    private data class ParsedUsage(
        val promptTokens: Int,
        val completionTokens: Int,
        val cost: Double
    )

    suspend fun analyzeFoodText(
        foodDescription: String,
        maxRetries: Int = 2,
        onRetry: ((attempt: Int, maxAttempts: Int) -> Unit)? = null
    ): Result<FoodBatchAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))

            val userPrompt = "请拆分并分析以下食物，按多条items返回：$foodDescription"
            var lastError: Throwable? = null
            val maxAttempts = (maxRetries + 1).coerceAtLeast(1)

            for (attempt in 1..maxAttempts) {
                val startTime = System.currentTimeMillis()
                try {
                    val (responseText, rawResponse) = aiApiClient.chatRaw(
                        config = config,
                        systemPrompt = SYSTEM_PROMPT,
                        userMessage = userPrompt,
                        temperature = 0.2,
                        maxTokens = 1400
                    )
                    val parsedUsage = parseUsage(rawResponse, config.protocol.name, config.modelId)
                    if (parsedUsage.promptTokens > 0 || parsedUsage.completionTokens > 0) {
                        recordTokenUsage(config.id, config.name, parsedUsage)
                    }
                    val parsedItems = parseBatchAnalysisResult(responseText)
                    val validItems = parsedItems.filter { validateNutritionData(it).isValid }
                    if (validItems.isNotEmpty()) {
                        recordApiCall(
                            configId = config.id,
                            configName = config.name,
                            modelId = config.modelId,
                            inputText = userPrompt,
                            outputText = responseText,
                            promptTokens = parsedUsage.promptTokens,
                            completionTokens = parsedUsage.completionTokens,
                            cost = parsedUsage.cost,
                            duration = System.currentTimeMillis() - startTime,
                            isSuccess = true
                        )
                        SecureLogger.event(
                            TAG,
                            "batch_analysis_success",
                            "attempt" to attempt,
                            "itemCount" to validItems.size,
                            "inputLength" to foodDescription.length
                        )
                        return@withContext Result.success(FoodBatchAnalysisResult(items = validItems))
                    }
                    lastError = Exception("AI返回结果为空或无效")
                    recordApiCall(
                        configId = config.id,
                        configName = config.name,
                        modelId = config.modelId,
                        inputText = userPrompt,
                        outputText = responseText,
                        promptTokens = parsedUsage.promptTokens,
                        completionTokens = parsedUsage.completionTokens,
                        cost = parsedUsage.cost,
                        duration = System.currentTimeMillis() - startTime,
                        isSuccess = false,
                        errorMessage = lastError?.message
                    )
                } catch (e: Exception) {
                    lastError = e
                    recordApiCall(
                        configId = config.id,
                        configName = config.name,
                        modelId = config.modelId,
                        inputText = userPrompt,
                        outputText = "",
                        promptTokens = 0,
                        completionTokens = 0,
                        cost = 0.0,
                        duration = System.currentTimeMillis() - startTime,
                        isSuccess = false,
                        errorMessage = e.message
                    )
                }

                if (attempt < maxAttempts) {
                    onRetry?.invoke(attempt, maxAttempts)
                    SecureLogger.w(
                        TAG,
                        "batch_analysis_retry | attempt=$attempt/$maxAttempts | reason=${lastError?.message}"
                    )
                }
            }

            Result.failure(lastError ?: Exception("AI分析失败"))

        } catch (e: AIApiException) {
            Result.failure(Exception("AI分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseBatchAnalysisResult(content: String): List<FoodAnalysisResult> {
        val jsonString = extractJsonFromText(content)
        
        var cleanedJson = jsonString
            .replace("，", ",")
            .replace("：", ":")
            .replace("estimated weight", "estimatedWeight")
        
        cleanedJson = cleanedJson
            .replace(""", "\"")
            .replace(""", "\"")
            .replace("'", "\"")
            .replace("'", "\"")
        
        val fieldNameMappings = mapOf(
            "estimated weight" to "estimatedWeight",
            "食物名称" to "foodName",
            "estimatedWeight" to "estimatedWeight",
            "热量" to "calories",
            "卡路里" to "calories",
            "蛋白质" to "protein",
            "碳水" to "carbs",
            "碳水化合物" to "carbs",
            "脂肪" to "fat",
            "膳食纤维" to "fiber",
            "纤维" to "fiber",
            "糖分" to "sugar",
            "糖" to "sugar",
            "饱和脂肪" to "saturatedFat",
            "胆固醇" to "cholesterol",
            "钠" to "sodium",
            "钾" to "potassium",
            "钙" to "calcium",
            "铁" to "iron",
            "维生素A" to "vitaminA",
            "维生素C" to "vitaminC"
        )
        
        for ((chineseName, englishName) in fieldNameMappings) {
            cleanedJson = cleanedJson.replace("\"$chineseName\":", "\"$englishName\":")
        }
        
        val numericFields = listOf(
            "calories", "protein", "carbs", "fat", "fiber", "sugar",
            "saturatedFat", "cholesterol", "sodium", "potassium",
            "calcium", "iron", "vitaminA", "vitaminC"
        )
        
        for (field in numericFields) {
            val regex = Regex("\"$field\"\\s*:\\s*['\"]([^'\"]+)['\"]")
            cleanedJson = regex.replace(cleanedJson) { matchResult ->
                val value = matchResult.groupValues[1]
                try {
                    value.toFloat()
                    "\"$field\":$value"
                } catch (e: Exception) {
                    matchResult.value
                }
            }
        }
        
        if (!cleanedJson.trimEnd().endsWith("}")) {
            cleanedJson = cleanedJson.trimEnd() + "}"
        }

        return try {
            val jsonObject = gson.fromJson(cleanedJson, com.google.gson.JsonObject::class.java)
            val itemsArray = jsonObject?.getAsJsonArray("items")
            if (itemsArray != null && itemsArray.size() > 0) {
                itemsArray.mapNotNull { element ->
                    runCatching { gson.fromJson(element, FoodAnalysisResult::class.java) }.getOrNull()
                }
            } else {
                // 兼容模型偶发返回单对象的情况
                listOf(gson.fromJson(cleanedJson, FoodAnalysisResult::class.java))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun validateNutritionData(result: FoodAnalysisResult): ValidationResult {
        if (result.foodName.isBlank()) {
            return ValidationResult(false, "食物名称为空")
        }
        if (result.calories <= 0) {
            return ValidationResult(false, "热量数据无效")
        }
        
        val basicNutrients = listOf(result.protein, result.carbs, result.fat)
        if (basicNutrients.all { it <= 0 }) {
            return ValidationResult(false, "基础营养素数据无效")
        }
        
        return ValidationResult(true, "数据有效")
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )

    private fun extractJsonFromText(raw: String): String {
        val idx = raw.indexOf('{')
        if (idx >= 0) {
            var depth = 0
            for (i in idx until raw.length) {
                when (raw[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return raw.substring(idx, i + 1).trim()
                    }
                }
            }
        }

        val jsonFenceRegex = Regex("```json\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        jsonFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        val anyFenceRegex = Regex("```\\s*([\\s\\S]*?)```")
        anyFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        return raw.trim()
    }

    private suspend fun recordApiCall(
        configId: String,
        configName: String,
        modelId: String,
        inputText: String,
        outputText: String,
        promptTokens: Int,
        completionTokens: Int,
        cost: Double,
        duration: Long,
        isSuccess: Boolean,
        errorMessage: String? = null
    ) {
        runCatching {
            apiCallRecordRepository.recordCall(
                configId = configId,
                configName = configName,
                modelId = modelId,
                inputText = inputText,
                outputText = outputText,
                promptTokens = promptTokens,
                completionTokens = completionTokens,
                cost = cost,
                duration = duration,
                isSuccess = isSuccess,
                errorMessage = errorMessage
            )
        }
    }

    private suspend fun recordTokenUsage(
        configId: String,
        configName: String,
        parsedUsage: ParsedUsage
    ) {
        runCatching {
            aiTokenUsageRepository.recordTokenUsage(
                configId = configId,
                configName = configName,
                promptTokens = parsedUsage.promptTokens,
                completionTokens = parsedUsage.completionTokens,
                cost = parsedUsage.cost
            )
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
