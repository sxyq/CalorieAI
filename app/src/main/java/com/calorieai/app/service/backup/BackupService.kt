package com.calorieai.app.service.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.model.AIChatHistory
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipeGuide
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipeGuideRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
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
    val version: Int = 6,  // 版本号升级到6
    val backupDate: String,
    val appVersion: String = "3.5.0",
    val foodRecords: List<FoodRecordBackup>,
    val exerciseRecords: List<ExerciseRecordBackup>,
    val userSettings: UserSettingsBackup?,
    val aiConfigs: List<AIConfigBackup> = emptyList(),
    val weightRecords: List<WeightRecordBackup> = emptyList(),
    val waterRecords: List<WaterRecordBackup> = emptyList(),  // 新增饮水记录
    val favoriteRecipes: List<FavoriteRecipeBackup> = emptyList(),
    val pantryIngredients: List<PantryIngredientBackup> = emptyList(),
    val recipeGuides: List<RecipeGuideBackup> = emptyList(),
    val recipePlans: List<RecipePlanBackup> = emptyList(),
    val aiChatHistory: List<AIChatHistoryBackup> = emptyList(),
    val apiCallLogs: List<APICallLogBackup> = emptyList(),
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
data class FavoriteRecipeBackup(
    val id: String,
    val sourceRecordId: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
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
    val createdAt: Long,
    val lastUsedAt: Long? = null,
    val useCount: Int = 0
)

