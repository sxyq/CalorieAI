package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.model.FoodBatchAnalysisResult
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.service.ai.common.AIResponseParsing
import com.calorieai.app.service.ai.common.AIResponseSanitizer
import com.calorieai.app.service.ai.common.ParsedUsage
import com.calorieai.app.utils.SecureLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodTextAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiImportConfigResolver: AIImportConfigResolver,
    private val apiCallRecordRepository: APICallRecordRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "FoodTextAnalysis"

        private const val SYSTEM_PROMPT = """你是营养师。只输出JSON，不要解释。
根对象必须是 {"items":[...]}。
每个item字段必须包含：
foodName, estimatedWeight, calories, protein, carbs, fat, fiber, sugar, saturatedFat, cholesterol, sodium, potassium, calcium, iron, vitaminA, vitaminC
规则：
1) 仅当用户明确列出多个独立食物且带数量单位时，才拆分多条；
2) 菜名/品牌餐品/套餐名不拆分，输出1条；
3) 数值必须为数字，不加引号，英文标点。"""
    }

    suspend fun analyzeFoodText(
        foodDescription: String,
        maxRetries: Int = 1,
        onRetry: ((attempt: Int, maxAttempts: Int) -> Unit)? = null,
        requestTag: String? = null
    ): Result<FoodBatchAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = resolveTextConfig()
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
                        maxTokens = 900
                    )
                    val parsedUsage = AIResponseParsing.parseUsage(
                        rawResponse = rawResponse,
                        protocol = config.protocol.name,
                        modelId = config.modelId,
                        aiApiClient = aiApiClient
                    )
                    if (parsedUsage.promptTokens > 0 || parsedUsage.completionTokens > 0) {
                        recordTokenUsage(config.id, config.name, parsedUsage)
                    }
                    val importableItems = FoodTextImportPostProcessor.process(
                        responseText = responseText,
                        foodDescription = foodDescription
                    )
                    if (importableItems.isNotEmpty()) {
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
                            "itemCount" to importableItems.size,
                            "inputLength" to foodDescription.length
                        )
                        return@withContext Result.success(
                            FoodBatchAnalysisResult(
                                items = importableItems,
                                promptTokens = parsedUsage.promptTokens,
                                completionTokens = parsedUsage.completionTokens
                            )
                        )
                    }
                    lastError = AIApiException(
                        message = "AI返回结果为空或无效",
                        category = AIErrorCategory.VALIDATION,
                        retryEligible = false
                    )
                    val errorInfo = AIErrorClassifier.classify(lastError)
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
                        errorMessage = errorInfo.toLogMessage()
                    )
                } catch (e: Exception) {
                    lastError = e
                    val errorInfo = AIErrorClassifier.classify(e)
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
                        errorMessage = errorInfo.toLogMessage()
                    )
                }

                val errorInfo = AIErrorClassifier.classify(lastError)
                if (attempt < maxAttempts && errorInfo.retryEligible) {
                    onRetry?.invoke(attempt, maxAttempts)
                    SecureLogger.w(
                        TAG,
                        "batch_analysis_retry | attempt=$attempt/$maxAttempts | category=${errorInfo.category} | reason=${errorInfo.detail}"
                    )
                } else if (errorInfo.category == AIErrorCategory.NETWORK) {
                    break
                }
            }

            val finalError = AIErrorClassifier.classify(lastError)
            Result.failure(Exception(finalError.userMessage))

        } catch (e: AIApiException) {
            Result.failure(Exception("AI分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseBatchAnalysisResult(content: String): List<FoodAnalysisResult> {
        val sanitizedItems = AIResponseSanitizer.parseFoodItems(content, gson)
        if (sanitizedItems.isNotEmpty()) return sanitizedItems

        // 兜底兼容：模型偶发返回单对象
        return AIResponseSanitizer.parseSingleFoodItem(content, gson)?.let { listOf(it) }.orEmpty()
    }

    private suspend fun resolveTextConfig() = aiImportConfigResolver.resolveTextConfig()

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

}
