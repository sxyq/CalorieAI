package com.calorieai.app.data.local.dao

import androidx.room.*
import com.calorieai.app.data.model.WaterRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterRecordDao {
    @Query("SELECT * FROM water_records ORDER BY recordTime DESC")
    fun getAllRecords(): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records ORDER BY recordTime DESC")
    suspend fun getAllRecordsOnce(): List<WaterRecord>

    @Query("SELECT * FROM water_records WHERE recordDate >= :startDate AND recordDate <= :endDate ORDER BY recordTime DESC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records WHERE recordDate >= :startDate AND recordDate <= :endDate ORDER BY recordTime DESC")
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<WaterRecord>

    @Query("SELECT * FROM water_records WHERE recordDate = :date ORDER BY recordTime DESC")
    fun getRecordsByDate(date: Long): Flow<List<WaterRecord>>

    @Query("SELECT SUM(amount) FROM water_records WHERE recordDate = :date")
    suspend fun getTotalAmountByDate(date: Long): Int?

    @Query("SELECT * FROM water_records ORDER BY recordTime DESC LIMIT 1")
    suspend fun getLatestRecord(): WaterRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WaterRecord): Long

    @Update
    suspend fun update(record: WaterRecord)

    @Delete
    suspend fun delete(record: WaterRecord)

    @Query("DELETE FROM water_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM water_records")
    suspend fun deleteAll()
}
