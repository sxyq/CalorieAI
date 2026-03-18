package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import kotlinx.coroutines.flow.Flow

/**
 * 每日热量数据查询结果
 */
data class DailyCalorieData(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "totalCalories") val totalCalories: Int
)

@Dao
interface FoodRecordDao {

    @Query("SELECT * FROM food_records ORDER BY recordTime DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 100): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records ORDER BY recordTime DESC")
    fun getAllRecords(): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records ORDER BY recordTime DESC LIMIT :limit")
    suspend fun getRecentRecordsOnce(limit: Int = 100): List<FoodRecord>

    @Query("SELECT * FROM food_records ORDER BY recordTime DESC")
    suspend fun getAllRecordsOnce(): List<FoodRecord>

    @Query("SELECT * FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    suspend fun getRecordsBetweenOnce(startTime: Long, endTime: Long): List<FoodRecord>

    @Query("SELECT * FROM food_records WHERE mealType = :mealType AND recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    fun getRecordsByMealType(mealType: MealType, startTime: Long, endTime: Long): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE id = :id")
    suspend fun getRecordById(id: String): FoodRecord?

    @Query("SELECT SUM(totalCalories) FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime")
    fun getTotalCaloriesBetween(startTime: Long, endTime: Long): Flow<Int?>

    @Query("""
        SELECT
            date(recordTime / 1000, 'unixepoch', 'localtime') as date,
            SUM(totalCalories) as totalCalories
        FROM food_records
        WHERE recordTime BETWEEN :startTime AND :endTime
        GROUP BY date
    """)
    fun getCalorieDataByDateRange(startTime: Long, endTime: Long): Flow<List<DailyCalorieData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FoodRecord)

    @Update
    suspend fun updateRecord(record: FoodRecord)

    @Delete
    suspend fun deleteRecord(record: FoodRecord)

    @Query("DELETE FROM food_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("UPDATE food_records SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarredStatus(id: String, isStarred: Boolean)
}
