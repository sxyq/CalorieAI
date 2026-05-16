package com.calorieai.app.service.backup

import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.model.AIChatHistory
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

internal data class BackupCurrentSnapshot(
    val foodRecords: List<FoodRecord>,
    val exerciseRecords: List<ExerciseRecord>,
    val userSettings: UserSettings?,
    val aiConfigs: List<AIConfig>,
    val weightRecords: List<WeightRecord>,
    val waterRecords: List<WaterRecord>,
    val favoriteRecipes: List<FavoriteRecipe>,
    val pantryIngredients: List<PantryIngredient>,
    val recipePlans: List<RecipePlan>,
    val aiChatHistory: List<AIChatHistory>,
    val apiCallLogs: List<APICallRecord>
) {
    val settingsCount: Int
        get() = if (userSettings != null) 1 else 0
}

@Singleton
class BackupSnapshotProvider @Inject constructor(
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
    internal suspend fun collect(includeAiConfigs: Boolean): BackupCurrentSnapshot {
        return BackupCurrentSnapshot(
            foodRecords = foodRecordRepository.getAllRecordsOnce(),
            exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE),
            userSettings = userSettingsRepository.getSettings().first(),
            aiConfigs = if (includeAiConfigs) aiConfigRepository.getAllConfigsOnce() else emptyList(),
            weightRecords = weightRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE),
            waterRecords = waterRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE),
            favoriteRecipes = favoriteRecipeRepository.getAllFavoritesOnce(),
            pantryIngredients = pantryIngredientRepository.getAllOnce(),
            recipePlans = recipePlanRepository.getAllOnce(),
            aiChatHistory = aiChatHistoryDao.getAllHistoryOnce(),
            apiCallLogs = apiCallRecordDao.getAllRecordsOnce()
        )
    }
}