@Serializable
data class PantryIngredientBackup(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val expiresAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RecipeGuideBackup(
    val id: String,
    val name: String,
    val ingredientsText: String,
    val stepsText: String,
    val toolsText: String,
    val difficulty: String,
    val durationMinutes: Int,
    val servings: Int,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sourceType: String,
    val linkedFavoriteId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RecipePlanBackup(
    val id: String,
    val title: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val menuText: String,
    val generatedByAI: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class AIChatHistoryBackup(
    val id: Long,
    val sessionId: String,
    val title: String,
    val messages: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int = 0,
    val isPinned: Boolean = false
)

@Serializable
data class APICallLogBackup(
    val id: String,
    val timestamp: Long,
    val configId: String,
    val configName: String,
    val modelId: String,
    val inputText: String,
    val outputText: String,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val cost: Double = 0.0,
    val duration: Long = 0,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
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
    val backgroundBehavior: String = "STANDARD",
    val startupPage: String = "HOME",
    val enableQuickAdd: Boolean = true,
    val enableLongPressHomeToAdd: Boolean = true,
    val enableLongPressOverviewToStats: Boolean = true,
    val enableLongPressMyToProfileEdit: Boolean = true,
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
    private val appDatabase: AppDatabase,
    private val aiChatHistoryDao: AIChatHistoryDao,
    private val apiCallRecordDao: APICallRecordDao,
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiConfigDao: com.calorieai.app.data.local.AIConfigDao,
    private val weightRecordRepository: com.calorieai.app.data.repository.WeightRecordRepository,
    private val waterRecordRepository: WaterRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val pantryIngredientRepository: PantryIngredientRepository,
    private val recipeGuideRepository: RecipeGuideRepository,
    private val recipePlanRepository: RecipePlanRepository,
    private val webDavBackupService: WebDavBackupService
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
            val waterRecords = waterRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val favoriteRecipes = favoriteRecipeRepository.getAllFavoritesOnce()
            val pantryIngredients = pantryIngredientRepository.getAllOnce()
            val recipeGuides = recipeGuideRepository.getAllOnce()
            val recipePlans = recipePlanRepository.getAllOnce()
            val aiChatHistory = aiChatHistoryDao.getAllHistoryOnce()
            val apiCallLogs = apiCallRecordDao.getAllRecordsOnce()

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
                waterRecords = waterRecords.map { it.toBackup() },
                favoriteRecipes = favoriteRecipes.map { it.toBackup() },
                pantryIngredients = pantryIngredients.map { it.toBackup() },
                recipeGuides = recipeGuides.map { it.toBackup() },
                recipePlans = recipePlans.map { it.toBackup() },
                aiChatHistory = aiChatHistory.map { it.toBackup() },
                apiCallLogs = apiCallLogs.map { it.toBackup() },
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
                waterRecordCount = backupData.waterRecords.size,
                favoriteRecipeCount = backupData.favoriteRecipes.size,
                pantryIngredientCount = backupData.pantryIngredients.size,
                recipeGuideCount = backupData.recipeGuides.size,
                recipePlanCount = backupData.recipePlans.size,
                aiChatHistoryCount = backupData.aiChatHistory.size,
                apiCallLogCount = backupData.apiCallLogs.size,
                aiConfigCount = if (includeAIConfigs) backupData.aiConfigs.size else 0,
                message = "备份成功"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBackupToWebDav(
        config: WebDavConfig,
        includeAIConfigs: Boolean = true
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val userSettings = userSettingsRepository.getSettings().first()
            val weightRecords = weightRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val waterRecords = waterRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
            val favoriteRecipes = favoriteRecipeRepository.getAllFavoritesOnce()
            val pantryIngredients = pantryIngredientRepository.getAllOnce()
            val recipeGuides = recipeGuideRepository.getAllOnce()
            val recipePlans = recipePlanRepository.getAllOnce()
            val aiChatHistory = aiChatHistoryDao.getAllHistoryOnce()
            val apiCallLogs = apiCallRecordDao.getAllRecordsOnce()
            val aiConfigs = if (includeAIConfigs) aiConfigDao.getAllConfigs().first() else emptyList()

            val backupData = BackupData(
                backupDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                foodRecords = foodRecords.map { it.toBackup() },
                exerciseRecords = exerciseRecords.map { it.toBackup() },
                userSettings = userSettings?.toBackup(),
                aiConfigs = aiConfigs.map { it.toBackup() },
                weightRecords = weightRecords.map { it.toBackup() },
                waterRecords = waterRecords.map { it.toBackup() },
                favoriteRecipes = favoriteRecipes.map { it.toBackup() },
                pantryIngredients = pantryIngredients.map { it.toBackup() },
                recipeGuides = recipeGuides.map { it.toBackup() },
                recipePlans = recipePlans.map { it.toBackup() },
                aiChatHistory = aiChatHistory.map { it.toBackup() },
                apiCallLogs = apiCallLogs.map { it.toBackup() },
                includeAIConfigs = includeAIConfigs
            )

            val jsonString = json.encodeToString(backupData)
            webDavBackupService.uploadJson(config, jsonString).getOrThrow()

            Result.success(
                BackupResult(
                    foodRecordCount = backupData.foodRecords.size,
                    exerciseRecordCount = backupData.exerciseRecords.size,
                    weightRecordCount = backupData.weightRecords.size,
                    waterRecordCount = backupData.waterRecords.size,
                    favoriteRecipeCount = backupData.favoriteRecipes.size,
                    pantryIngredientCount = backupData.pantryIngredients.size,
                    recipeGuideCount = backupData.recipeGuides.size,
                    recipePlanCount = backupData.recipePlans.size,
                    aiChatHistoryCount = backupData.aiChatHistory.size,
                    apiCallLogCount = backupData.apiCallLogs.size,
                    aiConfigCount = if (includeAIConfigs) backupData.aiConfigs.size else 0,
                    message = "云备份上传成功"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 恢复备份
     */
    suspend fun restoreBackup(
        uri: Uri,
        restoreMode: RestoreMode = RestoreMode.MERGE
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
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
            if (backupData.version > 6) {
                return@withContext Result.failure(Exception("备份版本(${backupData.version})高于当前应用支持的版本，请升级应用后重试"))
            }

            appDatabase.withTransaction {
                if (restoreMode == RestoreMode.OVERWRITE) {
                    foodRecordRepository.deleteAll()
                    exerciseRecordRepository.deleteAll()
                    weightRecordRepository.deleteAll()
                    waterRecordRepository.deleteAll()
                    favoriteRecipeRepository.deleteAll()
                    pantryIngredientRepository.deleteAll()
                    recipeGuideRepository.deleteAll()
                    recipePlanRepository.deleteAll()
                    aiChatHistoryDao.deleteAll()
                    apiCallRecordDao.deleteAllRecords()
                    if (backupData.includeAIConfigs) {
                        aiConfigDao.deleteAll()
                    }
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
                        id = backup.id,
                        weight = backup.weight,
                        recordDate = backup.recordDate,
                        note = backup.note
                    )
                    weightRecordRepository.insert(record)
                }

                // 恢复饮水记录
                backupData.waterRecords.forEach { backup ->
                    val record = com.calorieai.app.data.model.WaterRecord(
                        id = backup.id,
                        amount = backup.amount,
                        recordTime = backup.recordTime,
                        recordDate = backup.recordDate,
                        note = backup.note
                    )
                    waterRecordRepository.insert(record)
                }

                // 恢复收藏菜谱
                backupData.favoriteRecipes.forEach { backup ->
                    val recipe = FavoriteRecipe(
                        id = backup.id,
                        sourceRecordId = backup.sourceRecordId,
                        foodName = backup.foodName,
                        userInput = backup.userInput,
                        totalCalories = backup.totalCalories,
                        protein = backup.protein,
                        carbs = backup.carbs,
                        fat = backup.fat,
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
                        createdAt = backup.createdAt,
                        lastUsedAt = backup.lastUsedAt,
                        useCount = backup.useCount
                    )
                    favoriteRecipeRepository.upsert(recipe)
                }

                // 恢复食材库存
                backupData.pantryIngredients.forEach { backup ->
                    pantryIngredientRepository.upsert(
                        PantryIngredient(
                            id = backup.id,
                            name = backup.name,
                            quantity = backup.quantity,
                            unit = backup.unit,
                            expiresAt = backup.expiresAt,
                            notes = backup.notes,
                            createdAt = backup.createdAt,
                            updatedAt = backup.updatedAt
                        )
                    )
                }

                // 恢复标准化菜谱
                backupData.recipeGuides.forEach { backup ->
                    recipeGuideRepository.upsert(
                        RecipeGuide(
                            id = backup.id,
                            name = backup.name,
                            ingredientsText = backup.ingredientsText,
                            stepsText = backup.stepsText,
                            toolsText = backup.toolsText,
                            difficulty = backup.difficulty,
                            durationMinutes = backup.durationMinutes,
                            servings = backup.servings,
                            calories = backup.calories,
                            protein = backup.protein,
                            carbs = backup.carbs,
                            fat = backup.fat,
                            sourceType = backup.sourceType,
                            linkedFavoriteId = backup.linkedFavoriteId,
                            createdAt = backup.createdAt,
                            updatedAt = backup.updatedAt
                        )
                    )
                }

                // 恢复菜单计划
                backupData.recipePlans.forEach { backup ->
                    recipePlanRepository.upsert(
                        RecipePlan(
                            id = backup.id,
                            title = backup.title,
                            startDateEpochDay = backup.startDateEpochDay,
                            endDateEpochDay = backup.endDateEpochDay,
                            menuText = backup.menuText,
                            generatedByAI = backup.generatedByAI,
                            createdAt = backup.createdAt,
                            updatedAt = backup.updatedAt
                        )
                    )
                }

                // 恢复 AI 对话历史
                backupData.aiChatHistory.forEach { backup ->
                    aiChatHistoryDao.insert(
                        AIChatHistory(
                            id = backup.id,
                            sessionId = backup.sessionId,
                            title = backup.title,
                            messages = backup.messages,
                            createdAt = backup.createdAt,
                            updatedAt = backup.updatedAt,
                            messageCount = backup.messageCount,
                            isPinned = backup.isPinned
                        )
                    )
                }

                // 恢复 API 调用日志
                backupData.apiCallLogs.forEach { backup ->
                    apiCallRecordDao.insertRecord(
                        APICallRecord(
                            id = backup.id,
                            timestamp = backup.timestamp,
                            configId = backup.configId,
                            configName = backup.configName,
                            modelId = backup.modelId,
                            inputText = backup.inputText,
                            outputText = backup.outputText,
                            promptTokens = backup.promptTokens,
                            completionTokens = backup.completionTokens,
                            totalTokens = backup.totalTokens,
                            cost = backup.cost,
                            duration = backup.duration,
                            isSuccess = backup.isSuccess,
                            errorMessage = backup.errorMessage
                        )
                    )
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
                        backgroundBehavior = backup.backgroundBehavior,
                        startupPage = backup.startupPage,
                        enableQuickAdd = backup.enableQuickAdd,
                        enableLongPressHomeToAdd = backup.enableLongPressHomeToAdd,
                        enableLongPressOverviewToStats = backup.enableLongPressOverviewToStats,
                        enableLongPressMyToProfileEdit = backup.enableLongPressMyToProfileEdit,
                        enableGoalReminder = backup.enableGoalReminder,
                        enableStreakReminder = backup.enableStreakReminder,
                        enableAutoBackup = backup.enableAutoBackup,
                        enableCloudSync = backup.enableCloudSync,
                        showAIWidget = backup.showAIWidget,
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
            }

            Result.success(BackupResult(
                foodRecordCount = backupData.foodRecords.size,
                exerciseRecordCount = backupData.exerciseRecords.size,
                weightRecordCount = backupData.weightRecords.size,
                waterRecordCount = backupData.waterRecords.size,
                favoriteRecipeCount = backupData.favoriteRecipes.size,
                pantryIngredientCount = backupData.pantryIngredients.size,
                recipeGuideCount = backupData.recipeGuides.size,
                recipePlanCount = backupData.recipePlans.size,
                aiChatHistoryCount = backupData.aiChatHistory.size,
                apiCallLogCount = backupData.apiCallLogs.size,
                aiConfigCount = if (backupData.includeAIConfigs) backupData.aiConfigs.size else 0,
                message = if (restoreMode == RestoreMode.OVERWRITE) "全量覆盖恢复成功" else "合并导入恢复成功"
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

    suspend fun getBackupInfoFromWebDav(config: WebDavConfig): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val jsonString = webDavBackupService.downloadJson(config).getOrThrow()
            val backupData = json.decodeFromString<BackupData>(jsonString)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackupFromWebDav(
        config: WebDavConfig,
        restoreMode: RestoreMode = RestoreMode.MERGE
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            val jsonString = webDavBackupService.downloadJson(config).getOrThrow()
            val tempFile = File(context.cacheDir, "cloud_restore_backup.json")
            tempFile.writeText(jsonString)
            val result = restoreBackup(Uri.fromFile(tempFile), restoreMode)
            tempFile.delete()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buildRestorePreview(
        backupData: BackupData,
        mode: RestoreMode
    ): RestorePreview = withContext(Dispatchers.IO) {
        val currentFood = foodRecordRepository.getAllRecordsOnce()
        val currentExercise = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val currentWeight = weightRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val currentWater = waterRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val currentFavorites = favoriteRecipeRepository.getAllFavoritesOnce()
        val currentPantry = pantryIngredientRepository.getAllOnce()
        val currentGuides = recipeGuideRepository.getAllOnce()
        val currentPlans = recipePlanRepository.getAllOnce()
        val currentHistory = aiChatHistoryDao.getAllHistoryOnce()
        val currentApiLogs = apiCallRecordDao.getAllRecordsOnce()
        val currentSettings = if (userSettingsRepository.getSettingsOnce() != null) 1 else 0
        val currentAiConfigs = if (backupData.includeAIConfigs) aiConfigDao.getAllConfigs().first() else emptyList()

        fun mergeItem(
            label: String,
            backupIds: Set<String>,
            currentIds: Set<String>
        ): RestorePreviewItem {
            val addCount = (backupIds - currentIds).size
            val updateCount = (backupIds intersect currentIds).size
            return RestorePreviewItem(
                label = label,
                backupCount = backupIds.size,
                existingCount = currentIds.size,
                addCount = addCount,
                updateCount = updateCount,
                clearCount = 0
            )
        }

        fun overwriteItem(
            label: String,
            backupCount: Int,
            existingCount: Int
        ): RestorePreviewItem {
            return RestorePreviewItem(
                label = label,
                backupCount = backupCount,
                existingCount = existingCount,
                addCount = backupCount,
                updateCount = 0,
                clearCount = existingCount
            )
        }

        val items = if (mode == RestoreMode.OVERWRITE) {
            listOf(
                overwriteItem("饮食记录", backupData.foodRecords.size, currentFood.size),
                overwriteItem("运动记录", backupData.exerciseRecords.size, currentExercise.size),
                overwriteItem("体重记录", backupData.weightRecords.size, currentWeight.size),
                overwriteItem("饮水记录", backupData.waterRecords.size, currentWater.size),
                overwriteItem("收藏菜谱", backupData.favoriteRecipes.size, currentFavorites.size),
                overwriteItem("食材库存", backupData.pantryIngredients.size, currentPantry.size),
                overwriteItem("标准化菜谱", backupData.recipeGuides.size, currentGuides.size),
                overwriteItem("菜单计划", backupData.recipePlans.size, currentPlans.size),
                overwriteItem("AI对话历史", backupData.aiChatHistory.size, currentHistory.size),
                overwriteItem("API调用日志", backupData.apiCallLogs.size, currentApiLogs.size),
                overwriteItem("用户设置", if (backupData.userSettings != null) 1 else 0, currentSettings),
                overwriteItem("AI配置", if (backupData.includeAIConfigs) backupData.aiConfigs.size else 0, currentAiConfigs.size)
            )
        } else {
            val settingsBackupCount = if (backupData.userSettings != null) 1 else 0
            val settingsAdd = if (settingsBackupCount == 1 && currentSettings == 0) 1 else 0
            val settingsUpdate = if (settingsBackupCount == 1 && currentSettings == 1) 1 else 0
            val settingsItem = RestorePreviewItem(
                label = "用户设置",
                backupCount = settingsBackupCount,
                existingCount = currentSettings,
                addCount = settingsAdd,
                updateCount = settingsUpdate,
                clearCount = 0
            )

            listOf(
                mergeItem("饮食记录", backupData.foodRecords.map { it.id }.toSet(), currentFood.map { it.id }.toSet()),
                mergeItem("运动记录", backupData.exerciseRecords.map { it.id }.toSet(), currentExercise.map { it.id }.toSet()),
                mergeItem("体重记录", backupData.weightRecords.map { it.id.toString() }.toSet(), currentWeight.map { it.id.toString() }.toSet()),
                mergeItem("饮水记录", backupData.waterRecords.map { it.id.toString() }.toSet(), currentWater.map { it.id.toString() }.toSet()),
                mergeItem("收藏菜谱", backupData.favoriteRecipes.map { it.id }.toSet(), currentFavorites.map { it.id }.toSet()),
                mergeItem("食材库存", backupData.pantryIngredients.map { it.id }.toSet(), currentPantry.map { it.id }.toSet()),
                mergeItem("标准化菜谱", backupData.recipeGuides.map { it.id }.toSet(), currentGuides.map { it.id }.toSet()),
                mergeItem("菜单计划", backupData.recipePlans.map { it.id }.toSet(), currentPlans.map { it.id }.toSet()),
                mergeItem("AI对话历史", backupData.aiChatHistory.map { it.id.toString() }.toSet(), currentHistory.map { it.id.toString() }.toSet()),
                mergeItem("API调用日志", backupData.apiCallLogs.map { it.id }.toSet(), currentApiLogs.map { it.id }.toSet()),
                settingsItem,
                mergeItem(
                    "AI配置",
                    if (backupData.includeAIConfigs) backupData.aiConfigs.map { it.id }.toSet() else emptySet(),
                    currentAiConfigs.map { it.id }.toSet()
                )
            )
        }

        RestorePreview(mode = mode, items = items)
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

    private fun com.calorieai.app.data.model.WaterRecord.toBackup() = WaterRecordBackup(
        id = id,
        amount = amount,
        recordTime = recordTime,
        recordDate = recordDate,
        note = note
    )

    private fun FavoriteRecipe.toBackup() = FavoriteRecipeBackup(
        id = id,
        sourceRecordId = sourceRecordId,
        foodName = foodName,
        userInput = userInput,
        totalCalories = totalCalories,
        protein = protein,
        carbs = carbs,
        fat = fat,
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
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        useCount = useCount
    )

    private fun PantryIngredient.toBackup() = PantryIngredientBackup(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        expiresAt = expiresAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun RecipeGuide.toBackup() = RecipeGuideBackup(
        id = id,
        name = name,
        ingredientsText = ingredientsText,
        stepsText = stepsText,
        toolsText = toolsText,
        difficulty = difficulty,
        durationMinutes = durationMinutes,
        servings = servings,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        sourceType = sourceType,
        linkedFavoriteId = linkedFavoriteId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun RecipePlan.toBackup() = RecipePlanBackup(
        id = id,
        title = title,
        startDateEpochDay = startDateEpochDay,
        endDateEpochDay = endDateEpochDay,
        menuText = menuText,
        generatedByAI = generatedByAI,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun AIChatHistory.toBackup() = AIChatHistoryBackup(
        id = id,
        sessionId = sessionId,
        title = title,
        messages = messages,
        createdAt = createdAt,
        updatedAt = updatedAt,
        messageCount = messageCount,
        isPinned = isPinned
    )

    private fun APICallRecord.toBackup() = APICallLogBackup(
        id = id,
        timestamp = timestamp,
        configId = configId,
        configName = configName,
        modelId = modelId,
        inputText = inputText,
        outputText = outputText,
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        totalTokens = totalTokens,
        cost = cost,
        duration = duration,
        isSuccess = isSuccess,
        errorMessage = errorMessage
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
        backgroundBehavior = backgroundBehavior,
        startupPage = startupPage,
        enableQuickAdd = enableQuickAdd,
        enableLongPressHomeToAdd = enableLongPressHomeToAdd,
        enableLongPressOverviewToStats = enableLongPressOverviewToStats,
        enableLongPressMyToProfileEdit = enableLongPressMyToProfileEdit,
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
    val waterRecordCount: Int = 0,
    val favoriteRecipeCount: Int = 0,
    val pantryIngredientCount: Int = 0,
    val recipeGuideCount: Int = 0,
    val recipePlanCount: Int = 0,
    val aiChatHistoryCount: Int = 0,
    val apiCallLogCount: Int = 0,
    val aiConfigCount: Int = 0,
    val message: String
)

enum class RestoreMode {
    OVERWRITE, // 全量覆盖
    MERGE      // 合并导入
}

data class RestorePreviewItem(
    val label: String,
    val backupCount: Int,
    val existingCount: Int,
    val addCount: Int,
    val updateCount: Int,
    val clearCount: Int
)

data class RestorePreview(
    val mode: RestoreMode,
    val items: List<RestorePreviewItem>
)
