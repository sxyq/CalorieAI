package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodRecordDao {
    
    @Query("SELECT * FROM food_records ORDER BY recordTime DESC")
    fun getAllRecords(): Flow<List<FoodRecord>>
    
    @Query("SELECT * FROM food_records ORDER BY recordTime DESC")
    suspend fun getAllRecordsOnce(): List<FoodRecord>
    
    @Query("SELECT * FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<FoodRecord>>
    
    @Query("SELECT * FROM food_records WHERE mealType = :mealType AND recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    fun getRecordsByMealType(mealType: MealType, startTime: Long, endTime: Long): Flow<List<FoodRecord>>
    
    @Query("SELECT * FROM food_records WHERE id = :id")
    suspend fun getRecordById(id: String): FoodRecord?
    
    @Query("SELECT SUM(totalCalories) FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime")
    fun getTotalCaloriesBetween(startTime: Long, endTime: Long): Flow<Int?>
    
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
