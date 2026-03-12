package com.calorieai.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupSettingsUiState())
    val uiState: StateFlow<BackupSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.value = BackupSettingsUiState(
                        enableAutoBackup = it.enableAutoBackup,
                        lastBackupTime = it.lastBackupTime,
                        enableCloudSync = it.enableCloudSync
                    )
                }
            }
        }
    }

    fun updateAutoBackup(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableAutoBackup = enabled)
        saveSettings()
    }

    fun updateCloudSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableCloudSync = enabled)
        saveSettings()
    }

    fun exportData(context: Context) {
        // TODO: 实现数据导出逻辑
        _uiState.value = _uiState.value.copy(
            exportResult = BackupResult.Success("导出功能开发中")
        )
    }

    fun importData(context: Context) {
        // TODO: 实现数据导入逻辑
        _uiState.value = _uiState.value.copy(
            exportResult = BackupResult.Success("导入功能开发中")
        )
    }

    fun syncToCloud() {
        // TODO: 实现云同步逻辑
        _uiState.value = _uiState.value.copy(
            exportResult = BackupResult.Success("云同步功能开发中")
        )
    }

    fun restoreFromCloud() {
        // TODO: 实现从云端恢复逻辑
        _uiState.value = _uiState.value.copy(
            exportResult = BackupResult.Success("云端恢复功能开发中")
        )
    }

    fun clearExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val settings = UserSettings(
                enableAutoBackup = currentState.enableAutoBackup,
                lastBackupTime = currentState.lastBackupTime,
                enableCloudSync = currentState.enableCloudSync
            )
            userSettingsRepository.saveSettings(settings)
        }
    }
}

data class BackupSettingsUiState(
    val enableAutoBackup: Boolean = false,
    val lastBackupTime: String? = null,
    val enableCloudSync: Boolean = false,
    val exportResult: BackupResult? = null
)
