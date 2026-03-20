package com.calorieai.app.data.repository

import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.APICallStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APICallRecordRepository @Inject constructor(
    private val apiCallRecordDao: APICallRecordDao
) {
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
        val record = APICallRecord(
            configId = configId,
            configName = configName,
            modelId = modelId,
            inputText = inputText,
            outputText = outputText,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens,
            cost = cost,
            duration = duration,
            isSuccess = isSuccess,
            errorMessage = errorMessage
        )
        apiCallRecordDao.insertRecord(record)
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
    }
}
