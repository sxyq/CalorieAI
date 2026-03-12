package com.calorieai.app.data.repository

import com.calorieai.app.data.local.FoodRecordDao
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRecordRepository @Inject constructor(
    private val foodRecordDao: FoodRecordDao
) {
    fun getAllRecords(): Flow<List<FoodRecord>> = foodRecordDao.getAllRecords()
    
    fun getTodayRecords(): Flow<List<FoodRecord>> {
        val (startOfDay, endOfDay) = getTodayRange()
        return foodRecordDao.getRecordsBetween(startOfDay, endOfDay)
    }
    
    fun getRecordsByMealType(mealType: MealType): Flow<List<FoodRecord>> {
        val (startOfDay, endOfDay) = getTodayRange()
        return foodRecordDao.getRecordsByMealType(mealType, startOfDay, endOfDay)
    }
    
    fun getTodayTotalCalories(): Flow<Int?> {
        val (startOfDay, endOfDay) = getTodayRange()
        return foodRecordDao.getTotalCaloriesBetween(startOfDay, endOfDay)
    }
    
    suspend fun getRecordById(id: String): FoodRecord? = foodRecordDao.getRecordById(id)
    
    suspend fun addRecord(record: FoodRecord) = foodRecordDao.insertRecord(record)
    
    suspend fun updateRecord(record: FoodRecord) = foodRecordDao.updateRecord(record)
    
    suspend fun deleteRecord(record: FoodRecord) = foodRecordDao.deleteRecord(record)
    
    suspend fun deleteRecordById(id: String) = foodRecordDao.deleteRecordById(id)
    
    suspend fun toggleStarred(id: String, currentStatus: Boolean) {
        foodRecordDao.updateStarredStatus(id, !currentStatus)
    }
    
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        // Start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        return Pair(startOfDay, endOfDay)
    }
}
