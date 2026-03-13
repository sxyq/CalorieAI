package com.calorieai.app.service.ai

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AITokenUsageRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val aiConfigRepository: AIConfigRepository,
    private val aiTokenUsageRepository: AITokenUsageRepository,
    private val aiRateLimiter: AIRateLimiter
) {

    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        const val DEFAULT_DAILY_LIMIT = 50
    }

    suspend fun sendMessage(message: String): String {
        val config = aiConfigRepository.getDefaultConfig().firstOrNull()
            ?: throw Exception("未配置AI服务，请先配置AI服务")
        
        val (canCall, remaining) = aiRateLimiter.canMakeCall(config.id, DEFAULT_DAILY_LIMIT)
        if (!canCall) {
            throw Exception("今日API调用次数已用完（限制：${DEFAULT_DAILY_LIMIT}次/天），请明天再试")
        }

        val result = callAIAPI(config, message)
        aiRateLimiter.recordCall(config.id)
        
        return result
    }
    
    suspend fun getRemainingCalls(): Int {
        val config = aiConfigRepository.getDefaultConfig().firstOrNull()
            ?: return 0
        return aiRateLimiter.getRemainingCalls(config.id, DEFAULT_DAILY_LIMIT)
    }
    
    suspend fun getTodayCallCount(): Int {
        val config = aiConfigRepository.getDefaultConfig().firstOrNull()
            ?: return 0
        return aiRateLimiter.getTodayCallCount(config.id)
    }

    private suspend fun callAIAPI(config: AIConfig, message: String): String {
        val systemPrompt = """你是一位专业的营养师和健康顾问。请根据用户的问题提供准确、实用的营养和健康建议。

注意事项：
1. 回答要简洁明了，适合普通用户理解
2. 提供具体的建议和数据支持
3. 如果不确定，建议用户咨询专业医生
4. 保持友好、鼓励的语气"""

        val requestBody = buildRequestBody(config, systemPrompt, message)

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

        val result = parseResponse(responseBody, config.protocol.name)
        
        recordTokenUsage(config, responseBody, message, systemPrompt)
        
        return result
    }
    
    private suspend fun recordTokenUsage(config: AIConfig, responseBody: String, userMessage: String, systemPrompt: String) {
        try {
            val usage = extractTokenUsage(responseBody, config.protocol.name)
            if (usage != null) {
                val cost = calculateCost(usage.promptTokens, usage.completionTokens, config.protocol.name, config.modelId)
                aiTokenUsageRepository.recordTokenUsage(
                    configId = config.id,
                    configName = config.name,
                    promptTokens = usage.promptTokens,
                    completionTokens = usage.completionTokens,
                    cost = cost
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun extractTokenUsage(responseBody: String, protocol: String): TokenUsage? {
        return try {
            when (protocol) {
                "OPENAI", "KIMI", "GLM", "QWEN", "DEEPSEEK" -> {
                    val response = json.decodeFromString<OpenAIChatResponse>(responseBody)
                    response.usage?.let {
                        TokenUsage(it.prompt_tokens, it.completion_tokens)
                    }
                }
                "CLAUDE" -> {
                    val response = json.decodeFromString<ClaudeChatResponse>(responseBody)
                    response.usage?.let {
                        TokenUsage(it.input_tokens, it.output_tokens)
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
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

    private fun buildRequestBody(config: AIConfig, systemPrompt: String, userMessage: String): String {
        return when (config.protocol.name) {
            "CLAUDE" -> buildClaudeRequestBody(config, systemPrompt, userMessage)
            "GEMINI" -> buildGeminiRequestBody(config, systemPrompt, userMessage)
            else -> buildOpenAIRequestBody(config, systemPrompt, userMessage)
        }
    }

    private fun buildOpenAIRequestBody(config: AIConfig, systemPrompt: String, userMessage: String): String {
        return """
            {
                "model": "${config.modelId}",
                "messages": [
                    {"role": "system", "content": "$systemPrompt"},
                    {"role": "user", "content": "$userMessage"}
                ],
                "temperature": 0.7,
                "max_tokens": 1000
            }
        """.trimIndent()
    }

    private fun buildClaudeRequestBody(config: AIConfig, systemPrompt: String, userMessage: String): String {
        return """
            {
                "model": "${config.modelId}",
                "system": "$systemPrompt",
                "messages": [
                    {"role": "user", "content": "$userMessage"}
                ],
                "max_tokens": 1000
            }
        """.trimIndent()
    }

    private fun buildGeminiRequestBody(config: AIConfig, systemPrompt: String, userMessage: String): String {
        return """
            {
                "contents": [
                    {
                        "parts": [
                            {"text": "$systemPrompt\n\n$userMessage"}
                        ]
                    }
                ],
                "generationConfig": {
                    "temperature": 0.7,
                    "maxOutputTokens": 1000
                }
            }
        """.trimIndent()
    }

    private fun parseResponse(responseBody: String, protocol: String): String {
        return try {
            when (protocol) {
                "CLAUDE" -> {
                    val response = json.decodeFromString<ClaudeChatResponse>(responseBody)
                    response.content.firstOrNull()?.text ?: "抱歉，我无法理解您的请求"
                }
                "GEMINI" -> {
                    val response = json.decodeFromString<GeminiChatResponse>(responseBody)
                    response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "抱歉，我无法理解您的请求"
                }
                else -> {
                    val response = json.decodeFromString<OpenAIChatResponse>(responseBody)
                    response.choices.firstOrNull()?.message?.content ?: "抱歉，我无法理解您的请求"
                }
            }
        } catch (e: Exception) {
            "抱歉，解析响应时出错：${e.message}"
        }
    }
}

private data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int
)

// OpenAI格式响应
@kotlinx.serialization.Serializable
data class OpenAIChatResponse(
    val choices: List<OpenAIChatChoice>,
    val usage: OpenAIUsage? = null
)

@kotlinx.serialization.Serializable
data class OpenAIChatChoice(
    val message: OpenAIChatMessage
)

@kotlinx.serialization.Serializable
data class OpenAIChatMessage(
    val content: String
)

@kotlinx.serialization.Serializable
data class OpenAIUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int
)

// Claude格式响应
@kotlinx.serialization.Serializable
data class ClaudeChatResponse(
    val content: List<ClaudeChatContent>,
    val usage: ClaudeUsage? = null
)

@kotlinx.serialization.Serializable
data class ClaudeChatContent(
    val type: String,
    val text: String
)

@kotlinx.serialization.Serializable
data class ClaudeUsage(
    val input_tokens: Int,
    val output_tokens: Int
)

// Gemini格式响应
@kotlinx.serialization.Serializable
data class GeminiChatResponse(
    val candidates: List<GeminiChatCandidate>
)

@kotlinx.serialization.Serializable
data class GeminiChatCandidate(
    val content: GeminiChatContent
)

@kotlinx.serialization.Serializable
data class GeminiChatContent(
    val parts: List<GeminiChatPart>
)

@kotlinx.serialization.Serializable
data class GeminiChatPart(
    val text: String
)
