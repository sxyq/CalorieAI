package com.calorieai.app.service.backup

import androidx.room.withTransaction
import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.AIConfigRepository
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
    private val aiConfigRepository: AIConfigRepository,
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
                clearExistingData(includeAiConfigs = backupData.includeAIConfigs)
            }

            backupData.foodRecords.forEach { backup ->
                foodRecordRepository.addRecord(backup.toModel())
            }

            backupData.exerciseRecords.forEach { backup ->
                exerciseRecordRepository.addRecord(backup.toModel())
            }

            backupData.weightRecords.forEach { backup ->
                weightRecordRepository.insert(backup.toModel())
            }

            backupData.waterRecords.forEach { backup ->
                waterRecordRepository.insert(backup.toModel())
            }

            backupData.favoriteRecipes.forEach { backup ->
                favoriteRecipeRepository.upsert(backup.toModel())
            }

            backupData.pantryIngredients.forEach { backup ->
                pantryIngredientRepository.upsert(backup.toModel())
            }

            backupData.legacyRecipeGuides.forEach { backup ->
                mergeLegacyGuideToFavorite(backup)
            }

            backupData.recipePlans.forEach { backup ->
                recipePlanRepository.upsert(backup.toModel())
            }

            backupData.aiChatHistory.forEach { backup ->
                aiChatHistoryDao.insert(backup.toModel())
            }

            backupData.apiCallLogs.forEach { backup ->
                apiCallRecordDao.insertRecord(backup.toModel())
            }

            backupData.userSettings?.let { backup ->
                userSettingsRepository.saveSettings(backup.toModel())
            }

            if (backupData.includeAIConfigs && backupData.aiConfigs.isNotEmpty()) {
                backupData.aiConfigs.forEach { backup ->
                    aiConfigRepository.addConfig(backup.toModel())
                }
            }
        }

        backupData.toBackupResult(
            if (restoreMode == RestoreMode.OVERWRITE) "全量覆盖恢复成功" else "合并导入恢复成功"
        )
    }

    private suspend fun clearExistingData(includeAiConfigs: Boolean) {
        foodRecordRepository.deleteAll()
        exerciseRecordRepository.deleteAll()
        weightRecordRepository.deleteAll()
        waterRecordRepository.deleteAll()
        favoriteRecipeRepository.deleteAll()
        pantryIngredientRepository.deleteAll()
        recipePlanRepository.deleteAll()
        aiChatHistoryDao.deleteAll()
        apiCallRecordDao.deleteAllRecords()
        if (includeAiConfigs) {
            aiConfigRepository.deleteAll()
        }
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
