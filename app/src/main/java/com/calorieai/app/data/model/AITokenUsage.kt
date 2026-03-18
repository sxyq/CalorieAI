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
    val monthCost: Double,
    val modelStats: List<ModelTokenStats> = emptyList()  // 按模型统计
)

/**
 * 单个模型的Token使用统计
 */
data class ModelTokenStats(
    val modelId: String,           // 模型ID
    val modelName: String,         // 模型名称
    val configId: String,          // 配置ID
    val totalTokens: Int,          // 总Token数
    val promptTokens: Int,         // Prompt Token数
    val completionTokens: Int,     // Completion Token数
    val cost: Double,              // 成本
    val requestCount: Int          // 请求次数
)
