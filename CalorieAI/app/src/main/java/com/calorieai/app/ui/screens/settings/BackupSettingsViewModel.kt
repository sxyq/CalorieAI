package com.calorieai.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.service.backup.BackupData
import com.calorieai.app.service.backup.BackupResult
import com.calorieai.app.service.backup.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupSettingsUiState(
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val backupInfo: BackupData? = null,
    val pendingRestoreUri: Uri? = null,
    val includeAIConfigs: Boolean = true  // 是否包含AI配置
)

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val backupService: BackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupSettingsUiState())
    val uiState: StateFlow<BackupSettingsUiState> = _uiState.asStateFlow()

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
                        resultMessage = "${result.message}：${result.foodRecordCount}条饮食记录，${result.exerciseRecordCount}条运动记录" +
                            if (result.weightRecordCount > 0) "，${result.weightRecordCount}条体重记录" else "",
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showRestoreDialog = true,
                        backupInfo = backupData,
                        pendingRestoreUri = uri
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
        val uri = _uiState.value.pendingRestoreUri ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showRestoreDialog = false
            )
            
            backupService.restoreBackup(uri)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = "${result.message}：${result.foodRecordCount}条饮食记录，${result.exerciseRecordCount}条运动记录" +
                            if (result.weightRecordCount > 0) "，${result.weightRecordCount}条体重记录" else "",
                        isSuccess = true,
                        backupInfo = null,
                        pendingRestoreUri = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resultMessage = "恢复失败: ${error.message}",
                        isSuccess = false,
                        backupInfo = null,
                        pendingRestoreUri = null
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
            pendingRestoreUri = null
        )
    }
}
