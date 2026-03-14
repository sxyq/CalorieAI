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
    
    /**
     * 获取指定日期范围的记录
     */
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<FoodRecord>> {
        return foodRecordDao.getRecordsBetween(startTime, endTime)
    }
    
    /**
     * 获取指定日期范围的总热量
     */
    fun getTotalCaloriesByDateRange(startTime: Long, endTime: Long): Flow<Int?> {
        return foodRecordDao.getTotalCaloriesBetween(startTime, endTime)
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
    
    /**
     * 获取所有记录（一次性）
     */
    suspend fun getAllRecordsOnce(): List<FoodRecord> = foodRecordDao.getAllRecordsOnce()
    
    /**
     * 获取指定日期范围的记录（同步）
     */
    suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<FoodRecord> {
        return foodRecordDao.getAllRecordsOnce().filter { record ->
            record.recordTime in startTime..endTime
        }
    }
    
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
