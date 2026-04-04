package com.calorieai.app.service.ai.common

import com.calorieai.app.utils.SecureLogger
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit
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
        val requestBody = if (isOmniModel(config.modelId)) {
            mapOf(
                "model" to config.modelId,
                "messages" to buildOmniTextMessages(systemPrompt, userMessage),
                "temperature" to temperature,
                "max_tokens" to maxTokens,
                "stream" to false
            )
        } else {
            mapOf(
                "model" to config.modelId,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to userMessage)
                ),
                "temperature" to temperature,
                "max_tokens" to maxTokens,
                "stream" to false
            )
        }
        executeJsonPost(config, requestBody)
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
        val isLongcatOmni = config.protocol == AIProtocol.LONGCAT && isOmniModel(config.modelId)

        if (isLongcatOmni) {
            val requestBody = mapOf(
                "model" to config.modelId,
                "messages" to listOf(
                    mapOf(
                        "role" to "system",
                        "content" to listOf(
                            mapOf("type" to "text", "text" to systemPrompt)
                        )
                    ),
                    mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf(
                                "type" to "input_image",
                                "input_image" to mapOf(
                                    "type" to "base64",
                                    "data" to base64Image
                                )
                            ),
                            mapOf("type" to "text", "text" to userMessage)
                        )
                    )
                ),
                "stream" to false,
                "output_modalities" to listOf("text"),
                "temperature" to temperature,
                "max_tokens" to maxTokens
            )
            return@withContext executeJsonPost(config, requestBody)
        }

        val userContent = listOf(
            mapOf("type" to "text", "text" to userMessage),
            mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image"))
        )

        val messages = if (isOmniModel(config.modelId)) {
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
        executeJsonPost(config, requestBody)
    }

    private fun isOmniModel(modelId: String): Boolean {
        return modelId.contains("omni", ignoreCase = true) ||
            modelId.contains("o-mini", ignoreCase = true) ||
            modelId.contains("omini", ignoreCase = true)
    }

    private fun buildOmniTextMessages(systemPrompt: String, userMessage: String): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "role" to "system",
                "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
            ),
            mapOf(
                "role" to "user",
                "content" to listOf(mapOf("type" to "text", "text" to userMessage))
            )
        )
    }

    private fun executeJsonPost(
        config: AIConfig,
        requestBody: Any,
        client: OkHttpClient = okHttpClient
    ): Pair<String, String> {
        val body = gson.toJson(requestBody).toRequestBody(MEDIA_JSON.toMediaType())
        val httpRequest = Request.Builder()
            .url(config.apiUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", MEDIA_JSON)
            .post(body)
            .build()

        return try {
            client.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    SecureLogger.logApiError(TAG, response.code, responseBody)
                    throw AIApiException(
                        message = "API调用失败(${response.code})",
                        httpCode = response.code,
                        responseBody = responseBody,
                        category = AIErrorCategory.HTTP,
                        retryEligible = response.code in 429..503
                    )
                }
                if (responseBody.isBlank()) {
                    throw AIApiException(
                        message = "API返回空",
                        category = AIErrorCategory.PARSE,
                        retryEligible = true
                    )
                }

                val content = extractResponseContent(responseBody)
                    ?: throw AIApiException(
                        message = "响应内容为空",
                        category = AIErrorCategory.PARSE,
                        retryEligible = true
                    )
                content to responseBody
            }
        } catch (ioe: IOException) {
            throw AIApiException(
                message = "网络不可达: ${ioe.message ?: ioe.javaClass.simpleName}",
                category = AIErrorCategory.NETWORK,
                retryEligible = false,
                cause = ioe
            )
        }
    }

    private fun extractResponseContent(rawResponse: String): String? {
        runCatching {
            val chatResponse = gson.fromJson(rawResponse, ChatResponse::class.java)
            val text = chatResponse.choices.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) return text
        }

        return runCatching {
            val root = JsonParser.parseString(rawResponse).asJsonObject

            val fromChoices = root.getAsJsonArray("choices")
                ?.firstOrNull()
                ?.let { firstChoice ->
                    val choice = if (firstChoice.isJsonObject) firstChoice.asJsonObject else return@let null
                    extractText(choice.get("message"))
                        ?: extractText(choice.get("delta"))
                }
            if (!fromChoices.isNullOrBlank()) {
                return fromChoices
            }

            val fromOutput = root.getAsJsonArray("output")
                ?.firstOrNull()
                ?.let { firstOutput ->
                    val output = if (firstOutput.isJsonObject) firstOutput.asJsonObject else return@let null
                    extractText(output.get("content"))
                }
            if (!fromOutput.isNullOrBlank()) {
                return fromOutput
            }

            extractText(root.get("content"))
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun extractText(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null

        return when {
            element.isJsonPrimitive -> element.asString
            element.isJsonArray -> element.asJsonArray
                .mapNotNull { extractText(it) }
                .joinToString(" ")
                .trim()
                .ifBlank { null }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                extractText(obj.get("text"))
                    ?: extractText(obj.get("value"))
                    ?: extractText(obj.get("output_text"))
                    ?: extractText(obj.get("content"))
            }
            else -> null
        }
    }

    data class ConnectionTestResult(
        val success: Boolean,
        val message: String
    )

    suspend fun testConnection(
        config: AIConfig,
        timeoutSeconds: Long = 5
    ): ConnectionTestResult = withContext(Dispatchers.IO) {
        val requestBody = if (isOmniModel(config.modelId)) {
            mapOf(
                "model" to config.modelId,
                "messages" to buildOmniTextMessages("You are a health assistant.", "ping"),
                "temperature" to 0.1,
                "max_tokens" to 16,
                "stream" to false
            )
        } else {
            ChatRequest(
                model = config.modelId,
                messages = listOf(
                    Message(role = "system", content = "You are a health assistant."),
                    Message(role = "user", content = "ping")
                ),
                stream = false
            )
        }

        val shortTimeoutClient = okHttpClient.newBuilder()
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build()

        return@withContext runCatching {
            val (content, _) = executeJsonPost(
                config = config,
                requestBody = requestBody,
                client = shortTimeoutClient
            )
            if (content.isBlank()) {
                ConnectionTestResult(success = false, message = "连接成功但返回内容为空")
            } else {
                ConnectionTestResult(success = true, message = "连接成功")
            }
        }.getOrElse { error ->
            if (error is AIApiException) {
                val snippet = error.responseBody
                    ?.replace(Regex("\\s+"), " ")
                    ?.take(240)
                    .orEmpty()
                return@getOrElse ConnectionTestResult(
                    success = false,
                    message = "连接失败(HTTP ${error.httpCode}): ${if (snippet.isNotBlank()) snippet else "无错误详情"}"
                )
            }
            ConnectionTestResult(
                success = false,
                message = "连接失败: ${error.message ?: error.javaClass.simpleName}"
            )
        }
    }

    private fun extractStreamChunk(data: String): String? {
        runCatching {
            val response = gson.fromJson(data, ChatResponse::class.java)
            val content = response.choices.firstOrNull()?.delta?.content
            if (!content.isNullOrBlank()) return content
        }

        return runCatching {
            val root = JsonParser.parseString(data).asJsonObject
            val fromChoices = root.getAsJsonArray("choices")
                ?.firstOrNull()
                ?.let { firstChoice ->
                    if (!firstChoice.isJsonObject) return@let null
                    val choice = firstChoice.asJsonObject
                    extractText(choice.get("delta"))
                        ?: extractText(choice.get("message"))
                }
            if (!fromChoices.isNullOrBlank()) {
                return fromChoices
            }
            extractText(root.get("content"))
        }.getOrNull()?.takeIf { it.isNotBlank() }
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
    ): Flow<String> = callbackFlow {
        val isOmniModel = isOmniModel(config.modelId)
        
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
        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                
                try {
                    val content = extractStreamChunk(data)
                    if (!content.isNullOrBlank()) {
                        trySend(content)
                    }
                } catch (e: Exception) {
                    // 忽略单条流式消息解析错误，继续处理后续消息
                }
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val responseSnippet = runCatching { response?.body?.string() }
                    .getOrNull()
                    ?.replace(Regex("\\s+"), " ")
                    ?.take(240)
                val mappedError = when {
                    response != null -> AIApiException(
                        message = "Stream failed(${response.code})",
                        httpCode = response.code,
                        responseBody = responseSnippet,
                        category = AIErrorCategory.HTTP,
                        retryEligible = response.code in 429..503
                    )
                    t != null -> {
                        val classified = AIErrorClassifier.classify(t)
                        AIApiException(
                            message = classified.userMessage,
                            responseBody = classified.detail,
                            category = classified.category,
                            retryEligible = classified.retryEligible,
                            cause = t
                        )
                    }
                    else -> AIApiException(
                        message = "Stream failed",
                        category = AIErrorCategory.UNKNOWN,
                        retryEligible = false
                    )
                }
                close(mappedError)
            }
        }

        val eventSource = eventSourceFactory.newEventSource(httpRequest, listener)
        awaitClose {
            runCatching { eventSource.cancel() }
        }
    }.flowOn(Dispatchers.IO)
}

class AIApiException(
    override val message: String,
    val httpCode: Int = 0,
    val responseBody: String? = null,
    val category: AIErrorCategory = AIErrorCategory.UNKNOWN,
    val retryEligible: Boolean = false,
    cause: Throwable? = null
) : Exception(message, cause)
