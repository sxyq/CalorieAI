package com.calorieai.app.data.repository

import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.APICallStats
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APICallRecordRepository @Inject constructor(
    private val apiCallRecordDao: APICallRecordDao
) {
    @Volatile
    private var lastCleanupAt: Long = 0L
    private val cleanupMutex = Mutex()

    /**
     * 记录API调用
     */
    suspend fun recordCall(
        configId: String,
        configName: String,
        modelId: String,
        inputText: String,
        outputText: String,
        promptTokens: Int = 0,
        completionTokens: Int = 0,
        cost: Double = 0.0,
        duration: Long = 0,
        isSuccess: Boolean = true,
        errorMessage: String? = null
    ) {
        val now = System.currentTimeMillis()
        val record = APICallRecord(
            configId = configId,
            configName = configName,
            modelId = modelId,
            inputText = truncateLogText(inputText),
            outputText = truncateLogText(outputText),
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens,
            cost = cost,
            duration = duration,
            isSuccess = isSuccess,
            errorMessage = errorMessage?.take(MAX_ERROR_MESSAGE_CHARS),
            timestamp = now
        )
        apiCallRecordDao.insertRecord(record)
        cleanupOldDataIfNeeded(now)
    }

    /**
     * 获取所有调用记录
     */
    fun getAllRecords(): Flow<List<APICallRecord>> {
        return apiCallRecordDao.getAllRecords()
    }

    suspend fun getAllRecordsOnce(): List<APICallRecord> {
        return apiCallRecordDao.getAllRecordsOnce()
    }

    /**
     * 获取指定时间段的调用记录
     */
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<APICallRecord>> {
        return apiCallRecordDao.getRecordsBetween(startTime, endTime)
    }

    /**
     * 获取指定配置的调用记录
     */
    fun getRecordsByConfig(configId: String): Flow<List<APICallRecord>> {
        return apiCallRecordDao.getRecordsByConfig(configId)
    }

    /**
     * 获取API调用统计
     */
    suspend fun getStats(): APICallStats {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return apiCallRecordDao.getStats(startOfToday, endOfToday, startOfMonth, endOfToday)
    }

    /**
     * 删除指定记录
     */
    suspend fun deleteRecord(id: String) {
        apiCallRecordDao.deleteRecord(id)
    }

    /**
     * 清空所有记录
     */
    suspend fun deleteAllRecords() {
        apiCallRecordDao.deleteAllRecords()
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
        apiCallRecordDao.deleteRecordsBefore(threeMonthsAgo)
        lastCleanupAt = System.currentTimeMillis()
    }

    private suspend fun cleanupOldDataIfNeeded(now: Long) {
        if (now - lastCleanupAt < CLEANUP_INTERVAL_MS) return
        cleanupMutex.withLock {
            if (now - lastCleanupAt < CLEANUP_INTERVAL_MS) return
            val retentionStart = now - RETENTION_WINDOW_MS
            apiCallRecordDao.deleteRecordsBefore(retentionStart)
            lastCleanupAt = now
        }
    }

    private fun truncateLogText(value: String): String {
        return if (value.length <= MAX_LOG_TEXT_CHARS) {
            value
        } else {
            value.take(MAX_LOG_TEXT_CHARS)
        }
    }

    companion object {
        private const val MAX_LOG_TEXT_CHARS = 2048
        private const val MAX_ERROR_MESSAGE_CHARS = 512
        private const val CLEANUP_INTERVAL_MS = 24L * 60L * 60L * 1000L
        private const val RETENTION_WINDOW_MS = 90L * 24L * 60L * 60L * 1000L
    }
}
