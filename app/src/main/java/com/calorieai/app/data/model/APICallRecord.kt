package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * API调用记录
 * 记录每次AI API调用的详细信息，包括输入输出文本
 */
@Entity(tableName = "api_call_records")
data class APICallRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val configId: String,
    val configName: String,
    val modelId: String,
    val inputText: String,           // 用户输入文本
    val outputText: String,          // AI输出文本
    val promptTokens: Int = 0,       // 输入token数
    val completionTokens: Int = 0,   // 输出token数
    val totalTokens: Int = 0,        // 总token数
    val cost: Double = 0.0,          // 估算成本
    val duration: Long = 0,          // 调用耗时（毫秒）
    val isSuccess: Boolean = true,   // 是否成功
    val errorMessage: String? = null // 错误信息
)

/**
 * API调用记录统计
 */
data class APICallStats(
    val totalCalls: Int,
    val totalTokens: Int,
    val totalCost: Double,
    val avgDuration: Long,
    val todayCalls: Int,
    val todayCost: Double,
    val monthCalls: Int,
    val monthCost: Double
)
