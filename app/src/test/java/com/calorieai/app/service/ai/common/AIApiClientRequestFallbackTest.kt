package com.calorieai.app.service.ai.common

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.IconType
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIApiClientRequestFallbackTest {

    private lateinit var server: MockWebServer
    private lateinit var client: AIApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = AIApiClient(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun visionRaw_longcatOmniSendsBase64Array() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    "{" +
                        "\"choices\":[{\"message\":{\"content\":\"ok\"}}]," +
                        "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1}" +
                    "}"
                )
        )

        val result = client.visionRaw(
            config = testConfig(protocol = AIProtocol.LONGCAT),
            systemPrompt = "system",
            userMessage = "analyze",
            base64Image = "ZmFrZQ=="
        )

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        val userContent = body.getAsJsonArray("messages")
            .get(1)
            .asJsonObject
            .getAsJsonArray("content")
        val imagePart = userContent
            .first { it.asJsonObject.get("type").asString == "input_image" }
            .asJsonObject
            .getAsJsonObject("input_image")
        val data = imagePart.getAsJsonArray("data")
        assertEquals("ok", result.first)
        assertEquals("base64", imagePart.get("type").asString)
        assertEquals(1, data.size())
        assertEquals("ZmFrZQ==", data.get(0).asString)
    }

    @Test
    fun chatRaw_extractsReasoningContentWhenContentMissing() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    "{" +
                        "\"choices\":[{\"message\":{\"reasoning_content\":\"pong\"}}]," +
                        "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1}" +
                    "}"
                )
        )

        val result = client.chatRaw(
            config = testConfig(protocol = AIProtocol.LONGCAT).copy(
                modelId = "LongCat-Flash-Thinking-2601",
                isImageUnderstanding = false
            ),
            systemPrompt = "system",
            userMessage = "ping"
        )

        assertEquals("pong", result.first)
    }

    @Test
    fun chatRaw_throwsHttpErrorOnServerFailure() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{\"error\":\"server_error\"}"))

        val ex = assertThrows(AIApiException::class.java) {
            runBlocking {
                client.chatRaw(
                    config = testConfig(protocol = AIProtocol.LONGCAT),
                    systemPrompt = "system",
                    userMessage = "ping"
                )
            }
        }

        assertEquals(AIErrorCategory.HTTP, ex.category)
        assertEquals(500, ex.httpCode)
    }

    private fun testConfig(
        protocol: AIProtocol,
        apiUrl: String = server.url("/openai/v1/chat/completions").toString()
    ): AIConfig {
        return AIConfig(
            id = "cfg",
            name = "cfg",
            icon = "??",
            iconType = IconType.EMOJI,
            protocol = protocol,
            apiUrl = apiUrl,
            apiKey = "test-key",
            modelId = "LongCat-Flash-Omni-2603",
            isImageUnderstanding = true,
            isDefault = true
        )
    }
}
