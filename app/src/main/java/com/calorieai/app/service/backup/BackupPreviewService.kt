package com.calorieai.app.service.backup

import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupPreviewService @Inject constructor(
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
                overwriteItem("旧版菜谱指南(并入收藏)", backupData.legacyRecipeGuides.size, 0),
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
                mergeItem("旧版菜谱指南(并入收藏)", backupData.legacyRecipeGuides.map { it.id }.toSet(), emptySet()),
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
}
