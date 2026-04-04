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
import kotlinx.serialization.encodeToString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

internal data class BackupExportPayload(
    val backupData: BackupData,
    val json: String
)

@Singleton
class BackupExportService @Inject constructor(
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
    internal suspend fun buildExportPayload(includeAIConfigs: Boolean): BackupExportPayload = withContext(Dispatchers.IO) {
        val foodRecords = foodRecordRepository.getAllRecordsOnce()
        val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val userSettings = userSettingsRepository.getSettings().first()
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val waterRecords = waterRecordRepository.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val favoriteRecipes = favoriteRecipeRepository.getAllFavoritesOnce()
        val pantryIngredients = pantryIngredientRepository.getAllOnce()
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
            recipePlans = recipePlans.map { it.toBackup() },
            aiChatHistory = aiChatHistory.map { it.toBackup() },
            apiCallLogs = apiCallLogs.map { it.toBackup() },
            includeAIConfigs = includeAIConfigs
        )

        BackupExportPayload(
            backupData = backupData,
            json = backupJson.encodeToString(backupData)
        )
    }

    internal fun buildResult(payload: BackupExportPayload, successMessage: String): BackupResult {
        return payload.backupData.toBackupResult(successMessage)
    }
}

internal fun BackupData.toBackupResult(message: String): BackupResult {
    return BackupResult(
        foodRecordCount = foodRecords.size,
        exerciseRecordCount = exerciseRecords.size,
        weightRecordCount = weightRecords.size,
        waterRecordCount = waterRecords.size,
        favoriteRecipeCount = favoriteRecipes.size,
        pantryIngredientCount = pantryIngredients.size,
        recipeGuideCount = legacyRecipeGuides.size,
        recipePlanCount = recipePlans.size,
        aiChatHistoryCount = aiChatHistory.size,
        apiCallLogCount = apiCallLogs.size,
        aiConfigCount = if (includeAIConfigs) aiConfigs.size else 0,
        message = message
    )
}
