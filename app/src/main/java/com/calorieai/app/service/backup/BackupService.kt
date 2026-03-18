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
 * 备份数据模型 - v3.2.0 版本
 * 支持扩展营养素字段和壁纸设置
 */
@Serializable
data class BackupData(
    val version: Int = 3,  // 版本号升级到3
    val backupDate: String,
    val appVersion: String = "3.2.0",
    val foodRecords: List<FoodRecordBackup>,
    val exerciseRecords: List<ExerciseRecordBackup>,
    val userSettings: UserSettingsBackup?,
    val aiConfigs: List<AIConfigBackup> = emptyList(),
    val weightRecords: List<WeightRecordBackup> = emptyList(),
    val waterRecords: List<WaterRecordBackup> = emptyList(),  // 新增饮水记录
    val includeAIConfigs: Boolean = true
)

@Serializable
data class AIConfigBackup(
    val id: String,
    val name: String,
    val icon: String,
    val iconType: String,
    val protocol: String,
    val apiUrl: String,
    val apiKey: String? = null,
    val hasApiKey: Boolean = false,
    val modelId: String,
    val isImageUnderstanding: Boolean,
    val isDefault: Boolean,
    val isPreset: Boolean = false
)

@Serializable
data class FoodRecordBackup(
    val id: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    // 基础营养素
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    // 扩展营养素（v3.2.0新增）
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val cholesterol: Float = 0f,
    val saturatedFat: Float = 0f,
    val calcium: Float = 0f,
    val iron: Float = 0f,
    val vitaminC: Float = 0f,
    val vitaminA: Float = 0f,
    val potassium: Float = 0f,
    // 其他字段
    val ingredients: List<IngredientBackup> = emptyList(),
    val mealType: String,
    val recordTime: Long,
    val iconUrl: String? = null,
    val iconLocalPath: String? = null,
    val isStarred: Boolean = false,
    val confidence: String = "MEDIUM",
    val notes: String? = null
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
    val notes: String? = null,
    val recordTime: Long
)

@Serializable
data class WeightRecordBackup(
    val id: Long,
    val weight: Float,
    val recordDate: Long,
    val note: String? = null
)

@Serializable
data class WaterRecordBackup(
    val id: Long,
    val amount: Int,
    val recordTime: Long,
    val recordDate: Long,
    val note: String? = null
)

@Serializable
data class UserSettingsBackup(
    val dailyCalorieGoal: Int,
    val userName: String? = null,
    val userGender: String? = null,
    val userAge: Int? = null,
    val userHeight: Float? = null,
    val userWeight: Float? = null,
    val activityLevel: String = "MODERATE",
    val dietaryPreference: String? = null,
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean? = null,
    val themeMode: String = "SYSTEM",
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: String = "MEDIUM",
    val enableAnimations: Boolean = true,
    val feedbackType: String = "NONE",
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val startupPage: String = "HOME",
    val enableQuickAdd: Boolean = true,
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = true,
    val enableAutoBackup: Boolean = false,
    val enableCloudSync: Boolean = false,
    val showAIWidget: Boolean = true,
    // 壁纸设置（v3.2.0新增）
    val wallpaperType: String = "GRADIENT",
    val wallpaperColor: String? = null,
    val wallpaperGradientStart: String? = null,
    val wallpaperGradientEnd: String? = null,
    val wallpaperImageUri: String? = null
)

