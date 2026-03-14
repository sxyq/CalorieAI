package com.calorieai.app.service.ai.common

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    companion object {
        private const val MEDIA_JSON = "application/json; charset=utf-8"
    }

    // 简单的请求/响应数据类（完全模仿 Deadliner）
    data class Message(
        val role: String,
        val content: String
    )

    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val stream: Boolean = false
    )

    data class ChatResponse(
        val choices: List<Choice>,
        val usage: Usage?
    )

    data class Choice(
        val message: Message
    )

    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int? = null,
        @SerializedName("completion_tokens") val completionTokens: Int? = null
    )

    suspend fun chat(
        config: AIConfig,
        systemPrompt: String,
        userMessage: String,
        temperature: Double = 0.7,
        maxTokens: Int = 1000
    ): String = withContext(Dispatchers.IO) {
        // 判断是否为 Omni 模型（需要特殊格式）
        val isOmniModel = config.modelId.contains("Omni", ignoreCase = true)
        
        val requestBody = if (isOmniModel) {
            // Omni 模型使用数组格式的 content
            mapOf(
                "model" to config.modelId,
                "messages" to listOf(
                    mapOf(
                        "role" to "system",
                        "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                    ),
                    mapOf(
                        "role" to "user",
                        "content" to listOf(mapOf("type" to "text", "text" to userMessage))
                    )
                ),
                "stream" to false
            )
        } else {
            // 标准模型使用字符串格式的 content
            ChatRequest(
                model = config.modelId,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userMessage)
                ),
                stream = false
            )
        }

        val body = gson.toJson(requestBody).toRequestBody(MEDIA_JSON.toMediaType())
        
        val httpRequest = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", MEDIA_JSON)
            .post(body)
            .build()

        okHttpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw AIApiException("API调用失败(${response.code}): $errorBody", response.code, errorBody)
            }
            
            val responseBody = response.body?.string() ?: throw AIApiException("API返回空")
            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            chatResponse.choices.firstOrNull()?.message?.content ?: throw AIApiException("响应内容为空")
        }
    }

    suspend fun chatRaw(
        config: AIConfig,
        systemPrompt: String,
        userMessage: String,
        temperature: Double = 0.7,
        maxTokens: Int = 1000
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        // 判断是否为 Omni 模型（需要特殊格式）
        val isOmniModel = config.modelId.contains("Omni", ignoreCase = true)
        
        val requestBody = if (isOmniModel) {
            // Omni 模型使用数组格式的 content
            mapOf(
                "model" to config.modelId,
                "messages" to listOf(
                    mapOf(
                        "role" to "system",
                        "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                    ),
                    mapOf(
                        "role" to "user",
                        "content" to listOf(mapOf("type" to "text", "text" to userMessage))
                    )
                ),
                "stream" to false
            )
        } else {
            // 标准模型使用字符串格式的 content
            ChatRequest(
                model = config.modelId,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userMessage)
                ),
                stream = false
            )
        }

        val body = gson.toJson(requestBody).toRequestBody(MEDIA_JSON.toMediaType())
        
        val httpRequest = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", MEDIA_JSON)
            .post(body)
            .build()

        okHttpClient.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: throw AIApiException("API返回空")
            
            if (!response.isSuccessful) {
                throw AIApiException("API调用失败(${response.code}): $responseBody", response.code, responseBody)
            }
            
            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            val content = chatResponse.choices.firstOrNull()?.message?.content ?: throw AIApiException("响应内容为空")
            content to responseBody
        }
    }

    suspend fun vision(
        config: AIConfig,
        systemPrompt: String,
        userMessage: String,
        base64Image: String,
        temperature: Double = 0.3,
        maxTokens: Int = 1000
    ): String = withContext(Dispatchers.IO) {
        // Vision 使用 Map 构建复杂结构
        val userContent = listOf(
            mapOf("type" to "text", "text" to userMessage),
            mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image"))
        )

        val requestBody = mapOf(
            "model" to config.modelId,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userContent)
            )
        )

        val body = gson.toJson(requestBody).toRequestBody(MEDIA_JSON.toMediaType())
        
        val httpRequest = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", MEDIA_JSON)
            .post(body)
            .build()

        okHttpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw AIApiException("API调用失败(${response.code}): $errorBody", response.code, errorBody)
            }
            
            val responseBody = response.body?.string() ?: throw AIApiException("API返回空")
            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            chatResponse.choices.firstOrNull()?.message?.content ?: throw AIApiException("响应内容为空")
        }
    }

    fun extractOpenAIUsage(rawResponse: String): Usage? {
        return try {
            gson.fromJson(rawResponse, ChatResponse::class.java).usage
        } catch (e: Exception) {
            null
        }
    }

    fun extractClaudeUsage(rawResponse: String): Usage? {
        return extractOpenAIUsage(rawResponse)
    }
}

class AIApiException(
    override val message: String,
    val httpCode: Int = 0,
    val responseBody: String? = null
) : Exception(message)
