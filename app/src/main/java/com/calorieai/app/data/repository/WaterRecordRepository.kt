package com.calorieai.app.data.repository

import com.calorieai.app.data.local.dao.WaterRecordDao
import com.calorieai.app.data.model.WaterRecord
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRecordRepository @Inject constructor(
    private val waterRecordDao: WaterRecordDao
) {
    fun getAllRecords(): Flow<List<WaterRecord>> = waterRecordDao.getAllRecords()

    suspend fun getAllRecordsOnce(): List<WaterRecord> = waterRecordDao.getAllRecordsOnce()

    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<WaterRecord>> =
        waterRecordDao.getRecordsBetween(startDate, endDate)

    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<WaterRecord> =
        waterRecordDao.getRecordsBetweenSync(startDate, endDate)

    fun getRecordsByDate(date: Long): Flow<List<WaterRecord>> =
        waterRecordDao.getRecordsByDate(date)

    suspend fun getTotalAmountByDate(date: Long): Int =
        waterRecordDao.getTotalAmountByDate(date) ?: 0

    suspend fun getLatestRecord(): WaterRecord? = waterRecordDao.getLatestRecord()

    suspend fun insert(record: WaterRecord): Long = waterRecordDao.insert(sanitizeWaterRecord(record))

    suspend fun update(record: WaterRecord) = waterRecordDao.update(sanitizeWaterRecord(record))

    suspend fun delete(record: WaterRecord) = waterRecordDao.delete(record)

    suspend fun deleteById(id: Long) = waterRecordDao.deleteById(id)

    suspend fun deleteAll() = waterRecordDao.deleteAll()

    // 获取今日饮水量
    suspend fun getTodayTotalAmount(): Int {
        val today = getStartOfDay(System.currentTimeMillis())
        return getTotalAmountByDate(today)
    }

    // 获取本周饮水量
    suspend fun getWeeklyTotalAmount(): Int {
        val calendar = Calendar.getInstance()
        val endOfToday = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, -Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1)
        val startOfWeek = getStartOfDay(calendar.timeInMillis)
        
        val records = getRecordsBetweenSync(startOfWeek, endOfToday)
        return records.sumOf { it.amount }
    }

    // 获取本月饮水量
    suspend fun getMonthlyTotalAmount(): Int {
        val calendar = Calendar.getInstance()
        val endOfToday = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = getStartOfDay(calendar.timeInMillis)
        
        val records = getRecordsBetweenSync(startOfMonth, endOfToday)
        return records.sumOf { it.amount }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
