package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodBatchAnalysisResult
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.service.ai.common.AIResponseParsing
import com.calorieai.app.service.ai.common.ParsedUsage
import com.calorieai.app.utils.SecureLogger
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

    private suspend fun resolveTextConfig() = aiImportConfigResolver.resolveTextConfig()

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