/**
 * 备份服务 - v3.2.0
 * 支持完整的数据备份和恢复，包括扩展营养素和壁纸设置
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiConfigDao: com.calorieai.app.data.local.AIConfigDao,
    private val weightRecordRepository: com.calorieai.app.data.repository.WeightRecordRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 创建备份
     * @param includeAIConfigs 是否包含AI配置（默认true）
     */
    suspend fun createBackup(uri: Uri, includeAIConfigs: Boolean = true): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            // 收集所有数据
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val userSettings = userSettingsRepository.getSettings().first()
            val weightRecords = weightRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)

            // 收集AI配置
            val aiConfigs = if (includeAIConfigs) {
                aiConfigDao.getAllConfigs().first()
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
                weightRecords = weightRecords.map { it.toBackup() },
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

            Result.success(BackupResult(
                foodRecordCount = backupData.foodRecords.size,
                exerciseRecordCount = backupData.exerciseRecords.size,
                weightRecordCount = backupData.weightRecords.size,
                aiConfigCount = if (includeAIConfigs) backupData.aiConfigs.size else 0,
                message = "备份成功"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 恢复备份
     */
    suspend fun restoreBackup(uri: Uri): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            // 读取文件
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("无法读取文件"))

            // 解析JSON
            val backupData = json.decodeFromString<BackupData>(jsonString)

            // 版本兼容性检查
            if (backupData.version > 3) {
                return@withContext Result.failure(Exception("备份版本(${backupData.version})高于当前应用支持的版本，请升级应用后重试"))
            }

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
                    // 扩展营养素（v3.2.0+）
                    fiber = backup.fiber,
                    sugar = backup.sugar,
                    sodium = backup.sodium,
                    cholesterol = backup.cholesterol,
                    saturatedFat = backup.saturatedFat,
                    calcium = backup.calcium,
                    iron = backup.iron,
                    vitaminC = backup.vitaminC,
                    vitaminA = backup.vitaminA,
                    potassium = backup.potassium,
                    // 其他字段
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
                    confidence = try {
                        com.calorieai.app.data.model.ConfidenceLevel.valueOf(backup.confidence)
                    } catch (e: Exception) {
                        com.calorieai.app.data.model.ConfidenceLevel.MEDIUM
                    },
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

            // 恢复体重记录
            backupData.weightRecords.forEach { backup ->
                val record = com.calorieai.app.data.model.WeightRecord(
                    id = 0, // 自增ID
                    weight = backup.weight,
                    recordDate = backup.recordDate,
                    note = backup.note
                )
                weightRecordRepository.insert(record)
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
                    enableCloudSync = backup.enableCloudSync,
                    showAIWidget = backup.showAIWidget,
                    // 壁纸设置（v3.2.0+）
                    wallpaperType = backup.wallpaperType,
                    wallpaperColor = backup.wallpaperColor,
                    wallpaperGradientStart = backup.wallpaperGradientStart,
                    wallpaperGradientEnd = backup.wallpaperGradientEnd,
                    wallpaperImageUri = backup.wallpaperImageUri
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
                        apiKey = backup.apiKey ?: "",
                        modelId = backup.modelId,
                        isImageUnderstanding = backup.isImageUnderstanding,
                        isDefault = backup.isDefault,
                        isPreset = backup.isPreset
                    )
                    aiConfigDao.insertConfig(config)
                }
            }

            Result.success(BackupResult(
                foodRecordCount = backupData.foodRecords.size,
                exerciseRecordCount = backupData.exerciseRecords.size,
                weightRecordCount = backupData.weightRecords.size,
                aiConfigCount = if (backupData.includeAIConfigs) backupData.aiConfigs.size else 0,
                message = "恢复成功"
            ))
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
        // 扩展营养素
        fiber = fiber,
        sugar = sugar,
        sodium = sodium,
        cholesterol = cholesterol,
        saturatedFat = saturatedFat,
        calcium = calcium,
        iron = iron,
        vitaminC = vitaminC,
        vitaminA = vitaminA,
        potassium = potassium,
        // 其他字段
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

    private fun com.calorieai.app.data.model.WeightRecord.toBackup() = WeightRecordBackup(
        id = id,
        weight = weight,
        recordDate = recordDate,
        note = note
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
        enableCloudSync = enableCloudSync,
        showAIWidget = showAIWidget,
        // 壁纸设置
        wallpaperType = wallpaperType,
        wallpaperColor = wallpaperColor,
        wallpaperGradientStart = wallpaperGradientStart,
        wallpaperGradientEnd = wallpaperGradientEnd,
        wallpaperImageUri = wallpaperImageUri
    )

    private fun com.calorieai.app.data.model.AIConfig.toBackup() = AIConfigBackup(
        id = id,
        name = name,
        icon = icon,
        iconType = iconType.name,
        protocol = protocol.name,
        apiUrl = apiUrl,
        apiKey = null,
        hasApiKey = apiKey.isNotBlank(),
        modelId = modelId,
        isImageUnderstanding = isImageUnderstanding,
        isDefault = isDefault,
        isPreset = isPreset
    )
}

/**
 * 备份结果数据类
 */
data class BackupResult(
    val foodRecordCount: Int,
    val exerciseRecordCount: Int,
    val weightRecordCount: Int = 0,
    val aiConfigCount: Int = 0,
    val message: String
)
