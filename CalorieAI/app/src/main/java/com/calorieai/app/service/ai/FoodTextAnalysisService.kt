package com.calorieai.app.service.ai

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.repository.AIConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 食物文本分析服务 - 根据用户输入的食物描述分析热量和营养成分
 */
@Singleton
class FoodTextAnalysisService @Inject constructor(
    private val aiConfigRepository: AIConfigRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 分析食物文本描述
     * @param foodDescription 用户输入的食物描述，如"番茄炒蛋，番茄150g，鸡蛋2个，油10g"
     * @return 分析结果，包含食物名称、热量和营养成分
     */
    suspend fun analyzeFoodText(foodDescription: String): Result<TextFoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 获取默认AI配置
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))

            // 构建请求
            val requestBody = buildAnalysisRequest(config, foodDescription)

            val request = Request.Builder()
                .url(config.apiUrl)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.failure(
                    Exception("API请求失败: ${response.code} - ${responseBody ?: "未知错误"}")
                )
            }

            // 解析响应
            val result = parseAnalysisResponse(responseBody, config.protocol.name)
            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 构建分析请求
     */
    private fun buildAnalysisRequest(config: AIConfig, foodDescription: String): String {
        val systemPrompt = """你是一个专业的营养师，擅长分析食物的热量和营养成分。
请根据用户输入的食物描述，分析并提供以下信息：
1. 食物名称（简洁明了）
2. 估算重量（克）
3. 热量（千卡）
4. 蛋白质（克）
5. 碳水化合物（克）
6. 脂肪（克）

请以JSON格式返回结果，格式如下：
{
  "foodName": "食物名称",
  "estimatedWeight": 200,
  "calories": 300,
  "protein": 15.5,
  "carbs": 25.0,
  "fat": 12.0
}

注意：
- 如果用户没有提供具体重量，请根据常见份量估算
- 营养成分请根据标准食物成分表计算
- 只返回JSON，不要包含其他说明文字"""

        val userPrompt = "请分析以下食物的热量和营养成分：$foodDescription"

        return when (config.protocol) {
            AIProtocol.OPENAI, AIProtocol.GLM, AIProtocol.KIMI, AIProtocol.QWEN, AIProtocol.DEEPSEEK -> {
                json.encodeToString(
                    OpenAITextRequest(
                        model = config.modelId,
                        messages = listOf(
                            TextMessage(role = "system", content = systemPrompt),
                            TextMessage(role = "user", content = userPrompt)
                        ),
                        temperature = 0.3
                    )
                )
            }
            AIProtocol.CLAUDE -> {
                json.encodeToString(
                    ClaudeTextRequest(
                        model = config.modelId,
                        max_tokens = 1024,
                        messages = listOf(
                            TextMessage(role = "user", content = "$systemPrompt\n\n$userPrompt")
                        )
                    )
                )
            }
            AIProtocol.GEMINI -> {
                json.encodeToString(
                    GeminiTextRequest(
                        contents = listOf(
                            GeminiTextContent(
                                parts = listOf(
                                    GeminiTextPart(text = "$systemPrompt\n\n$userPrompt")
                                )
                            )
                        )
                    )
                )
            }
        }
    }

    /**
     * 解析分析响应
     */
    private fun parseAnalysisResponse(responseBody: String, protocol: String): TextFoodAnalysisResult {
        return when (protocol) {
            "CLAUDE" -> parseClaudeResponse(responseBody)
            "GEMINI" -> parseGeminiResponse(responseBody)
            else -> parseOpenAIResponse(responseBody)
        }
    }

    private fun parseOpenAIResponse(responseBody: String): TextFoodAnalysisResult {
        val response = json.decodeFromString<OpenAITextResponse>(responseBody)
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseClaudeResponse(responseBody: String): TextFoodAnalysisResult {
        val response = json.decodeFromString<ClaudeTextResponse>(responseBody)
        val content = response.content.firstOrNull()?.text
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseGeminiResponse(responseBody: String): TextFoodAnalysisResult {
        val response = json.decodeFromString<GeminiTextResponse>(responseBody)
        val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("API返回内容为空")
        return parseAnalysisResult(content)
    }

    private fun parseAnalysisResult(content: String): TextFoodAnalysisResult {
        // 尝试从内容中提取JSON
        val jsonPattern = """\{[\s\S]*?\}""".toRegex()
        val jsonMatch = jsonPattern.find(content)
        val jsonString = jsonMatch?.value ?: content

        return try {
            json.decodeFromString<TextFoodAnalysisResult>(jsonString)
        } catch (e: Exception) {
            // 如果解析失败，返回默认值
            TextFoodAnalysisResult(
                foodName = "未知食物",
                estimatedWeight = 0,
                calories = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f
            )
        }
    }
}

/**
 * 文本食物分析结果
 */
@Serializable
data class TextFoodAnalysisResult(
    val foodName: String = "",
    val estimatedWeight: Int = 0,
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f
)

// OpenAI 请求/响应数据类
@Serializable
private data class OpenAITextRequest(
    val model: String,
    val messages: List<TextMessage>,
    val temperature: Double = 0.3
)

@Serializable
private data class TextMessage(
    val role: String,
    val content: String
)

@Serializable
private data class OpenAITextResponse(
    val choices: List<TextChoice>
)

@Serializable
private data class TextChoice(
    val message: TextMessage
)

// Claude 请求/响应数据类
@Serializable
private data class ClaudeTextRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<TextMessage>
)

@Serializable
private data class ClaudeTextResponse(
    val content: List<ClaudeTextContent>
)

@Serializable
private data class ClaudeTextContent(
    val text: String
)

// Gemini 请求/响应数据类
@Serializable
private data class GeminiTextRequest(
    val contents: List<GeminiTextContent>
)

@Serializable
private data class GeminiTextContent(
    val parts: List<GeminiTextPart>
)

@Serializable
private data class GeminiTextPart(
    val text: String
)

@Serializable
private data class GeminiTextResponse(
    val candidates: List<GeminiTextCandidate>
)

@Serializable
private data class GeminiTextCandidate(
    val content: GeminiTextContent
)