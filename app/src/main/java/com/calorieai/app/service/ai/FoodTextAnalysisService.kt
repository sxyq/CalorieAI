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
import java.util.UUID
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
        
        private const val SYSTEM_PROMPT = """你是专业营养师。请先判断用户是否在“明确列举多种独立食物”。

【严格要求 - 必须遵守】
1. 返回JSON对象，且根字段必须是 items 数组
2. 只有当用户明确列举多种独立食物时，才返回多条 items
3. 如果用户输入的是单一菜名/品牌餐品/套餐名称（如“汉堡王大皇堡”），必须只返回1条，不能拆成配料
3. foodName 字段必须使用中文名称（由你命名）
2. 必须严格使用英文标点符号（逗号、引号、冒号）
3. 所有数值必须是纯数字，不能带引号，不能使用字符串
4. 所有营养素字段必须返回，不能省略
5. 如果无法准确估算，使用合理的估计值（不能全部填0）

【拆分判定规则（非常重要）】
- 应拆分：用户明确列出多食物，且通常带数量/单位，例如“100g牛肉 50g鸡肉 1个鸡蛋”
- 不应拆分：品牌名、菜名、单个餐品、套餐/便当/盖饭/汉堡名，即使其内部有多个配料，也按1条返回
- 当无法确定时，优先不拆分，按1条返回

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
        onRetry: ((attempt: Int, maxAttempts: Int) -> Unit)? = null,
        requestTag: String? = null
    ): Result<FoodBatchAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))
            val batchId = UUID.randomUUID().toString().substring(0, 8)

            val userPrompt = buildString {
                append("请拆分并分析以下食物，按多条items返回：")
                append(foodDescription)
                if (!requestTag.isNullOrBlank()) {
                    append("\n\n请求追踪ID：")
                    append(requestTag)
                    append("（仅用于防缓存与日志追踪，不参与营养结论）")
                }
            }
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
                    val intentAlignedItems = alignItemsWithUserIntent(parsedItems, foodDescription)
                    val normalizedItems = intentAlignedItems.mapIndexed { index, item ->
                        normalizeNutritionData(item, foodDescription, index)
                    }
                    val validItems = normalizedItems.filter { validateNutritionData(it).isValid }
                    if (validItems.isNotEmpty()) {
                        recordApiCall(
                            configId = config.id,
                            configName = config.name,
                            modelId = config.modelId,
                            inputText = "[文本分析任务#$batchId][尝试#$attempt] $userPrompt",
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
                        return@withContext Result.success(
                            FoodBatchAnalysisResult(
                                items = validItems,
                                promptTokens = parsedUsage.promptTokens,
                                completionTokens = parsedUsage.completionTokens
                            )
                        )
                    }
                    lastError = Exception("AI返回结果为空或无效")
                    recordApiCall(
                        configId = config.id,
                        configName = config.name,
                        modelId = config.modelId,
                        inputText = "[文本分析任务#$batchId][尝试#$attempt] $userPrompt",
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
                        inputText = "[文本分析任务#$batchId][尝试#$attempt] $userPrompt",
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
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‘", "\"")
            .replace("’", "\"")
        
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

    private fun alignItemsWithUserIntent(
        items: List<FoodAnalysisResult>,
        foodDescription: String
    ): List<FoodAnalysisResult> {
        if (items.isEmpty()) return items
        if (shouldSplitByExplicitQuantity(foodDescription)) {
            return items
        }
        // 非“明确多食物+数量”场景，强制合并为单条，避免把菜名拆成配料。
        return listOf(mergeToSingleItem(items, foodDescription))
    }

    private fun shouldSplitByExplicitQuantity(input: String): Boolean {
        val normalized = input.lowercase()
        // 统计“数量+单位”出现次数。出现2次及以上，视为明确多食物拆分意图。
        val quantityRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(g|克|kg|千克|ml|毫升|l|升|个|份|片|勺|杯|碗)")
        val quantityCount = quantityRegex.findAll(normalized).count()
        if (quantityCount >= 2) return true

        // 兜底：常见分隔词 + 至少两个数字，也判定为拆分意图
        val hasSplitter = listOf("，", ",", "、", "和", "及", "+", " plus ").any { normalized.contains(it) }
        val numberCount = Regex("\\d+(?:\\.\\d+)?").findAll(normalized).count()
        return hasSplitter && numberCount >= 2
    }

    private fun mergeToSingleItem(
        items: List<FoodAnalysisResult>,
        foodDescription: String
    ): FoodAnalysisResult {
        if (items.size == 1) return items.first()
        val name = deriveMergedName(foodDescription, items)
        val weight = items.sumOf { it.estimatedWeight.toDouble() }.toInt().coerceAtLeast(1)
        return FoodAnalysisResult(
            foodName = name,
            estimatedWeight = weight,
            calories = items.sumOf { it.calories.toDouble() }.toFloat(),
            protein = items.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = items.sumOf { it.carbs.toDouble() }.toFloat(),
            fat = items.sumOf { it.fat.toDouble() }.toFloat(),
            fiber = items.sumOf { it.fiber.toDouble() }.toFloat(),
            sugar = items.sumOf { it.sugar.toDouble() }.toFloat(),
            saturatedFat = items.sumOf { it.saturatedFat.toDouble() }.toFloat(),
            cholesterol = items.sumOf { it.cholesterol.toDouble() }.toFloat(),
            sodium = items.sumOf { it.sodium.toDouble() }.toFloat(),
            potassium = items.sumOf { it.potassium.toDouble() }.toFloat(),
            calcium = items.sumOf { it.calcium.toDouble() }.toFloat(),
            iron = items.sumOf { it.iron.toDouble() }.toFloat(),
            vitaminA = items.sumOf { it.vitaminA.toDouble() }.toFloat(),
            vitaminC = items.sumOf { it.vitaminC.toDouble() }.toFloat()
        )
    }

    private fun deriveMergedName(
        foodDescription: String,
        items: List<FoodAnalysisResult>
    ): String {
        val cleaned = foodDescription.trim()
        if (cleaned.isNotEmpty()) return cleaned.take(30)
        return items.maxByOrNull { it.calories }?.foodName ?: "混合食物"
    }

    private fun normalizeNutritionData(
        result: FoodAnalysisResult,
        foodDescription: String,
        index: Int
    ): FoodAnalysisResult {
        val normalizedFoodName = result.foodName.trim().ifBlank {
            deriveFallbackName(foodDescription, index)
        }

        val protein = sanitizeNumeric(result.protein)
        val carbs = sanitizeNumeric(result.carbs)
        val fat = sanitizeNumeric(result.fat)
        var calories = sanitizeNumeric(result.calories)

        if (calories <= 0f) {
            val estimatedCalories = protein * 4f + carbs * 4f + fat * 9f
            if (estimatedCalories > 0f) {
                calories = estimatedCalories
            }
        }

        return result.copy(
            foodName = normalizedFoodName.take(30),
            estimatedWeight = result.estimatedWeight.coerceAtLeast(0),
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = sanitizeNumeric(result.fiber),
            sugar = sanitizeNumeric(result.sugar),
            saturatedFat = sanitizeNumeric(result.saturatedFat),
            cholesterol = sanitizeNumeric(result.cholesterol),
            sodium = sanitizeNumeric(result.sodium),
            potassium = sanitizeNumeric(result.potassium),
            calcium = sanitizeNumeric(result.calcium),
            iron = sanitizeNumeric(result.iron),
            vitaminA = sanitizeNumeric(result.vitaminA),
            vitaminC = sanitizeNumeric(result.vitaminC)
        )
    }

    private fun deriveFallbackName(foodDescription: String, index: Int): String {
        val cleaned = foodDescription.trim().take(30)
        if (cleaned.isNotBlank()) return cleaned
        return "未命名食物${index + 1}"
    }

    private fun sanitizeNumeric(value: Float): Float {
        if (value.isNaN() || value.isInfinite()) return 0f
        return value.coerceAtLeast(0f)
    }

    private fun validateNutritionData(result: FoodAnalysisResult): ValidationResult {
        if (result.foodName.isBlank()) {
            return ValidationResult(false, "食物名称为空")
        }

        val hasCalories = result.calories > 0f
        val hasMacroNutrients = result.protein > 0f || result.carbs > 0f || result.fat > 0f
        if (!hasCalories && !hasMacroNutrients) {
            return ValidationResult(false, "热量和宏量营养素均无效")
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
