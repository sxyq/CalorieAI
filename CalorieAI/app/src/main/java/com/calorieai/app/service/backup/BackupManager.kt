package com.calorieai.app.service.backup

import android.content.Context
import android.net.Uri
import com.calorieai.app.data.local.FoodRecordDao
import com.calorieai.app.data.model.FoodRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRecordDao: FoodRecordDao
) {

    private val gson = Gson()

    suspend fun exportToJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val records = foodRecordDao.getAllRecords().first()
            
            val backupData = BackupData(
                version = 1,
                exportTime = System.currentTimeMillis(),
                records = records
            )
            
            val json = gson.toJson(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            
            Result.success(records.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("无法读取文件"))
            
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            // 导入记录（替换现有记录）
            backupData.records.forEach { record ->
                foodRecordDao.insertRecord(record)
            }
            
            Result.success(backupData.records.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class BackupData(
        val version: Int,
        val exportTime: Long,
        val records: List<FoodRecord>
    )
}
