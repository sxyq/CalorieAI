package com.calorieai.app.ui.screens.settings

import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.service.backup.WebDavConfig
import com.calorieai.app.service.backup.BackupData
import com.calorieai.app.service.backup.BackupResult
import com.calorieai.app.service.backup.BackupService
import com.calorieai.app.service.backup.RestorePreview
import com.calorieai.app.service.backup.RestoreMode
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject

data class BackupSettingsUiState(
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val backupInfo: BackupData? = null,
    val pendingRestoreUri: Uri? = null,
    val pendingCloudRestore: Boolean = false,
    val includeAIConfigs: Boolean = true,  // 是否包含AI配置
    val restoreMode: RestoreMode = RestoreMode.OVERWRITE,
    val restorePreview: RestorePreview? = null,
    val webDavUrl: String = "",
    val webDavDirectory: String = "calorieai",
    val webDavFileName: String = "backup_latest.json",
    val webDavUsername: String = "",
    val webDavPassword: String = ""
)

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val backupService: BackupService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupSettingsUiState())
    val uiState: StateFlow<BackupSettingsUiState> = _uiState.asStateFlow()
    private val cloudPrefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "cloud_backup_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences("cloud_backup_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    init {
        _uiState.value = _uiState.value.copy(
            webDavUrl = cloudPrefs.getString("webdav_url", "") ?: "",
            webDavDirectory = cloudPrefs.getString("webdav_dir", "calorieai") ?: "calorieai",
            webDavFileName = cloudPrefs.getString("webdav_file", "backup_latest.json") ?: "backup_latest.json",
            webDavUsername = cloudPrefs.getString("webdav_user", "") ?: "",
            webDavPassword = cloudPrefs.getString("webdav_pass", "") ?: ""
        )
    }

    /**
     * 创建备份
     */
    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, resultMessage = null)
            
            backupService.createBackup(uri, _uiState.value.includeAIConfigs)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = buildResultMessage(result),
                        isSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = "备份失败: ${error.message}",
                        isSuccess = false
                    )
                }
        }
    }
    
    /**
     * 设置是否包含AI配置
     */
    fun setIncludeAIConfigs(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeAIConfigs = include)
    }

    /**
     * 加载备份信息
     */
    fun loadBackupInfo(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            backupService.getBackupInfo(uri)
                .onSuccess { backupData ->
                    val preview = backupService.buildRestorePreview(backupData, _uiState.value.restoreMode)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showRestoreDialog = true,
                        backupInfo = backupData,
                        pendingRestoreUri = uri,
                        pendingCloudRestore = false,
                        restorePreview = preview
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = "读取备份失败: ${error.message}",
                        isSuccess = false
                    )
                }
        }
    }

    /**
     * 确认恢复
     */
    fun confirmRestore() {
        val current = _uiState.value
        val localUri = current.pendingRestoreUri
        val fromCloud = current.pendingCloudRestore
        if (localUri == null && !fromCloud) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showRestoreDialog = false
            )
            
            val restoreRequest = if (fromCloud) {
                backupService.restoreBackupFromWebDav(current.toWebDavConfig(), current.restoreMode)
            } else {
                backupService.restoreBackup(localUri!!, current.restoreMode)
            }
            restoreRequest
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = buildResultMessage(result),
                        isSuccess = true,
                        backupInfo = null,
                        pendingRestoreUri = null,
                        pendingCloudRestore = false,
                        restorePreview = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = "恢复失败: ${error.message}",
                        isSuccess = false,
                        backupInfo = null,
                        pendingRestoreUri = null,
                        pendingCloudRestore = false,
                        restorePreview = null
                    )
                }
        }
    }

    /**
     * 关闭恢复对话框
     */
    fun dismissRestoreDialog() {
        _uiState.value = _uiState.value.copy(
            showRestoreDialog = false,
            backupInfo = null,
            pendingRestoreUri = null,
            pendingCloudRestore = false,
            restorePreview = null
        )
    }

    fun setRestoreMode(mode: RestoreMode) {
        _uiState.value = _uiState.value.copy(restoreMode = mode)
        refreshRestorePreview()
    }

    private fun refreshRestorePreview() {
        val backupInfo = _uiState.value.backupInfo ?: return
        viewModelScope.launch {
            val preview = backupService.buildRestorePreview(backupInfo, _uiState.value.restoreMode)
            _uiState.value = _uiState.value.copy(restorePreview = preview)
        }
    }

    fun updateWebDavUrl(value: String) {
        _uiState.value = _uiState.value.copy(webDavUrl = value)
        cloudPrefs.edit().putString("webdav_url", value).apply()
    }

    fun updateWebDavDirectory(value: String) {
        _uiState.value = _uiState.value.copy(webDavDirectory = value)
        cloudPrefs.edit().putString("webdav_dir", value).apply()
    }

    fun updateWebDavFileName(value: String) {
        _uiState.value = _uiState.value.copy(webDavFileName = value)
        cloudPrefs.edit().putString("webdav_file", value).apply()
    }

    fun updateWebDavUsername(value: String) {
        _uiState.value = _uiState.value.copy(webDavUsername = value)
        cloudPrefs.edit().putString("webdav_user", value).apply()
    }

    fun updateWebDavPassword(value: String) {
        _uiState.value = _uiState.value.copy(webDavPassword = value)
        cloudPrefs.edit().putString("webdav_pass", value).apply()
    }

    fun uploadCloudBackup() {
        val config = _uiState.value.toWebDavConfig()
        if (!config.isValid()) {
            _uiState.value = _uiState.value.copy(
                isSuccess = false,
                resultMessage = "请完整填写 WebDAV 地址、文件名、用户名和密码"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, resultMessage = null)
            backupService.uploadBackupToWebDav(config, _uiState.value.includeAIConfigs)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        resultMessage = buildResultMessage(result)
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        resultMessage = "云备份上传失败: ${error.message}"
                    )
                }
        }
    }

    fun loadCloudBackupInfo() {
        val config = _uiState.value.toWebDavConfig()
        if (!config.isValid()) {
            _uiState.value = _uiState.value.copy(
                isSuccess = false,
                resultMessage = "请完整填写 WebDAV 地址、文件名、用户名和密码"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, resultMessage = null)
            backupService.getBackupInfoFromWebDav(config)
                .onSuccess { backupData ->
                    val preview = backupService.buildRestorePreview(backupData, _uiState.value.restoreMode)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showRestoreDialog = true,
                        backupInfo = backupData,
                        pendingRestoreUri = null,
                        pendingCloudRestore = true,
                        restorePreview = preview
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        resultMessage = "读取云备份失败: ${error.message}"
                    )
                }
        }
    }

    private fun BackupSettingsUiState.toWebDavConfig(): WebDavConfig {
        return WebDavConfig(
            baseUrl = webDavUrl.trim(),
            directory = webDavDirectory.trim(),
            fileName = webDavFileName.trim(),
            username = webDavUsername.trim(),
            password = webDavPassword
        )
    }

    private fun WebDavConfig.isValid(): Boolean {
        return baseUrl.isNotBlank() &&
            fileName.isNotBlank() &&
            username.isNotBlank() &&
            password.isNotBlank()
    }

    private fun buildResultMessage(result: BackupResult): String {
        return buildString {
            append("${result.message}：${result.foodRecordCount}条饮食记录，${result.exerciseRecordCount}条运动记录")
            if (result.weightRecordCount > 0) append("，${result.weightRecordCount}条体重记录")
            if (result.waterRecordCount > 0) append("，${result.waterRecordCount}条饮水记录")
            if (result.favoriteRecipeCount > 0) append("，${result.favoriteRecipeCount}条收藏菜谱")
            if (result.aiChatHistoryCount > 0) append("，${result.aiChatHistoryCount}条AI对话历史")
            if (result.apiCallLogCount > 0) append("，${result.apiCallLogCount}条API调用日志")
            if (result.aiConfigCount > 0) append("，${result.aiConfigCount}个AI配置")
        }
    }
}
