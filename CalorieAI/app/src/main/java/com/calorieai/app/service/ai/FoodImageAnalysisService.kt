package com.calorieai.app.service.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 食物图片分析服务
 * 通过拍照识别食物并分析营养成分
 */
@Singleton
class FoodImageAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository
) {
    private val gson = Gson()

    companion object {
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
{"foodName":"番茄炒蛋","estimatedWeight":200,"calories":185,"protein":13.4,"carbs":7.9,"fat":12.0,"fiber":2.5,"sugar":3.0,"saturatedFat":4.0,"cholesterol":160.8,"sodium":257.3,"potassium":492.3,"calcium":15.0,"iron":1.0,"vitaminA":3975.0,"vitaminC":12.3,"description":"番茄炒蛋配米饭"}

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

    suspend fun analyzeFoodImage(
        imageUri: Uri,
        context: Context,
        userHint: String = "",
        maxRetries: Int = 2,
        onRetry: ((attempt: Int, maxAttempts: Int, reason: String) -> Unit)? = null
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                    ?: return@withContext Result.failure(Exception("未配置AI服务"))

                if (!config.isImageUnderstanding) {
                    return@withContext Result.failure(Exception("当前AI配置不支持图像理解"))
                }

                val base64Image = uriToBase64(imageUri, context)
                    ?: return@withContext Result.failure(Exception("图片转换失败"))

                val userMessage = if (userHint.isNotBlank()) {
                    "用户提示：$userHint\n\n请分析这张图片中的食物。"
                } else {
                    "请分析这张图片中的食物。"
                }

                val responseText = aiApiClient.vision(
                    config = config,
                    systemPrompt = SYSTEM_PROMPT,
                    userMessage = userMessage,
                    base64Image = base64Image,
                    temperature = 0.3,
                    maxTokens = 1000
                )

                val result = parseAnalysisResult(responseText)
                
                // 验证营养素数据
                val validation = validateNutritionData(result)
                if (validation.isValid) {
                    return@withContext Result.success(result)
                } else {
                    // 数据无效，需要重试
                    lastException = Exception(validation.errorMessage)
                    if (attempt < maxRetries) {
                        onRetry?.invoke(attempt + 1, maxRetries + 1, validation.errorMessage)
                        kotlinx.coroutines.delay(1000L * (attempt + 1))
                    }
                }

            } catch (e: AIApiException) {
                lastException = Exception("AI图片分析失败: ${e.message}")
                if (attempt < maxRetries) {
                    onRetry?.invoke(attempt + 1, maxRetries + 1, "API错误: ${e.message}")
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    onRetry?.invoke(attempt + 1, maxRetries + 1, "未知错误: ${e.message}")
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }
        
        // 所有重试都失败了
        Result.failure(lastException ?: Exception("图片分析失败，请稍后重试"))
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

    /**
     * 从 AI 返回的文本中提取 JSON 并解析
     */
    private fun parseAnalysisResult(content: String): FoodAnalysisResult {
        val jsonString = extractJsonFromText(content)

        return try {
            gson.fromJson(jsonString, FoodAnalysisResult::class.java)
                ?: FoodAnalysisResult()
        } catch (e: Exception) {
            FoodAnalysisResult(
                foodName = "未知食物",
                estimatedWeight = 0,
                calories = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f,
                description = content.take(200)
            )
        }
    }

    /**
     * 验证营养素数据是否有效
     */
    private fun validateNutritionData(result: FoodAnalysisResult): ValidationResult {
        // 检查基本信息
        if (result.foodName.isBlank()) {
            return ValidationResult(false, "食物名称为空")
        }
        if (result.calories <= 0) {
            return ValidationResult(false, "热量数据无效")
        }
        
        // 检查基础营养素（3种关键营养素不能同时为0）
        val basicNutrients = listOf(result.protein, result.carbs, result.fat)
        if (basicNutrients.all { it <= 0 }) {
            return ValidationResult(false, "基础营养素（蛋白质、碳水、脂肪）数据无效，全部为0")
        }
        
        return ValidationResult(true, "数据有效")
    }
    
    /**
     * 验证结果数据类
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )

    /**
     * 从可能包含 Markdown 代码块的文本中提取 JSON
     * 参考 Deadliner 的 AIUtils.extractJsonFromMarkdown()
     */
    private fun extractJsonFromText(raw: String): String {
        // 1. 查找第一个 { 并配对闭合的 }
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

        // 2. 尝试从 ```json ... ``` 代码块中提取
        val jsonFenceRegex = Regex("```json\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        jsonFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        val anyFenceRegex = Regex("```\\s*([\\s\\S]*?)```")
        anyFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        // 3. 兜底返回原文
        return raw.trim()
    }
}

/**
 * 食物图片分析结果
 */
data class FoodAnalysisResult(
    @SerializedName("foodName") val foodName: String = "",
    @SerializedName("estimatedWeight") val estimatedWeight: Int = 0,
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("description") val description: String = ""
)
