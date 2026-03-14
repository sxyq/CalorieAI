package com.calorieai.app.service.ai

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI API调用限制管理器
 * 管理每日API调用次数限制
 */
@Singleton
class AIRateLimiter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "ai_rate_limiter"
        private const val KEY_CALL_COUNT = "call_count"
        private const val KEY_LAST_CALL_DATE = "last_call_date"
        private const val DEFAULT_DAILY_LIMIT = 50  // 默认每天10次
    }
    
    /**
     * 检查是否可以进行API调用
     * @param configId AI配置ID
     * @param dailyLimit 每日限制次数（默认10次）
     * @return Pair<是否可以调用, 剩余次数>
     */
    fun canMakeCall(configId: String, dailyLimit: Int = DEFAULT_DAILY_LIMIT): Pair<Boolean, Int> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val lastCallDate = prefs.getString("${KEY_LAST_CALL_DATE}_$configId", "")
        
        // 如果是新的一天，重置计数
        if (lastCallDate != today) {
            prefs.edit()
                .putString("${KEY_LAST_CALL_DATE}_$configId", today)
                .putInt("${KEY_CALL_COUNT}_$configId", 0)
                .apply()
            return Pair(true, dailyLimit)
        }
        
        val currentCount = prefs.getInt("${KEY_CALL_COUNT}_$configId", 0)
        val remaining = dailyLimit - currentCount
        
        return Pair(remaining > 0, remaining)
    }
    
    /**
     * 记录一次API调用
     * @param configId AI配置ID
     */
    fun recordCall(configId: String) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val lastCallDate = prefs.getString("${KEY_LAST_CALL_DATE}_$configId", "")
        
        // 如果是新的一天，重置计数
        val currentCount = if (lastCallDate != today) {
            prefs.edit().putString("${KEY_LAST_CALL_DATE}_$configId", today).apply()
            0
        } else {
            prefs.getInt("${KEY_CALL_COUNT}_$configId", 0)
        }
        
        prefs.edit()
            .putInt("${KEY_CALL_COUNT}_$configId", currentCount + 1)
            .apply()
    }
    
    /**
     * 获取今日已调用次数
     * @param configId AI配置ID
     * @return 今日已调用次数
     */
    fun getTodayCallCount(configId: String): Int {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val lastCallDate = prefs.getString("${KEY_LAST_CALL_DATE}_$configId", "")
        
        return if (lastCallDate != today) {
            0
        } else {
            prefs.getInt("${KEY_CALL_COUNT}_$configId", 0)
        }
    }
    
    /**
     * 获取剩余调用次数
     * @param configId AI配置ID
     * @param dailyLimit 每日限制次数
     * @return 剩余调用次数
     */
    fun getRemainingCalls(configId: String, dailyLimit: Int = DEFAULT_DAILY_LIMIT): Int {
        val (_, remaining) = canMakeCall(configId, dailyLimit)
        return remaining
    }
    
    /**
     * 重置调用计数（用于测试）
     * @param configId AI配置ID
     */
    fun resetCounter(configId: String) {
        prefs.edit()
            .remove("${KEY_CALL_COUNT}_$configId")
            .remove("${KEY_LAST_CALL_DATE}_$configId")
            .apply()
    }
}
