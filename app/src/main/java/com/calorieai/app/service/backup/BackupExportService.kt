package com.calorieai.app.service.backup

import kotlinx.coroutines.Dispatchers
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
    private val backupSnapshotProvider: BackupSnapshotProvider
) {
    internal suspend fun buildExportPayload(includeAIConfigs: Boolean): BackupExportPayload = withContext(Dispatchers.IO) {
        val snapshot = backupSnapshotProvider.collect(includeAIConfigs)

        val backupData = BackupData(
            backupDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            foodRecords = snapshot.foodRecords.map { it.toBackup() },
            exerciseRecords = snapshot.exerciseRecords.map { it.toBackup() },
            userSettings = snapshot.userSettings?.toBackup(),
            aiConfigs = snapshot.aiConfigs.map { it.toBackup() },
            weightRecords = snapshot.weightRecords.map { it.toBackup() },
            waterRecords = snapshot.waterRecords.map { it.toBackup() },
            favoriteRecipes = snapshot.favoriteRecipes.map { it.toBackup() },
            pantryIngredients = snapshot.pantryIngredients.map { it.toBackup() },
            recipePlans = snapshot.recipePlans.map { it.toBackup() },
            aiChatHistory = snapshot.aiChatHistory.map { it.toBackup() },
            apiCallLogs = snapshot.apiCallLogs.map { it.toBackup() },
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
