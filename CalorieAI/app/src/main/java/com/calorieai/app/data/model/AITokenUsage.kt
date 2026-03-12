package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI Token使用记录
 */
@Entity(tableName = "ai_token_usage")
data class AITokenUsage(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val configId: String,
    val configName: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cost: Double // 估算成本（美元）
)

/**
 * Token使用统计
 */
data class TokenUsageStats(
    val totalTokens: Int,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalCost: Double,
    val requestCount: Int,
    val todayTokens: Int,
    val todayCost: Double,
    val monthTokens: Int,
    val monthCost: Double
)
