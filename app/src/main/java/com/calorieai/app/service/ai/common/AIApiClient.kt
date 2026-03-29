package com.calorieai.app.service.ai.common

import com.calorieai.app.utils.SecureLogger
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    companion object {
        private const val MEDIA_JSON = "application/json; charset=utf-8"
        private const val TAG = "AIApiClient"
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
        val message: Message?,
        val delta: Delta?
    )

    data class Delta(
        val content: String?
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
    ): String {
        val (content, _) = chatRaw(config, systemPrompt, userMessage, temperature, maxTokens)
        return content
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
                SecureLogger.logApiError(TAG, response.code, responseBody)
                throw AIApiException("API调用失败(${response.code})", response.code, responseBody)
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
        val (content, _) = visionRaw(
            config = config,
            systemPrompt = systemPrompt,
            userMessage = userMessage,
            base64Image = base64Image,
            temperature = temperature,
            maxTokens = maxTokens
        )
        content
    }

    suspend fun visionRaw(
        config: AIConfig,
        systemPrompt: String,
        userMessage: String,
        base64Image: String,
        temperature: Double = 0.3,
        maxTokens: Int = 1000
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val isOmniModel = config.modelId.contains("omni", ignoreCase = true) ||
            config.modelId.contains("o-mini", ignoreCase = true) ||
            config.modelId.contains("omini", ignoreCase = true)
        val userContent = listOf(
            mapOf("type" to "text", "text" to userMessage),
            mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image"))
        )

        val messages = if (isOmniModel) {
            listOf(
                mapOf(
                    "role" to "system",
                    "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                ),
                mapOf("role" to "user", "content" to userContent)
            )
        } else {
            listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userContent)
            )
        }

        val requestBody = mapOf(
            "model" to config.modelId,
            "messages" to messages,
            "temperature" to temperature,
            "max_tokens" to maxTokens
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
                SecureLogger.logApiError(TAG, response.code, errorBody)
                throw AIApiException("API调用失败(${response.code})", response.code, errorBody)
            }
            
            val responseBody = response.body?.string() ?: throw AIApiException("API返回空")
            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            val content = chatResponse.choices.firstOrNull()?.message?.content ?: throw AIApiException("响应内容为空")
            content to responseBody
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

    /**
     * 流式聊天 - 返回Flow<String>实现打字机效果
     */
    fun chatStream(
        config: AIConfig,
        systemPrompt: String,
        userMessage: String,
        temperature: Double = 0.7,
        maxTokens: Int = 1000
    ): Flow<String> = flow {
        val isOmniModel = config.modelId.contains("Omni", ignoreCase = true)
        
        val requestBody = if (isOmniModel) {
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
                "stream" to true
            )
        } else {
            mapOf(
                "model" to config.modelId,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to userMessage)
                ),
                "stream" to true
            )
        }

        val body = gson.toJson(requestBody).toRequestBody(MEDIA_JSON.toMediaType())
        
        val httpRequest = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", MEDIA_JSON)
            .addHeader("Accept", "text/event-stream")
            .post(body)
            .build()

        val eventSourceFactory = EventSources.createFactory(okHttpClient)
        val fullContent = AtomicReference("")
        val error = AtomicReference<Throwable?>(null)
        val isComplete = AtomicReference(false)

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    isComplete.set(true)
                    return
                }
                
                try {
                    val response = gson.fromJson(data, ChatResponse::class.java)
                    val content = response.choices.firstOrNull()?.delta?.content
                    if (!content.isNullOrBlank()) {
                        fullContent.getAndAccumulate(content) { old, new -> old + new }
                    }
                } catch (e: Exception) {
                    // 忽略解析错误，继续处理
                }
            }

            override fun onClosed(eventSource: EventSource) {
                isComplete.set(true)
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                error.set(t ?: Exception("Stream failed: ${response?.code} - ${response?.body?.string()}"))
                isComplete.set(true)
            }
        }

        eventSourceFactory.newEventSource(httpRequest, listener)

        // 等待流式数据并逐字发送
        var lastEmittedLength = 0
        while (!isComplete.get()) {
            val currentContent = fullContent.get()
            if (currentContent.length > lastEmittedLength) {
                val newContent = currentContent.substring(lastEmittedLength)
                // 逐字符发送实现打字机效果
                newContent.forEach { char ->
                    emit(char.toString())
                    kotlinx.coroutines.delay(30) // 30ms每字符
                }
                lastEmittedLength = currentContent.length
            }
            kotlinx.coroutines.delay(10) // 检查间隔
        }

        // 发送剩余内容
        val finalContent = fullContent.get()
        if (finalContent.length > lastEmittedLength) {
            val remainingContent = finalContent.substring(lastEmittedLength)
            remainingContent.forEach { char ->
                emit(char.toString())
                kotlinx.coroutines.delay(30)
            }
        }

        // 检查错误
        error.get()?.let { throw it }
    }.flowOn(Dispatchers.IO)
}

class AIApiException(
    override val message: String,
    val httpCode: Int = 0,
    val responseBody: String? = null
) : Exception(message)
