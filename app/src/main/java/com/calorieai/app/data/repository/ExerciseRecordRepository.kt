package com.calorieai.app.data.repository

import com.calorieai.app.data.local.ExerciseRecordDao
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRecordRepository @Inject constructor(
    private val exerciseRecordDao: ExerciseRecordDao
) {
    fun getAllRecords(): Flow<List<ExerciseRecord>> {
        return exerciseRecordDao.getAllRecords()
    }

    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<ExerciseRecord>> {
        return exerciseRecordDao.getRecordsBetween(startTime, endTime)
    }

    suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<ExerciseRecord> {
        return exerciseRecordDao.getRecordsBetweenSync(startTime, endTime)
    }

    suspend fun getRecordById(id: String): ExerciseRecord? {
        return exerciseRecordDao.getRecordById(id)
    }

    suspend fun addRecord(record: ExerciseRecord) {
        exerciseRecordDao.insertRecord(sanitizeExerciseRecord(record))
    }

    suspend fun updateRecord(record: ExerciseRecord) {
        exerciseRecordDao.updateRecord(sanitizeExerciseRecord(record))
    }

    suspend fun deleteRecord(record: ExerciseRecord) {
        exerciseRecordDao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: String) {
        exerciseRecordDao.deleteRecordById(id)
    }

    suspend fun deleteAll() {
        exerciseRecordDao.deleteAll()
    }

    suspend fun getTotalCaloriesBurnedBetween(startTime: Long, endTime: Long): Int {
        return exerciseRecordDao.getTotalCaloriesBurnedBetween(startTime, endTime) ?: 0
    }

    suspend fun getTotalDurationBetween(startTime: Long, endTime: Long): Int {
        return exerciseRecordDao.getTotalDurationBetween(startTime, endTime) ?: 0
    }

    suspend fun getMostFrequentExerciseTypes(): List<Pair<ExerciseType, Int>> {
        return exerciseRecordDao.getMostFrequentExerciseTypes().map {
            Pair(it.exerciseType, it.count)
        }
    }

    suspend fun getAllRecordsOnce(): List<ExerciseRecord> {
        return exerciseRecordDao.getAllRecordsOnce()
    }
}
