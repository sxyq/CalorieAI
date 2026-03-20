package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseRecordDao {
    @Query("SELECT * FROM exercise_records ORDER BY recordTime DESC")
    fun getAllRecords(): Flow<List<ExerciseRecord>>

    @Query("SELECT * FROM exercise_records WHERE recordTime >= :startTime AND recordTime < :endTime ORDER BY recordTime DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<ExerciseRecord>>

    @Query("SELECT * FROM exercise_records WHERE recordTime >= :startTime AND recordTime < :endTime ORDER BY recordTime DESC")
    suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<ExerciseRecord>

    @Query("SELECT * FROM exercise_records WHERE id = :id")
    suspend fun getRecordById(id: String): ExerciseRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ExerciseRecord)

    @Update
    suspend fun updateRecord(record: ExerciseRecord)

    @Delete
    suspend fun deleteRecord(record: ExerciseRecord)

    @Query("DELETE FROM exercise_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM exercise_records")
    suspend fun deleteAll()

    @Query("SELECT SUM(caloriesBurned) FROM exercise_records WHERE recordTime >= :startTime AND recordTime < :endTime")
    suspend fun getTotalCaloriesBurnedBetween(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(durationMinutes) FROM exercise_records WHERE recordTime >= :startTime AND recordTime < :endTime")
    suspend fun getTotalDurationBetween(startTime: Long, endTime: Long): Int?

    @Query("SELECT exerciseType, COUNT(*) as count FROM exercise_records GROUP BY exerciseType ORDER BY count DESC LIMIT 5")
    suspend fun getMostFrequentExerciseTypes(): List<ExerciseTypeCount>

    @Query("SELECT * FROM exercise_records ORDER BY recordTime DESC")
    suspend fun getAllRecordsOnce(): List<ExerciseRecord>
}

data class ExerciseTypeCount(
    val exerciseType: ExerciseType,
    val count: Int
)
