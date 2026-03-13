package com.calorieai.app.service.backup

import android.content.Context
import android.net.Uri
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 备份数据模型
 */
@Serializable
data class BackupData(
    val version: Int = 2,
    val backupDate: String,
    val foodRecords: List<FoodRecordBackup>,
    val exerciseRecords: List<ExerciseRecordBackup>,
    val userSettings: UserSettingsBackup?,
    val aiConfigs: List<AIConfigBackup> = emptyList(),  // AI配置
    val includeAIConfigs: Boolean = true  // 是否包含AI配置
)

@Serializable
data class AIConfigBackup(
    val id: String,
    val name: String,
    val icon: String,
    val iconType: String,
    val protocol: String,
    val apiUrl: String,
    val apiKey: String,
    val modelId: String,
    val isImageUnderstanding: Boolean,
    val isDefault: Boolean
)

@Serializable
data class FoodRecordBackup(
    val id: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val ingredients: List<IngredientBackup>,
    val mealType: String,
    val recordTime: Long,
    val iconUrl: String?,
    val iconLocalPath: String?,
    val isStarred: Boolean,
    val confidence: String,
    val notes: String?
)

@Serializable
data class IngredientBackup(
    val name: String,
    val weight: String,
    val calories: Int
)

@Serializable
data class ExerciseRecordBackup(
    val id: String,
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String?,
    val recordTime: Long
)

@Serializable
data class UserSettingsBackup(
    val dailyCalorieGoal: Int,
    val userName: String?,
    val userGender: String?,
    val userAge: Int?,
    val userHeight: Float?,
    val userWeight: Float?,
    val activityLevel: String,
    val dietaryPreference: String?,
    val isNotificationEnabled: Boolean,
    val isDarkMode: Boolean?,
    val themeMode: String,
    val useDeadlinerStyle: Boolean,
    val hideDividers: Boolean,
    val fontSize: String,
    val enableAnimations: Boolean,
    val feedbackType: String,
    val enableVibration: Boolean,
    val enableSound: Boolean,
    val startupPage: String,
    val enableQuickAdd: Boolean,
    val enableGoalReminder: Boolean,
    val enableStreakReminder: Boolean,
    val enableAutoBackup: Boolean,
    val enableCloudSync: Boolean
)

