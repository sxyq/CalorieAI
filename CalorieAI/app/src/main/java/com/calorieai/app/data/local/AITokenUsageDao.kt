package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.AITokenUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface AITokenUsageDao {
    @Insert
    suspend fun insertTokenUsage(tokenUsage: AITokenUsage)

    @Query("SELECT * FROM ai_token_usage ORDER BY timestamp DESC")
    fun getAllTokenUsage(): Flow<List<AITokenUsage>>

    @Query("SELECT * FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getTokenUsageBetween(startTime: Long, endTime: Long): Flow<List<AITokenUsage>>

    @Query("SELECT * FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    suspend fun getTokenUsageBetweenSync(startTime: Long, endTime: Long): List<AITokenUsage>

    @Query("SELECT SUM(totalTokens) FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getTotalTokensBetween(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(promptTokens) FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getPromptTokensBetween(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(completionTokens) FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getCompletionTokensBetween(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(cost) FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getTotalCostBetween(startTime: Long, endTime: Long): Double?

    @Query("SELECT COUNT(*) FROM ai_token_usage WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getRequestCountBetween(startTime: Long, endTime: Long): Int?

    @Query("DELETE FROM ai_token_usage WHERE timestamp < :beforeTime")
    suspend fun deleteTokenUsageBefore(beforeTime: Long)
}
