package com.calorieai.app.data.repository

import com.calorieai.app.data.local.AITokenUsageDao
import com.calorieai.app.data.model.AITokenUsage
import com.calorieai.app.data.model.TokenUsageStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AITokenUsageRepository @Inject constructor(
    private val aiTokenUsageDao: AITokenUsageDao
) {
    /**
     * 记录Token使用情况
     */
    suspend fun recordTokenUsage(
        configId: String,
        configName: String,
        promptTokens: Int,
        completionTokens: Int,
        cost: Double
    ) {
        val tokenUsage = AITokenUsage(
            configId = configId,
            configName = configName,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens,
            cost = cost
        )
        aiTokenUsageDao.insertTokenUsage(tokenUsage)
    }

    /**
     * 获取Token使用统计
     */
    fun getTokenUsageStats(): Flow<TokenUsageStats?> = flow {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 今日统计
        val todayTokens = aiTokenUsageDao.getTotalTokensBetween(startOfToday, endOfToday) ?: 0
        val todayCost = aiTokenUsageDao.getTotalCostBetween(startOfToday, endOfToday) ?: 0.0

        // 本月统计
        val monthTokens = aiTokenUsageDao.getTotalTokensBetween(startOfMonth, endOfToday) ?: 0
        val monthCost = aiTokenUsageDao.getTotalCostBetween(startOfMonth, endOfToday) ?: 0.0

        // 总计统计（所有时间）
        val startOfTime = 0L
        val totalTokens = aiTokenUsageDao.getTotalTokensBetween(startOfTime, endOfToday) ?: 0
        val promptTokens = aiTokenUsageDao.getPromptTokensBetween(startOfTime, endOfToday) ?: 0
        val completionTokens = aiTokenUsageDao.getCompletionTokensBetween(startOfTime, endOfToday) ?: 0
        val totalCost = aiTokenUsageDao.getTotalCostBetween(startOfTime, endOfToday) ?: 0.0
        val requestCount = aiTokenUsageDao.getRequestCountBetween(startOfTime, endOfToday) ?: 0

        if (requestCount == 0) {
            emit(null)
        } else {
            emit(
                TokenUsageStats(
                    totalTokens = totalTokens,
                    promptTokens = promptTokens,
                    completionTokens = completionTokens,
                    totalCost = totalCost,
                    requestCount = requestCount,
                    todayTokens = todayTokens,
                    todayCost = todayCost,
                    monthTokens = monthTokens,
                    monthCost = monthCost
                )
            )
        }
    }

    /**
     * 获取指定时间段的Token使用记录
     */
    fun getTokenUsageBetween(startTime: Long, endTime: Long): Flow<List<AITokenUsage>> {
        return aiTokenUsageDao.getTokenUsageBetween(startTime, endTime)
    }

    /**
     * 清理旧数据（保留最近3个月）
     */
    suspend fun cleanupOldData() {
        val threeMonthsAgo = LocalDate.now()
            .minusMonths(3)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        aiTokenUsageDao.deleteTokenUsageBefore(threeMonthsAgo)
    }
}
