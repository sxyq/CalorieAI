package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI对话历史记录
 */
@Entity(tableName = "ai_chat_history")
data class AIChatHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,  // 会话ID，用于分组
    val title: String,  // 对话标题（第一条用户消息的摘要）
    val messages: String,  // JSON格式的消息列表
    val createdAt: Long,  // 创建时间
    val updatedAt: Long,  // 最后更新时间
    val messageCount: Int = 0,  // 消息数量
    val isPinned: Boolean = false  // 是否置顶
)

/**
 * 聊天消息数据类（用于序列化存储）
 */
@kotlinx.serialization.Serializable
data class ChatMessageData(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val type: String = "TEXT"  // TEXT, IMAGE, SYSTEM
)

/**
 * 会话摘要信息
 */
data class ChatSessionSummary(
    val sessionId: String,
    val title: String,
    val lastMessage: String,
    val updatedAt: Long,
    val messageCount: Int,
    val isPinned: Boolean
)
