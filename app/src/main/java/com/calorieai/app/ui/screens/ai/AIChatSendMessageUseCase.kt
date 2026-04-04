package com.calorieai.app.ui.screens.ai

import com.calorieai.app.service.ai.AIChatService
import com.calorieai.app.service.ai.AIChatService.ConversationMessage
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatSendMessageUseCase @Inject constructor(
    private val aiChatService: AIChatService
) {
    fun buildConversationHistory(
        existingMessages: List<ChatMessage>,
        userMessage: ChatMessage
    ): List<ConversationMessage> {
        return (existingMessages + userMessage).map { chatMessage ->
            ConversationMessage(
                role = if (chatMessage.isFromUser) "user" else "assistant",
                content = chatMessage.content,
                timestamp = chatMessage.timestamp
            )
        }
    }

    suspend fun streamAssistantReply(
        inputMessage: String,
        conversationHistory: List<ConversationMessage>,
        onChunk: (String) -> Unit
    ): String {
        val fullResponse = StringBuilder()
        aiChatService.sendMessageStream(inputMessage, conversationHistory).collect { chunk ->
            val piece = chunk.toString()
            fullResponse.append(piece)
            onChunk(piece)
        }
        return fullResponse.toString()
    }
}
