package com.calorieai.app.data.repository

import com.calorieai.app.data.local.dao.WeightRecordDao
import com.calorieai.app.data.model.WeightRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 体重记录仓库
 */
@Singleton
class WeightRecordRepository @Inject constructor(
    private val weightRecordDao: WeightRecordDao
) {
    fun getAllRecords(): Flow<List<WeightRecord>> = weightRecordDao.getAllRecords()
    
    suspend fun getAllRecordsOnce(): List<WeightRecord> = weightRecordDao.getAllRecordsOnce()
    
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<WeightRecord>> = 
        weightRecordDao.getRecordsBetween(startDate, endDate)
    
    fun getLatestRecord(): Flow<WeightRecord?> = weightRecordDao.getLatestRecord()
    
    suspend fun getLatestRecordOnce(): WeightRecord? = weightRecordDao.getLatestRecordOnce()
    
    fun getAllRecordsByDateAsc(): Flow<List<WeightRecord>> = weightRecordDao.getAllRecordsByDateAsc()
    
    suspend fun insert(record: WeightRecord): Long = weightRecordDao.insert(sanitizeWeightRecord(record))
    
    suspend fun update(record: WeightRecord) = weightRecordDao.update(sanitizeWeightRecord(record))
    
    suspend fun delete(record: WeightRecord) = weightRecordDao.delete(record)
    
    suspend fun deleteAll() = weightRecordDao.deleteAll()
    
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<WeightRecord> = 
        weightRecordDao.getRecordsBetweenSync(startDate, endDate)
    
    suspend fun deleteRecordById(id: Long) = weightRecordDao.deleteById(id)
    
    fun getRecentRecords(days: Int): Flow<List<WeightRecord>> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
        return weightRecordDao.getRecordsBetween(startTime, endTime)
    }
}
