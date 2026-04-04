package com.calorieai.app.service.backup

import androidx.room.withTransaction
import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.model.AIChatHistory
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupImportService @Inject constructor(
    private val appDatabase: AppDatabase,
    private val aiChatHistoryDao: AIChatHistoryDao,
    private val apiCallRecordDao: APICallRecordDao,
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiConfigDao: AIConfigDao,
    private val weightRecordRepository: com.calorieai.app.data.repository.WeightRecordRepository,
    private val waterRecordRepository: WaterRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val pantryIngredientRepository: PantryIngredientRepository,
    private val recipePlanRepository: RecipePlanRepository
) {
    suspend fun restoreBackupData(
        backupData: BackupData,
        restoreMode: RestoreMode
    ): BackupResult = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            if (restoreMode == RestoreMode.OVERWRITE) {
                foodRecordRepository.deleteAll()
                exerciseRecordRepository.deleteAll()
                weightRecordRepository.deleteAll()
                waterRecordRepository.deleteAll()
                favoriteRecipeRepository.deleteAll()
                pantryIngredientRepository.deleteAll()
                recipePlanRepository.deleteAll()
                aiChatHistoryDao.deleteAll()
                apiCallRecordDao.deleteAllRecords()
                if (backupData.includeAIConfigs) {
                    aiConfigDao.deleteAll()
                }
            }

            backupData.foodRecords.forEach { backup ->
                foodRecordRepository.addRecord(
                    FoodRecord(
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
                        confidence = runCatching {
                            com.calorieai.app.data.model.ConfidenceLevel.valueOf(backup.confidence)
                        }.getOrElse { com.calorieai.app.data.model.ConfidenceLevel.MEDIUM },
                        notes = backup.notes
                    )
                )
            }

            backupData.exerciseRecords.forEach { backup ->
                exerciseRecordRepository.addRecord(
                    ExerciseRecord(
                        id = backup.id,
                        exerciseType = com.calorieai.app.data.model.ExerciseType.valueOf(backup.exerciseType),
                        durationMinutes = backup.durationMinutes,
                        caloriesBurned = backup.caloriesBurned,
                        notes = backup.notes,
                        recordTime = backup.recordTime
                    )
                )
            }

            backupData.weightRecords.forEach { backup ->
                weightRecordRepository.insert(
                    com.calorieai.app.data.model.WeightRecord(
                        id = backup.id,
                        weight = backup.weight,
                        recordDate = backup.recordDate,
                        note = backup.note
                    )
                )
            }

            backupData.waterRecords.forEach { backup ->
                waterRecordRepository.insert(
                    com.calorieai.app.data.model.WaterRecord(
                        id = backup.id,
                        amount = backup.amount,
                        recordTime = backup.recordTime,
                        recordDate = backup.recordDate,
                        note = backup.note
                    )
                )
            }

            backupData.favoriteRecipes.forEach { backup ->
                favoriteRecipeRepository.upsert(
                    FavoriteRecipe(
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
                        recipeIngredientsText = backup.recipeIngredientsText,
                        recipeStepsText = backup.recipeStepsText,
                        recipeToolsText = backup.recipeToolsText,
                        recipeDifficulty = backup.recipeDifficulty,
                        recipeDurationMinutes = backup.recipeDurationMinutes,
                        recipeServings = backup.recipeServings,
                        recipeSourceType = backup.recipeSourceType,
                        recipeUpdatedAt = backup.recipeUpdatedAt,
                        createdAt = backup.createdAt,
                        lastUsedAt = backup.lastUsedAt,
                        useCount = backup.useCount
                    )
                )
            }

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

            backupData.legacyRecipeGuides.forEach { backup ->
                mergeLegacyGuideToFavorite(backup)
            }

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

            backupData.userSettings?.let { backup ->
                userSettingsRepository.saveSettings(
                    UserSettings(
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
                )
            }

            if (backupData.includeAIConfigs && backupData.aiConfigs.isNotEmpty()) {
                backupData.aiConfigs.forEach { backup ->
                    aiConfigDao.insertConfig(
                        com.calorieai.app.data.model.AIConfig(
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
                    )
                }
            }
        }

        backupData.toBackupResult(
            if (restoreMode == RestoreMode.OVERWRITE) "全量覆盖恢复成功" else "合并导入恢复成功"
        )
    }

    private suspend fun mergeLegacyGuideToFavorite(backup: RecipeGuideBackup) {
        val linkedFavorite = backup.linkedFavoriteId
            ?.takeIf { it.isNotBlank() }
            ?.let { linkedId -> favoriteRecipeRepository.getById(linkedId) }

        if (linkedFavorite != null) {
            favoriteRecipeRepository.upsert(
                linkedFavorite.copy(
                    recipeIngredientsText = backup.ingredientsText,
                    recipeStepsText = backup.stepsText,
                    recipeToolsText = backup.toolsText,
                    recipeDifficulty = backup.difficulty,
                    recipeDurationMinutes = backup.durationMinutes,
                    recipeServings = backup.servings,
                    recipeSourceType = backup.sourceType,
                    recipeUpdatedAt = backup.updatedAt
                )
            )
            return
        }

        val syntheticSourceRecordId = "guide:${backup.id}"
        val existingSynthetic = favoriteRecipeRepository.getBySourceRecordId(syntheticSourceRecordId)
        val base = existingSynthetic ?: FavoriteRecipe(
            id = "guide_${backup.id}",
            sourceRecordId = syntheticSourceRecordId,
            foodName = backup.name,
            userInput = backup.name,
            totalCalories = backup.calories,
            protein = backup.protein,
            carbs = backup.carbs,
            fat = backup.fat,
            createdAt = backup.createdAt
        )

        favoriteRecipeRepository.upsert(
            base.copy(
                foodName = backup.name,
                totalCalories = backup.calories,
                protein = backup.protein,
                carbs = backup.carbs,
                fat = backup.fat,
                recipeIngredientsText = backup.ingredientsText,
                recipeStepsText = backup.stepsText,
                recipeToolsText = backup.toolsText,
                recipeDifficulty = backup.difficulty,
                recipeDurationMinutes = backup.durationMinutes,
                recipeServings = backup.servings,
                recipeSourceType = backup.sourceType,
                recipeUpdatedAt = backup.updatedAt
            )
        )
    }
}
