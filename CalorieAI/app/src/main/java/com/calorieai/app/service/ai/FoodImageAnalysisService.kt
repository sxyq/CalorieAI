package com.calorieai.app.service.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.repository.AIConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodImageAnalysisService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val aiConfigRepository: AIConfigRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeFoodImage(
        imageUri: Uri,
        context: Context,
        userHint: String = ""
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 获取默认AI配置
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))

            // 检查是否支持图像理解
            if (!config.isImageUnderstanding) {
                return@withContext Result.failure(Exception("当前AI配置不支持图像理解"))
            }

            // 将图片转换为base64
            val base64Image = uriToBase64(imageUri, context)
                ?: return@withContext Result.failure(Exception("图片转换失败"))

            // 调用AI API进行分析
            val result = callVisionAPI(config, base64Image, userHint)

            Result.success(result)
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

            // 压缩图片以减少API调用大小
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

        // 如果还是太大，缩放图片
        if (outputStream.size() / 1024 > maxSizeKB) {
            val scaleFactor = Math.sqrt((maxSizeKB * 1024).toDouble() / outputStream.size())
            val newWidth = (bitmap.width * scaleFactor).toInt()
            val newHeight = (bitmap.height * scaleFactor).toInt()
            compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }

        return compressedBitmap
    }

    private suspend fun callVisionAPI(
        config: AIConfig,
        base64Image: String,
        userHint: String
    ): FoodAnalysisResult {
        val requestBody = buildVisionRequest(config, base64Image, userHint)

        val request = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("API返回为空")

        if (!response.isSuccessful) {
            throw Exception("API调用失败: ${response.code} - $responseBody")
        }

        return parseVisionResponse(responseBody, config.protocol)
    }

    private fun buildVisionRequest(
        config: AIConfig,
        base64Image: String,
        userHint: String
    ): String {
        val systemPrompt = """你是一个专业的营养师，擅长通过图片识别食物并分析其营养成分。
请仔细分析图片中的食物，提供以下信息：
1. 食物名称（具体、准确）
2. 估计的重量（克）
3. 热量（千卡）
4. 蛋白质（克）
5. 碳水化合物（克）
6. 脂肪（克）
7. 简短的描述

请以JSON格式返回，格式如下：
{
  "foodName": "食物名称",
  "estimatedWeight": 100,
  "calories": 250,
  "protein": 10.5,
  "carbs": 30.0,
  "fat": 8.5,
  "description": "描述"
}"""

        val userContent = if (userHint.isNotBlank()) {
            "用户提示：$userHint\n\n请分析这张图片中的食物。"
        } else {
            "请分析这张图片中的食物。"
        }

        return when (config.protocol) {
            AIProtocol.OPENAI, AIProtocol.GLM, AIProtocol.KIMI, AIProtocol.QWEN, AIProtocol.DEEPSEEK -> {
                buildOpenAIVisionRequest(config.modelId, systemPrompt, userContent, base64Image)
            }
            AIProtocol.CLAUDE -> {
                buildClaudeVisionRequest(config.modelId, systemPrompt, userContent, base64Image)
            }
            AIProtocol.GEMINI -> {
                buildGeminiVisionRequest(systemPrompt, userContent, base64Image)
            }
        }
    }

    private fun buildOpenAIVisionRequest(
        modelId: String,
        systemPrompt: String,
        userContent: String,
        base64Image: String
    ): String {
        return """{
            "model": "$modelId",
            "messages": [
                {
                    "role": "system",
                    "content": "${systemPrompt.replace("\"", "\\\"")}"
                },
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": "$userContent"
                        },
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": "data:image/jpeg;base64,$base64Image"
                            }
                        }
                    ]
                }
            ],
            "temperature": 0.3,
            "max_tokens": 1000
        }""".trimIndent()
    }

    private fun buildClaudeVisionRequest(
        modelId: String,
        systemPrompt: String,
        userContent: String,
        base64Image: String
    ): String {
        return """{
            "model": "$modelId",
            "system": "${systemPrompt.replace("\"", "\\\"")}",
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": "$userContent"
                        },
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": "image/jpeg",
                                "data": "$base64Image"
                            }
                        }
                    ]
                }
            ],
            "max_tokens": 1000
        }""".trimIndent()
    }

    private fun buildGeminiVisionRequest(
        systemPrompt: String,
        userContent: String,
        base64Image: String
    ): String {
        return """{
            "contents": [
                {
                    "parts": [
                        {
                            "text": "${systemPrompt}\n\n$userContent"
                        },
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": "$base64Image"
                            }
                        }
                    ]
                }
            ],
            "generationConfig": {
                "temperature": 0.3,
                "maxOutputTokens": 1000
            }
        }""".trimIndent()
    }

    private fun parseVisionResponse(responseBody: String, protocol: AIProtocol): FoodAnalysisResult {
        return when (protocol) {
            AIProtocol.OPENAI, AIProtocol.GLM, AIProtocol.KIMI, AIProtocol.QWEN, AIProtocol.DEEPSEEK -> {
                parseOpenAIResponse(responseBody)
            }
            AIProtocol.CLAUDE -> {
                parseClaudeResponse(responseBody)
            }
            AIProtocol.GEMINI -> {
                parseGeminiResponse(responseBody)
            }
        }
    }

    private fun parseOpenAIResponse(responseBody: String): FoodAnalysisResult {
        val response = json.decodeFromString<OpenAIVisionResponse>(responseBody)
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseClaudeResponse(responseBody: String): FoodAnalysisResult {
        val response = json.decodeFromString<ClaudeVisionResponse>(responseBody)
        val content = response.content.firstOrNull()?.text
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseGeminiResponse(responseBody: String): FoodAnalysisResult {
        val response = json.decodeFromString<GeminiVisionResponse>(responseBody)
        val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseAnalysisResult(content: String): FoodAnalysisResult {
        // 尝试从内容中提取JSON
        val jsonPattern = """\{[\s\S]*?\}""".toRegex()
        val jsonMatch = jsonPattern.find(content)
        val jsonString = jsonMatch?.value ?: content

        return try {
            json.decodeFromString<FoodAnalysisResult>(jsonString)
        } catch (e: Exception) {
            // 如果解析失败，返回默认值
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
}

@Serializable
data class FoodAnalysisResult(
    val foodName: String = "",
    val estimatedWeight: Int = 0,
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val description: String = ""
)

// OpenAI格式响应
@Serializable
data class OpenAIVisionResponse(
    val choices: List<OpenAIChoice>
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

@Serializable
data class OpenAIMessage(
    val content: String
)

// Claude格式响应
@Serializable
data class ClaudeVisionResponse(
    val content: List<ClaudeContent>
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String
)

// Gemini格式响应
@Serializable
data class GeminiVisionResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)
