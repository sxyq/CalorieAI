package com.calorieai.app.ui.screens.ai

import com.calorieai.app.data.model.ChatMessageData
import com.calorieai.app.data.model.ChatSessionSummary
import com.calorieai.app.data.repository.AIChatHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatSessionUseCase @Inject constructor(
    private val aiChatHistoryRepository: AIChatHistoryRepository
) {
    fun observeSessions(): Flow<List<ChatSessionInfo>> {
        return aiChatHistoryRepository.getAllHistory().map { sessions ->
            sessions.map { it.toChatSessionInfo() }
        }
    }

    suspend fun createNewSession(title: String = "新对话"): String {
        return aiChatHistoryRepository.createNewSession(title)
    }

    suspend fun loadSession(sessionId: String): LoadedSession? {
        val (history, messages) = aiChatHistoryRepository.getHistoryBySessionId(sessionId)
        return history?.let {
            LoadedSession(
                sessionId = sessionId,
                title = it.title,
                messages = messages.map { msg -> msg.toChatMessage() }
            )
        }
    }

    suspend fun saveCurrentSession(state: AIChatUiState) {
        if (state.messages.isEmpty()) return
        val title = state.messages.firstOrNull { it.isFromUser }?.content?.take(30) ?: "新对话"
        val messages = state.messages.map { it.toChatMessageData() }
        aiChatHistoryRepository.saveChatSession(
            sessionId = state.currentSessionId,
            title = title,
            messages = messages
        )
    }

    suspend fun deleteSession(sessionId: String) {
        aiChatHistoryRepository.deleteSession(sessionId)
    }

    suspend fun togglePinSession(sessionId: String) {
        aiChatHistoryRepository.togglePin(sessionId)
    }

    data class LoadedSession(
        val sessionId: String,
        val title: String,
        val messages: List<ChatMessage>
    )

    private fun ChatMessage.toChatMessageData(): ChatMessageData {
        return ChatMessageData(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            type = "TEXT"
        )
    }

    private fun ChatMessageData.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp
        )
    }

    private fun ChatSessionSummary.toChatSessionInfo(): ChatSessionInfo {
        return ChatSessionInfo(
            sessionId = sessionId,
            title = title,
            lastMessage = lastMessage,
            updatedAt = updatedAt,
            messageCount = messageCount,
            isPinned = isPinned
        )
    }
}