/**
 * 备份服务
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiConfigRepository: com.calorieai.app.data.local.AIConfigDao
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * 创建备份
     * @param includeAIConfigs 是否包含AI配置（默认true）
     */
    suspend fun createBackup(uri: Uri, includeAIConfigs: Boolean = true): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 收集所有数据
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val userSettings = userSettingsRepository.getSettings().first()
            
            // 收集AI配置
            val aiConfigs = if (includeAIConfigs) {
                aiConfigRepository.getAllConfigs().first()
            } else {
                emptyList()
            }

            // 转换为备份格式
            val backupData = BackupData(
                backupDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                foodRecords = foodRecords.map { it.toBackup() },
                exerciseRecords = exerciseRecords.map { it.toBackup() },
                userSettings = userSettings?.toBackup(),
                aiConfigs = aiConfigs.map { it.toBackup() },
                includeAIConfigs = includeAIConfigs
            )

            // 序列化为JSON
            val jsonString = json.encodeToString(backupData)

            // 写入文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("无法创建输出流"))

            Result.success("备份成功：${backupData.foodRecords.size}条饮食记录，${backupData.exerciseRecords.size}条运动记录")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 恢复备份
     */
    suspend fun restoreBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 读取文件
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("无法读取文件"))

            // 解析JSON
            val backupData = json.decodeFromString<BackupData>(jsonString)

            // 恢复饮食记录
            backupData.foodRecords.forEach { backup ->
                val record = FoodRecord(
                    id = backup.id,
                    foodName = backup.foodName,
                    userInput = backup.userInput,
                    totalCalories = backup.totalCalories,
                    protein = backup.protein,
                    fat = backup.fat,
                    carbs = backup.carbs,
                    ingredients = backup.ingredients.map { 
                        com.calorieai.app.data.model.Ingredient(
                            name = it.name,
                            weight = it.weight,
                            calories = it.calories
                        )
                    },
                    mealType = com.calorieai.app.data.model.MealType.valueOf(backup.mealType),
                    recordTime = backup.recordTime,
                    iconUrl = backup.iconUrl,
                    iconLocalPath = backup.iconLocalPath,
                    isStarred = backup.isStarred,
                    confidence = com.calorieai.app.data.model.ConfidenceLevel.valueOf(backup.confidence),
                    notes = backup.notes
                )
                foodRecordRepository.addRecord(record)
            }

            // 恢复运动记录
            backupData.exerciseRecords.forEach { backup ->
                val record = ExerciseRecord(
                    id = backup.id,
                    exerciseType = com.calorieai.app.data.model.ExerciseType.valueOf(backup.exerciseType),
                    durationMinutes = backup.durationMinutes,
                    caloriesBurned = backup.caloriesBurned,
                    notes = backup.notes,
                    recordTime = backup.recordTime
                )
                exerciseRecordRepository.addRecord(record)
            }

            // 恢复用户设置
            backupData.userSettings?.let { backup ->
                val settings = UserSettings(
                    dailyCalorieGoal = backup.dailyCalorieGoal,
                    userName = backup.userName,
                    userGender = backup.userGender,
                    userAge = backup.userAge,
                    userHeight = backup.userHeight,
                    userWeight = backup.userWeight,
                    activityLevel = backup.activityLevel,
                    dietaryPreference = backup.dietaryPreference,
                    isNotificationEnabled = backup.isNotificationEnabled,
                    isDarkMode = backup.isDarkMode,
                    themeMode = backup.themeMode,
                    useDeadlinerStyle = backup.useDeadlinerStyle,
                    hideDividers = backup.hideDividers,
                    fontSize = backup.fontSize,
                    enableAnimations = backup.enableAnimations,
                    feedbackType = backup.feedbackType,
                    enableVibration = backup.enableVibration,
                    enableSound = backup.enableSound,
                    startupPage = backup.startupPage,
                    enableQuickAdd = backup.enableQuickAdd,
                    enableGoalReminder = backup.enableGoalReminder,
                    enableStreakReminder = backup.enableStreakReminder,
                    enableAutoBackup = backup.enableAutoBackup,
                    enableCloudSync = backup.enableCloudSync
                )
                userSettingsRepository.saveSettings(settings)
            }
            
            // 恢复AI配置（如果备份包含）
            if (backupData.includeAIConfigs && backupData.aiConfigs.isNotEmpty()) {
                backupData.aiConfigs.forEach { backup ->
                    val config = com.calorieai.app.data.model.AIConfig(
                        id = backup.id,
                        name = backup.name,
                        icon = backup.icon,
                        iconType = com.calorieai.app.data.model.IconType.valueOf(backup.iconType),
                        protocol = com.calorieai.app.data.model.AIProtocol.valueOf(backup.protocol),
                        apiUrl = backup.apiUrl,
                        apiKey = backup.apiKey,
                        modelId = backup.modelId,
                        isImageUnderstanding = backup.isImageUnderstanding,
                        isDefault = backup.isDefault
                    )
                    aiConfigRepository.insertConfig(config)
                }
            }

            Result.success("恢复成功：${backupData.foodRecords.size}条饮食记录，${backupData.exerciseRecords.size}条运动记录" + 
                if (backupData.includeAIConfigs && backupData.aiConfigs.isNotEmpty()) "，${backupData.aiConfigs.size}个AI配置" else "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取备份信息（不恢复）
     */
    suspend fun getBackupInfo(uri: Uri): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("无法读取文件"))

            val backupData = json.decodeFromString<BackupData>(jsonString)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 扩展函数：转换为备份格式
    private fun FoodRecord.toBackup() = FoodRecordBackup(
        id = id,
        foodName = foodName,
        userInput = userInput,
        totalCalories = totalCalories,
        protein = protein,
        fat = fat,
        carbs = carbs,
        ingredients = ingredients.map { 
            IngredientBackup(
                name = it.name,
                weight = it.weight,
                calories = it.calories
            )
        },
        mealType = mealType.name,
        recordTime = recordTime,
        iconUrl = iconUrl,
        iconLocalPath = iconLocalPath,
        isStarred = isStarred,
        confidence = confidence.name,
        notes = notes
    )

    private fun ExerciseRecord.toBackup() = ExerciseRecordBackup(
        id = id,
        exerciseType = exerciseType.name,
        durationMinutes = durationMinutes,
        caloriesBurned = caloriesBurned,
        notes = notes,
        recordTime = recordTime
    )

    private fun UserSettings.toBackup() = UserSettingsBackup(
        dailyCalorieGoal = dailyCalorieGoal,
        userName = userName,
        userGender = userGender,
        userAge = userAge,
        userHeight = userHeight,
        userWeight = userWeight,
        activityLevel = activityLevel,
        dietaryPreference = dietaryPreference,
        isNotificationEnabled = isNotificationEnabled,
        isDarkMode = isDarkMode,
        themeMode = themeMode,
        useDeadlinerStyle = useDeadlinerStyle,
        hideDividers = hideDividers,
        fontSize = fontSize,
        enableAnimations = enableAnimations,
        feedbackType = feedbackType,
        enableVibration = enableVibration,
        enableSound = enableSound,
        startupPage = startupPage,
        enableQuickAdd = enableQuickAdd,
        enableGoalReminder = enableGoalReminder,
        enableStreakReminder = enableStreakReminder,
        enableAutoBackup = enableAutoBackup,
        enableCloudSync = enableCloudSync
    )
    
    private fun com.calorieai.app.data.model.AIConfig.toBackup() = AIConfigBackup(
        id = id,
        name = name,
        icon = icon,
        iconType = iconType.name,
        protocol = protocol.name,
        apiUrl = apiUrl,
        apiKey = apiKey,
        modelId = modelId,
        isImageUnderstanding = isImageUnderstanding,
        isDefault = isDefault
    )
}
