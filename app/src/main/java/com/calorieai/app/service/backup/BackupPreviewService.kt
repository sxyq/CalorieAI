package com.calorieai.app.service.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupPreviewService @Inject constructor(
    private val backupSnapshotProvider: BackupSnapshotProvider
) {
    suspend fun buildRestorePreview(
        backupData: BackupData,
        mode: RestoreMode
    ): RestorePreview = withContext(Dispatchers.IO) {
        val snapshot = backupSnapshotProvider.collect(backupData.includeAIConfigs)

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
                overwriteItem("饮食记录", backupData.foodRecords.size, snapshot.foodRecords.size),
                overwriteItem("运动记录", backupData.exerciseRecords.size, snapshot.exerciseRecords.size),
                overwriteItem("体重记录", backupData.weightRecords.size, snapshot.weightRecords.size),
                overwriteItem("饮水记录", backupData.waterRecords.size, snapshot.waterRecords.size),
                overwriteItem("收藏菜谱", backupData.favoriteRecipes.size, snapshot.favoriteRecipes.size),
                overwriteItem("食材库存", backupData.pantryIngredients.size, snapshot.pantryIngredients.size),
                overwriteItem("旧版菜谱指南(并入收藏)", backupData.legacyRecipeGuides.size, 0),
                overwriteItem("菜单计划", backupData.recipePlans.size, snapshot.recipePlans.size),
                overwriteItem("AI对话历史", backupData.aiChatHistory.size, snapshot.aiChatHistory.size),
                overwriteItem("API调用日志", backupData.apiCallLogs.size, snapshot.apiCallLogs.size),
                overwriteItem("用户设置", if (backupData.userSettings != null) 1 else 0, snapshot.settingsCount),
                overwriteItem("AI配置", if (backupData.includeAIConfigs) backupData.aiConfigs.size else 0, snapshot.aiConfigs.size)
            )
        } else {
            val settingsBackupCount = if (backupData.userSettings != null) 1 else 0
            val settingsAdd = if (settingsBackupCount == 1 && snapshot.settingsCount == 0) 1 else 0
            val settingsUpdate = if (settingsBackupCount == 1 && snapshot.settingsCount == 1) 1 else 0
            val settingsItem = RestorePreviewItem(
                label = "用户设置",
                backupCount = settingsBackupCount,
                existingCount = snapshot.settingsCount,
                addCount = settingsAdd,
                updateCount = settingsUpdate,
                clearCount = 0
            )

            listOf(
                mergeItem("饮食记录", backupData.foodRecords.map { it.id }.toSet(), snapshot.foodRecords.map { it.id }.toSet()),
                mergeItem("运动记录", backupData.exerciseRecords.map { it.id }.toSet(), snapshot.exerciseRecords.map { it.id }.toSet()),
                mergeItem("体重记录", backupData.weightRecords.map { it.id.toString() }.toSet(), snapshot.weightRecords.map { it.id.toString() }.toSet()),
                mergeItem("饮水记录", backupData.waterRecords.map { it.id.toString() }.toSet(), snapshot.waterRecords.map { it.id.toString() }.toSet()),
                mergeItem("收藏菜谱", backupData.favoriteRecipes.map { it.id }.toSet(), snapshot.favoriteRecipes.map { it.id }.toSet()),
                mergeItem("食材库存", backupData.pantryIngredients.map { it.id }.toSet(), snapshot.pantryIngredients.map { it.id }.toSet()),
                mergeItem("旧版菜谱指南(并入收藏)", backupData.legacyRecipeGuides.map { it.id }.toSet(), emptySet()),
                mergeItem("菜单计划", backupData.recipePlans.map { it.id }.toSet(), snapshot.recipePlans.map { it.id }.toSet()),
                mergeItem("AI对话历史", backupData.aiChatHistory.map { it.id.toString() }.toSet(), snapshot.aiChatHistory.map { it.id.toString() }.toSet()),
                mergeItem("API调用日志", backupData.apiCallLogs.map { it.id }.toSet(), snapshot.apiCallLogs.map { it.id }.toSet()),
                settingsItem,
                mergeItem(
                    "AI配置",
                    if (backupData.includeAIConfigs) backupData.aiConfigs.map { it.id }.toSet() else emptySet(),
                    snapshot.aiConfigs.map { it.id }.toSet()
                )
            )
        }

        RestorePreview(mode = mode, items = items)
    }
}
