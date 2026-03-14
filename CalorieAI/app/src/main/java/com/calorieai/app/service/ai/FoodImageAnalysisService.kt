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

重要规则：
1. foodName 字段必须使用中文名称
2. 必须严格使用英文标点符号（逗号、引号、冒号）

请以JSON格式返回，格式如下：
{"foodName":"食物中文名称","estimatedWeight":100,"calories":250,"protein":10.5,"carbs":30.0,"fat":8.5,"fiber":2.0,"sugar":5.0,"saturatedFat":2.0,"transFat":0.0,"cholesterol":30.0,"sodium":200.0,"potassium":150.0,"calcium":50.0,"iron":2.0,"zinc":1.0,"magnesium":30.0,"vitaminA":100.0,"vitaminC":10.0,"vitaminD":2.0,"vitaminE":3.0,"vitaminB1":0.5,"vitaminB2":0.6,"vitaminB6":0.8,"vitaminB12":1.0,"description":"描述"}

注意：
1. foodName 必须是中文，例如："汉堡王 皇堡"、"麦当劳 巨无霸"
2. 只返回JSON，不要包含其他说明文字
3. 如果某些营养素无法准确估算，可以填0
4. 必须使用英文标点符号"""
    }

    suspend fun analyzeFoodImage(
        imageUri: Uri,
        context: Context,
        userHint: String = ""
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
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
            Result.success(result)

        } catch (e: AIApiException) {
            Result.failure(Exception("AI图片分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
