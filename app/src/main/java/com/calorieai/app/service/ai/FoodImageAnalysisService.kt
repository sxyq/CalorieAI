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
import com.calorieai.app.utils.SecureLogger
import com.google.gson.Gson
import com.google.gson.JsonParser
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

            val batchId = UUID.randomUUID().toString().substring(0, 8)
            val maxAttempts = (maxRetries + 1).coerceAtLeast(1)
            var lastError: Throwable? = null

            for (attempt in 1..maxAttempts) {
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
                    val errorInfo = AIErrorClassifier.classify(lastError)
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
                if (attempt < maxAttempts && errorInfo.retryEligible) {
                    val retryReason = errorInfo.detail.ifBlank { "返回结果不完整" }
                    onRetry?.invoke(attempt, maxAttempts, retryReason)
                    SecureLogger.w(
                        TAG,
                        "image_analysis_retry | attempt=$attempt/$maxAttempts | category=${errorInfo.category} | reason=$retryReason"
                    )
                } else if (errorInfo.category == AIErrorCategory.NETWORK) {
                    break
                }
            }

            val finalError = AIErrorClassifier.classify(lastError)
            Result.failure(Exception(finalError.userMessage))

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
        var cleanedJson = normalizeJsonText(jsonString)

        val fieldNameMappings = mapOf(
            "食物名称" to "foodName",
            "名称" to "foodName",
            "重量" to "estimatedWeight",
            "估计重量" to "estimatedWeight",
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
        fieldNameMappings.forEach { (chineseName, englishName) ->
            cleanedJson = cleanedJson.replace("\"$chineseName\"\\s*:".toRegex(), "\"$englishName\":")
        }

        val numericFields = listOf(
            "estimatedWeight",
            "calories",
            "protein",
            "carbs",
            "fat",
            "fiber",
            "sugar",
            "saturatedFat",
            "cholesterol",
            "sodium",
            "potassium",
            "calcium",
            "iron",
            "vitaminA",
            "vitaminC"
        )
        numericFields.forEach { field ->
            val quotedRegex = Regex("\"$field\"\\s*:\\s*['\"]([^'\"]+)['\"]")
            cleanedJson = quotedRegex.replace(cleanedJson) { matchResult ->
                val raw = matchResult.groupValues[1]
                normalizeNumericLiteral(raw)?.let { "\"$field\":$it" } ?: "\"$field\":0"
            }

            val rawRegex = Regex("\"$field\"\\s*:\\s*([^,}\\n]+)")
            cleanedJson = rawRegex.replace(cleanedJson) { matchResult ->
                val raw = matchResult.groupValues[1]
                normalizeNumericLiteral(raw)?.let { "\"$field\":$it" } ?: "\"$field\":0"
            }
        }

        return try {
            gson.fromJson(cleanedJson, FoodAnalysisResult::class.java) ?: parseAnalysisResultFallback(cleanedJson)
        } catch (e: Exception) {
            parseAnalysisResultFallback(cleanedJson)
        }
    }

    private fun parseAnalysisResultFallback(cleanedJson: String): FoodAnalysisResult {
        return runCatching {
            val obj = JsonParser.parseString(cleanedJson).asJsonObject

            fun text(vararg keys: String): String {
                return keys.firstNotNullOfOrNull { key ->
                    obj.get(key)?.takeIf { !it.isJsonNull }?.asString
                }.orEmpty()
            }

            fun numeric(vararg keys: String): Float {
                return keys.firstNotNullOfOrNull { key ->
                    obj.get(key)?.takeIf { !it.isJsonNull }?.let { element ->
                        if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
                            element.asFloat
                        } else {
                            normalizeNumericLiteral(element.asString)?.toFloatOrNull()
                        }
                    }
                } ?: 0f
            }

            FoodAnalysisResult(
                foodName = text("foodName", "name"),
                estimatedWeight = numeric("estimatedWeight", "weight").toInt().coerceAtLeast(0),
                calories = numeric("calories"),
                protein = numeric("protein"),
                carbs = numeric("carbs"),
                fat = numeric("fat"),
                fiber = numeric("fiber"),
                sugar = numeric("sugar"),
                saturatedFat = numeric("saturatedFat"),
                cholesterol = numeric("cholesterol"),
                sodium = numeric("sodium"),
                potassium = numeric("potassium"),
                calcium = numeric("calcium"),
                iron = numeric("iron"),
                vitaminA = numeric("vitaminA"),
                vitaminC = numeric("vitaminC"),
                description = text("description")
            )
        }.getOrDefault(FoodAnalysisResult())
    }

    private fun normalizeJsonText(json: String): String {
        val normalized = toHalfWidth(json)
            .replace("，", ",")
            .replace("：", ":")
            .replace("；", ",")
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‘", "\"")
            .replace("’", "\"")

        val quotedKeys = Regex("([\\{,]\\s*)([A-Za-z_\\u4e00-\\u9fa5][A-Za-z0-9_\\u4e00-\\u9fa5]*)\\s*:")
            .replace(normalized) { match ->
                "${match.groupValues[1]}\"${match.groupValues[2]}\":"
            }

        return Regex(":\\s*'([^']*)'").replace(quotedKeys) { match ->
            ":\"${match.groupValues[1]}\""
        }
    }

    private fun normalizeNumericLiteral(raw: String): String? {
        val normalized = toHalfWidth(raw)
            .replace(",", "")
            .replace(" ", "")
            .replace("千卡", "")
            .replace("大卡", "")
            .replace("kcal", "", ignoreCase = true)
            .replace("卡路里", "")
            .replace("毫克", "")
            .replace("mg", "", ignoreCase = true)
            .replace("克", "")
            .replace("g", "", ignoreCase = true)
            .replace("μg", "", ignoreCase = true)
            .replace("ug", "", ignoreCase = true)
            .replace("IU", "", ignoreCase = true)

        val match = Regex("-?\\d+(\\.\\d+)?").find(normalized)?.value ?: return null
        return match.toFloatOrNull()
            ?.coerceAtLeast(0f)
            ?.toString()
    }

    private fun toHalfWidth(input: String): String {
        val result = StringBuilder(input.length)
        input.forEach { ch ->
            when (ch) {
                in '０'..'９' -> result.append(('0'.code + (ch.code - '０'.code)).toChar())
                '．' -> result.append('.')
                '－' -> result.append('-')
                '＋' -> result.append('+')
                else -> result.append(ch)
            }
        }
        return result.toString()
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
