package com.calorieai.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.service.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportData(uri: Uri, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            backupManager.exportToJson(uri)
                .onSuccess { count ->
                    onResult(true, count)
                }
                .onFailure {
                    onResult(false, 0)
                }
        }
    }

    fun importData(uri: Uri, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            backupManager.importFromJson(uri)
                .onSuccess { count ->
                    onResult(true, count)
                }
                .onFailure {
                    onResult(false, 0)
                }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false
)
