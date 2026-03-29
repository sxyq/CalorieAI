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
import com.calorieai.app.utils.SecureLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        private const val CONCURRENT_CALLS = 5
        
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
        maxRetries: Int = 2,
        onRetry: ((attempt: Int, maxAttempts: Int, reason: String) -> Unit)? = null
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = resolveImageConfig()
                ?: return@withContext Result.failure(Exception("未找到可用的图像识别模型，请在AI配置里启用支持图像的模型（推荐Omni/o-mini）"))
            SecureLogger.event(
                TAG,
                "image_model_selected",
                "configId" to config.id,
                "configName" to config.name,
                "modelId" to config.modelId
            )

            val base64Image = uriToBase64(imageUri, context)
                ?: return@withContext Result.failure(Exception("图片转换失败"))

            val userMessage = if (userHint.isNotBlank()) {
                "用户提示：$userHint\n\n请分析这张图片中的食物。"
            } else {
                "请分析这张图片中的食物。"
            }

            // 并发调用5次
            val results = mutableListOf<FoodAnalysisResult>()
            val errors = mutableListOf<String>()
            val batchId = UUID.randomUUID().toString().substring(0, 8)
            
            val deferredResults = (1..CONCURRENT_CALLS).map { index ->
                async {
                    val startTime = System.currentTimeMillis()
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
                        val result = parseAnalysisResult(responseText)
                        val validation = validateNutritionData(result)
                        if (validation.isValid) {
                            recordApiCall(
                                configId = config.id,
                                configName = config.name,
                                modelId = config.modelId,
                                inputText = "[图片分析任务#$batchId][尝试#$index] $userMessage",
                                outputText = responseText,
                                promptTokens = parsedUsage.promptTokens,
                                completionTokens = parsedUsage.completionTokens,
                                cost = parsedUsage.cost,
                                duration = System.currentTimeMillis() - startTime,
                                isSuccess = true
                            )
                            Result.success(result)
                        } else {
                            recordApiCall(
                                configId = config.id,
                                configName = config.name,
                                modelId = config.modelId,
                                inputText = "[图片分析任务#$batchId][尝试#$index] $userMessage",
                                outputText = responseText,
                                promptTokens = parsedUsage.promptTokens,
                                completionTokens = parsedUsage.completionTokens,
                                cost = parsedUsage.cost,
                                duration = System.currentTimeMillis() - startTime,
                                isSuccess = false,
                                errorMessage = validation.errorMessage
                            )
                            Result.failure<FoodAnalysisResult>(Exception(validation.errorMessage))
                        }
                    } catch (e: Exception) {
                        recordApiCall(
                            configId = config.id,
                            configName = config.name,
                                modelId = config.modelId,
                                inputText = "[图片分析任务#$batchId][尝试#$index] $userMessage",
                                outputText = "",
                                promptTokens = 0,
                                completionTokens = 0,
                                cost = 0.0,
                                duration = System.currentTimeMillis() - startTime,
                                isSuccess = false,
                                errorMessage = e.message
                        )
                        Result.failure<FoodAnalysisResult>(e)
                    }
                }
            }
            
            deferredResults.awaitAll().forEach { result ->
                result.fold(
                    onSuccess = { results.add(it) },
                    onFailure = { errors.add(it.message ?: "未知错误") }
                )
            }

            if (results.isEmpty()) {
                return@withContext Result.failure(Exception("所有分析尝试失败: ${errors.take(3).joinToString("; ")}"))
            }

            // 计算平均值
            val averagedResult = calculateAverageResult(results)
            Result.success(averagedResult)

        } catch (e: AIApiException) {
            Result.failure(Exception("AI图片分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveImageConfig(): com.calorieai.app.data.model.AIConfig? {
        val defaultConfig = aiConfigRepository.getDefaultConfig().firstOrNull()
        if (defaultConfig != null && defaultConfig.isImageUnderstanding) {
            return defaultConfig
        }

        val allConfigs = aiConfigRepository.getAllConfigs().firstOrNull().orEmpty()
        val imageConfigs = allConfigs.filter { it.isImageUnderstanding }
        if (imageConfigs.isEmpty()) {
            return null
        }

        // 优先选 Omni / o-mini 这类视觉能力模型
        return imageConfigs.firstOrNull {
            val modelId = it.modelId.lowercase()
            modelId.contains("omni") || modelId.contains("o-mini") || modelId.contains("omini")
        } ?: imageConfigs.first()
    }
    
    /**
     * 计算多次分析结果的平均值
     */
    private fun calculateAverageResult(results: List<FoodAnalysisResult>): FoodAnalysisResult {
        if (results.isEmpty()) return FoodAnalysisResult(foodName = "未知食物")
        if (results.size == 1) return results.first()
        
        // 选择出现最多的食物名称
        val foodNames = results.map { it.foodName }.groupingBy { it }.eachCount()
        val mostCommonName = foodNames.maxByOrNull { it.value }?.key ?: "未知食物"
        
        // 计算数值字段的平均值
        fun averageOf(selector: (FoodAnalysisResult) -> Float): Float {
            val values = results.map(selector)
            return values.sum() / values.size
        }
        
        return FoodAnalysisResult(
            foodName = mostCommonName,
            estimatedWeight = (results.map { it.estimatedWeight }.sum() / results.size),
            calories = averageOf { it.calories },
            protein = averageOf { it.protein },
            carbs = averageOf { it.carbs },
            fat = averageOf { it.fat },
            fiber = averageOf { it.fiber },
            sugar = averageOf { it.sugar },
            saturatedFat = averageOf { it.saturatedFat },
            cholesterol = averageOf { it.cholesterol },
            sodium = averageOf { it.sodium },
            potassium = averageOf { it.potassium },
            calcium = averageOf { it.calcium },
            iron = averageOf { it.iron },
            vitaminA = averageOf { it.vitaminA },
            vitaminC = averageOf { it.vitaminC }
        )
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
        val jsonString = extractJsonFromText(content)

        return try {
            gson.fromJson(jsonString, FoodAnalysisResult::class.java)
                ?: FoodAnalysisResult()
        } catch (e: Exception) {
            FoodAnalysisResult(foodName = "未知食物")
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
            return ValidationResult(false, "基础营养素（蛋白质、碳水、脂肪）数据无效，全部为0")
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
