package com.calorieai.app.data.repository

import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.model.AIChatHistory
import com.calorieai.app.data.model.ChatMessageData
import com.calorieai.app.data.model.ChatSessionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI对话历史仓库
 */
@Singleton
class AIChatHistoryRepository @Inject constructor(
    private val aiChatHistoryDao: AIChatHistoryDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllHistory(): Flow<List<ChatSessionSummary>> {
        return aiChatHistoryDao.getAllHistory().map { list ->
            list.map { it.toSummary() }
        }
    }

    suspend fun getAllRawHistory(): List<AIChatHistory> {
        return aiChatHistoryDao.getAllHistoryOnce()
    }

    suspend fun getHistoryBySessionId(sessionId: String): Pair<AIChatHistory?, List<ChatMessageData>> {
        val history = aiChatHistoryDao.getHistoryBySessionId(sessionId)
        val messages = history?.let {
            json.decodeFromString<List<ChatMessageData>>(it.messages)
        } ?: emptyList()
        return history to messages
    }

    suspend fun saveChatSession(
        sessionId: String,
        title: String,
        messages: List<ChatMessageData>,
        isPinned: Boolean = false
    ) {
        val existing = aiChatHistoryDao.getHistoryBySessionId(sessionId)
        val now = System.currentTimeMillis()
        
        val history = AIChatHistory(
            id = existing?.id ?: 0,
            sessionId = sessionId,
            title = title.take(50), // 限制标题长度
            messages = json.encodeToString(messages),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            messageCount = messages.size,
            isPinned = isPinned
        )
        
        if (existing != null) {
            aiChatHistoryDao.update(history)
        } else {
            aiChatHistoryDao.insert(history)
        }
    }

    suspend fun createNewSession(title: String = "新对话"): String {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val history = AIChatHistory(
            sessionId = sessionId,
            title = title,
            messages = "[]",
            createdAt = now,
            updatedAt = now,
            messageCount = 0,
            isPinned = false
        )
        aiChatHistoryDao.insert(history)
        return sessionId
    }

    suspend fun deleteSession(sessionId: String) {
        aiChatHistoryDao.deleteBySessionId(sessionId)
    }

    suspend fun deleteAllHistory() {
        aiChatHistoryDao.deleteAll()
    }

    suspend fun togglePin(sessionId: String) {
        val history = aiChatHistoryDao.getHistoryBySessionId(sessionId)
        history?.let {
            val updated = it.copy(isPinned = !it.isPinned)
            aiChatHistoryDao.update(updated)
        }
    }

    suspend fun getRecentSessions(limit: Int): List<ChatSessionSummary> {
        return aiChatHistoryDao.getRecentHistory(limit).map { it.toSummary() }
    }

    private fun AIChatHistory.toSummary(): ChatSessionSummary {
        val messages = json.decodeFromString<List<ChatMessageData>>(this.messages)
        val lastMessage = messages.lastOrNull()?.content?.take(30) ?: "无消息"
        
        return ChatSessionSummary(
            sessionId = sessionId,
            title = title,
            lastMessage = lastMessage,
            updatedAt = updatedAt,
            messageCount = messageCount,
            isPinned = isPinned
        )
    }
}
