package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.APICallStats
import kotlinx.coroutines.flow.Flow

@Dao
interface APICallRecordDao {
    @Insert
    suspend fun insertRecord(record: APICallRecord)

    @Query("SELECT * FROM api_call_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<APICallRecord>>

    @Query("SELECT * FROM api_call_records WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<APICallRecord>>

    @Query("SELECT * FROM api_call_records WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<APICallRecord>

    @Query("SELECT * FROM api_call_records WHERE configId = :configId ORDER BY timestamp DESC")
    fun getRecordsByConfig(configId: String): Flow<List<APICallRecord>>

    @Query("SELECT * FROM api_call_records WHERE timestamp >= :startTime AND timestamp < :endTime AND isSuccess = 1")
    suspend fun getSuccessfulRecordsBetween(startTime: Long, endTime: Long): List<APICallRecord>

    @Query("SELECT COUNT(*) FROM api_call_records")
    suspend fun getTotalCalls(): Int

    @Query("SELECT COUNT(*) FROM api_call_records WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getCallsBetween(startTime: Long, endTime: Long): Int

    @Query("SELECT SUM(totalTokens) FROM api_call_records WHERE isSuccess = 1")
    suspend fun getTotalTokens(): Int?

    @Query("SELECT SUM(cost) FROM api_call_records WHERE isSuccess = 1")
    suspend fun getTotalCost(): Double?

    @Query("SELECT AVG(duration) FROM api_call_records WHERE isSuccess = 1")
    suspend fun getAvgDuration(): Long?

    @Query("DELETE FROM api_call_records WHERE timestamp < :beforeTime")
    suspend fun deleteRecordsBefore(beforeTime: Long)

    @Query("DELETE FROM api_call_records WHERE id = :id")
    suspend fun deleteRecord(id: String)

    @Query("DELETE FROM api_call_records")
    suspend fun deleteAllRecords()

    @Transaction
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM api_call_records) as totalCalls,
            (SELECT COALESCE(SUM(totalTokens), 0) FROM api_call_records WHERE isSuccess = 1) as totalTokens,
            (SELECT COALESCE(SUM(cost), 0) FROM api_call_records WHERE isSuccess = 1) as totalCost,
            (SELECT COALESCE(AVG(duration), 0) FROM api_call_records WHERE isSuccess = 1) as avgDuration,
            (SELECT COUNT(*) FROM api_call_records WHERE timestamp >= :todayStart AND timestamp < :todayEnd) as todayCalls,
            (SELECT COALESCE(SUM(cost), 0) FROM api_call_records WHERE timestamp >= :todayStart AND timestamp < :todayEnd AND isSuccess = 1) as todayCost,
            (SELECT COUNT(*) FROM api_call_records WHERE timestamp >= :monthStart AND timestamp < :monthEnd) as monthCalls,
            (SELECT COALESCE(SUM(cost), 0) FROM api_call_records WHERE timestamp >= :monthStart AND timestamp < :monthEnd AND isSuccess = 1) as monthCost
    """)
    suspend fun getStats(todayStart: Long, todayEnd: Long, monthStart: Long, monthEnd: Long): APICallStats
}
