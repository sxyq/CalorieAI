package com.calorieai.app.service.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 备份服务 - v3.2.0
 * 支持完整的数据备份和恢复，包括扩展营养素和壁纸设置
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavBackupService: WebDavBackupService,
    private val backupExportService: BackupExportService,
    private val backupImportService: BackupImportService,
    private val backupPreviewService: BackupPreviewService
) {
    private val json = backupJson

    /**
     * 创建备份
     * @param includeAIConfigs 是否包含AI配置（默认true）
     */
    suspend fun createBackup(uri: Uri, includeAIConfigs: Boolean = true): Result<BackupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = backupExportService.buildExportPayload(includeAIConfigs)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(payload.json)
                }
            } ?: throw IllegalStateException("无法创建输出流")
            backupExportService.buildResult(payload, "备份成功")
        }
    }

    suspend fun uploadBackupToWebDav(
        config: WebDavConfig,
        includeAIConfigs: Boolean = true
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = backupExportService.buildExportPayload(includeAIConfigs)
            webDavBackupService.uploadJson(config, payload.json).getOrThrow()
            backupExportService.buildResult(payload, "云备份上传成功")
        }
    }

    /**
     * 恢复备份
     */
    suspend fun restoreBackup(
        uri: Uri,
        restoreMode: RestoreMode = RestoreMode.MERGE
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = readUriText(uri)
            val backupData = decodeBackupData(jsonString)
            validateBackupVersion(backupData)
            backupImportService.restoreBackupData(backupData, restoreMode)
        }
    }

    /**
     * 获取备份信息（不恢复）
     */
    suspend fun getBackupInfo(uri: Uri): Result<BackupData> = withContext(Dispatchers.IO) {
        runCatching {
            decodeBackupData(readUriText(uri))
        }
    }

    suspend fun getBackupInfoFromWebDav(config: WebDavConfig): Result<BackupData> = withContext(Dispatchers.IO) {
        runCatching {
            decodeBackupData(webDavBackupService.downloadJson(config).getOrThrow())
        }
    }

    suspend fun restoreBackupFromWebDav(
        config: WebDavConfig,
        restoreMode: RestoreMode = RestoreMode.MERGE
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val backupData = decodeBackupData(webDavBackupService.downloadJson(config).getOrThrow())
            validateBackupVersion(backupData)
            backupImportService.restoreBackupData(backupData, restoreMode)
        }
    }

    suspend fun buildRestorePreview(
        backupData: BackupData,
        mode: RestoreMode
    ): RestorePreview = backupPreviewService.buildRestorePreview(backupData, mode)

    private fun readUriText(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalStateException("无法读取文件")
    }

    private fun decodeBackupData(raw: String): BackupData {
        return json.decodeFromString(raw)
    }

    private fun validateBackupVersion(backupData: BackupData) {
        if (backupData.version > 7) {
            throw IllegalStateException("备份版本(${backupData.version})高于当前应用支持的版本，请升级应用后重试")
        }
    }

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
