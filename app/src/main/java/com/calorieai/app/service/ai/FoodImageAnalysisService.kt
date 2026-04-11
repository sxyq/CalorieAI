package com.calorieai.app.service.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.repository.APICallRecordRepository
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.service.ai.common.AIResponseSanitizer
import com.calorieai.app.utils.SecureLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodImageAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository,
    private val apiCallRecordRepository: APICallRecordRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "FoodImageAnalysis"

        private const val SYSTEM_PROMPT = """你是一个专业的营养师，擅长通过图片识别食物并分析其营养成分。请仔细分析图片中的食物，提供详细的营养信息。

【严格要求 - 必须遵守】
1. foodName 字段必须使用中文名称
2. 必须严格使用英文标点符号（逗号、引号、冒号）
3. 所有数值必须是纯数字，不能带引号，不能使用字符串
4. 所有营养素字段必须返回，不能省略
5. 如果无法准确估算，使用合理的估计值（不能全部填0）

【13种必需营养素字段】
- 基础营养素（3种）：protein, carbs, fat
- 扩展营养素（10种）：fiber, sugar, saturatedFat, cholesterol, sodium, potassium, calcium, iron, vitaminA, vitaminC

【JSON格式示例】
{"foodName":"番茄炒蛋","estimatedWeight":200,"calories":185,"protein":13.4,"carbs":7.9,"fat":12.0,"fiber":2.5,"sugar":3.0,"saturatedFat":4.0,"cholesterol":160.8,"sodium":257.3,"potassium":492.3,"calcium":15.0,"iron":1.0,"vitaminA":3975.0,"vitaminC":12.3}

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
✗ 不要返回说明文字，只返回JSON"""
    }

    private data class ParsedUsage(
        val promptTokens: Int,
        val completionTokens: Int,
        val cost: Double
    )

    suspend fun analyzeFoodImage(
        imageUri: Uri,
        context: Context,
        userHint: String = "",
        maxRetries: Int = 1,
        onRetry: ((attempt: Int, maxAttempts: Int, reason: String) -> Unit)? = null
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val configs = resolveImageConfigs()
            if (configs.isEmpty()) {
                return@withContext Result.failure(
                    Exception("未找到可用的图像识别模型，请在AI配置里启用支持图像的模型并填写有效API Key")
                )
            }

            val base64Image = uriToBase64(imageUri, context)
                ?: return@withContext Result.failure(Exception("图片转换失败"))

            val baseUserMessage = if (userHint.isNotBlank()) {
                "用户提示：$userHint\n\n请分析这张图片中的食物。"
            } else {
                "请分析这张图片中的食物。"
            }

            val batchId = UUID.randomUUID().toString().substring(0, 8)
            val maxAttempts = (maxRetries + 1).coerceAtLeast(1)
            var finalError: Throwable? = null

            for (config in configs) {
                SecureLogger.event(
                    TAG,
                    "image_model_selected",
                    "configId" to config.id,
                    "configName" to config.name,
                    "modelId" to config.modelId
                )

                var lastError: Throwable? = null
                var lastResponseSnippet = ""
                var lastFailureDetail = ""

                for (attempt in 1..maxAttempts) {
                    val startTime = System.currentTimeMillis()
                    val userMessage = if (attempt == 1) {
                        baseUserMessage
                    } else {
                        buildRepairUserPrompt(baseUserMessage, lastFailureDetail, lastResponseSnippet)
                    }
                    try {
                        val (responseText, rawResponse) = aiApiClient.visionRaw(
                            config = config,
                            systemPrompt = SYSTEM_PROMPT,
                            userMessage = userMessage,
                            base64Image = base64Image,
                            temperature = 0.3,
                            maxTokens = 1000
                        )
                        val parsedUsage = parseUsage(rawResponse, config.protocol.name, config.modelId)
                        if (parsedUsage.promptTokens > 0 || parsedUsage.completionTokens > 0) {
                            recordTokenUsage(config.id, config.name, parsedUsage)
                        }

                        val parsedResult = parseAnalysisResult(responseText)
                        val normalized = normalizeNutritionData(parsedResult)
                        val validation = validateNutritionData(normalized)

                        if (validation.isValid) {
                            recordApiCall(
                                configId = config.id,
                                configName = config.name,
                                modelId = config.modelId,
                                inputText = "[图片分析任务#$batchId][尝试#$attempt] $userMessage",
                                outputText = responseText,
                                promptTokens = parsedUsage.promptTokens,
                                completionTokens = parsedUsage.completionTokens,
                                cost = parsedUsage.cost,
                                duration = System.currentTimeMillis() - startTime,
                                isSuccess = true
                            )
                            return@withContext Result.success(normalized)
                        }

                        lastError = AIApiException(
                            message = validation.errorMessage,
                            category = AIErrorCategory.VALIDATION,
                            retryEligible = true
                        )
                        lastResponseSnippet = responseText.take(800)
                        val errorInfo = AIErrorClassifier.classify(lastError)
                        lastFailureDetail = errorInfo.detail
                        recordApiCall(
                            configId = config.id,
                            configName = config.name,
                            modelId = config.modelId,
                            inputText = "[图片分析任务#$batchId][尝试#$attempt] $userMessage",
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
                        lastFailureDetail = errorInfo.detail
                        recordApiCall(
                            configId = config.id,
                            configName = config.name,
                            modelId = config.modelId,
                            inputText = "[图片分析任务#$batchId][尝试#$attempt] $userMessage",
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
                    lastFailureDetail = errorInfo.detail
                    if (attempt < maxAttempts && errorInfo.retryEligible) {
                        val retryReason = errorInfo.detail.ifBlank { "返回结果不完整" }
                        onRetry?.invoke(attempt, maxAttempts, retryReason)
                        SecureLogger.w(
                            TAG,
                            "image_analysis_retry | attempt=$attempt/$maxAttempts | category=${errorInfo.category} | reason=$retryReason"
                        )
                    } else if (errorInfo.category == AIErrorCategory.NETWORK) {
                        finalError = lastError
                        break
                    }
                }

                finalError = lastError
                val configError = AIErrorClassifier.classify(lastError)
                if (configError.category == AIErrorCategory.NETWORK) {
                    break
                }
            }

            val finalErrorInfo = AIErrorClassifier.classify(finalError)
            Result.failure(Exception(finalErrorInfo.userMessage))

        } catch (e: AIApiException) {
            Result.failure(Exception("AI图片分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveImageConfigs(): List<com.calorieai.app.data.model.AIConfig> {
        val defaultConfig = aiConfigRepository.getDefaultConfig().firstOrNull()
        val allConfigs = aiConfigRepository.getAllConfigs().firstOrNull().orEmpty()

        fun isUsable(config: com.calorieai.app.data.model.AIConfig): Boolean {
            return config.isImageUnderstanding &&
                config.apiUrl.isNotBlank() &&
                config.modelId.isNotBlank() &&
                config.apiKey.isNotBlank()
        }

        val usableImageConfigs = allConfigs.filter(::isUsable)
        if (usableImageConfigs.isEmpty()) {
            return emptyList()
        }

        val sortedByModelPriority = usableImageConfigs.sortedByDescending {
            when {
                isPreferredVisionModel(it.modelId) -> 2
                it.modelId.contains("vision", ignoreCase = true) -> 1
                else -> 0
            }
        }

        val prioritized = mutableListOf<com.calorieai.app.data.model.AIConfig>()
        if (defaultConfig != null && isUsable(defaultConfig)) {
            prioritized += defaultConfig
        }
        prioritized += sortedByModelPriority.filterNot { candidate ->
            prioritized.any { existing -> existing.id == candidate.id }
        }
        return prioritized
    }

    private fun isPreferredVisionModel(modelId: String): Boolean {
        val normalized = modelId.lowercase()
        return normalized.contains("omni") ||
            normalized.contains("o-mini") ||
            normalized.contains("omini")
    }

    private fun uriToBase64(uri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            val compressedBitmap = compressBitmap(bitmap, maxSizeKB = 1024)

            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        var quality = 100
        var compressedBitmap = bitmap

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.size() / 1024 > maxSizeKB && quality > 50) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        if (outputStream.size() / 1024 > maxSizeKB) {
            val scaleFactor = Math.sqrt((maxSizeKB * 1024).toDouble() / outputStream.size())
            val newWidth = (bitmap.width * scaleFactor).toInt()
            val newHeight = (bitmap.height * scaleFactor).toInt()
            compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }

        return compressedBitmap
    }

    private fun parseAnalysisResult(content: String): FoodAnalysisResult {
        return AIResponseSanitizer.parseSingleFoodItem(content, gson) ?: FoodAnalysisResult()
    }

    private fun normalizeNutritionData(result: FoodAnalysisResult): FoodAnalysisResult {
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
            foodName = result.foodName.trim().ifBlank { "未知食物" }.take(30),
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

    private fun buildRepairUserPrompt(
        baseUserPrompt: String,
        lastFailureDetail: String,
        lastResponseSnippet: String
    ): String {
        return buildString {
            append(baseUserPrompt)
            append("\n\n上次输出未通过校验，原因：")
            append(lastFailureDetail.ifBlank { "字段缺失或JSON格式不稳定" })
            append("\n请严格只返回一个 JSON 对象，字段必须包含 foodName、estimatedWeight、calories、protein、carbs、fat 及扩展营养素；不要输出Markdown代码块，不要附加解释。")
            if (lastResponseSnippet.isNotBlank()) {
                append("\n上次输出片段（请修复后重写，不要复述原文）：\n")
                append(lastResponseSnippet)
            }
        }
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
