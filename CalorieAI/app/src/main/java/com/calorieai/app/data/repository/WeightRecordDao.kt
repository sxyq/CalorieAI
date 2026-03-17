package com.calorieai.app.data.repository

import androidx.room.*
import com.calorieai.app.data.model.WeightRecord
import kotlinx.coroutines.flow.Flow

/**
 * 体重记录数据访问对象
 */
@Dao
interface WeightRecordDao {
    
    @Query("SELECT * FROM weight_records ORDER BY recordDate DESC")
    fun getAllRecords(): Flow<List<WeightRecord>>
    
    @Query("SELECT * FROM weight_records ORDER BY recordDate DESC")
    suspend fun getAllRecordsOnce(): List<WeightRecord>
    
    @Query("SELECT * FROM weight_records ORDER BY recordDate ASC")
    fun getAllRecordsByDateAsc(): Flow<List<WeightRecord>>
    
    @Query("SELECT * FROM weight_records WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<WeightRecord>>
    
    @Query("SELECT * FROM weight_records ORDER BY recordDate DESC LIMIT 1")
    fun getLatestRecord(): Flow<WeightRecord?>
    
    @Query("SELECT * FROM weight_records ORDER BY recordDate DESC LIMIT 1")
    suspend fun getLatestRecordOnce(): WeightRecord?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeightRecord): Long
    
    @Update
    suspend fun update(record: WeightRecord)
    
    @Delete
    suspend fun delete(record: WeightRecord)
    
    @Query("DELETE FROM weight_records")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM weight_records WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate ASC")
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<WeightRecord>
    
    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
