package com.calorieai.app.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.WeightRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeightRecordViewModel @Inject constructor(
    private val weightRecordRepository: WeightRecordRepository,
    private val userSettingsRepository: com.calorieai.app.data.repository.UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightRecordUiState())
    val uiState: StateFlow<WeightRecordUiState> = _uiState.asStateFlow()

    init {
        loadCurrentWeight()
        loadWeightHistory()
    }

    private fun loadCurrentWeight() {
        viewModelScope.launch {
            weightRecordRepository.getLatestRecord().collect { record ->
                _uiState.value = _uiState.value.copy(
                    currentWeight = record?.weight,
                    lastRecordDate = record?.recordDate
                )
            }
        }
    }

    private fun loadWeightHistory() {
        viewModelScope.launch {
            weightRecordRepository.getAllRecords().collect { records ->
                val items = records.map { record ->
                    WeightRecordItem(
                        id = record.id.toString(),
                        weight = record.weight,
                        recordDate = record.recordDate,
                        note = record.note ?: ""
                    )
                }
                _uiState.value = _uiState.value.copy(weightHistory = items)
            }
        }
    }

    fun updateWeightInput(weight: String) {
        _uiState.value = _uiState.value.copy(weightInput = weight)
    }

    fun updateNoteInput(note: String) {
        _uiState.value = _uiState.value.copy(noteInput = note)
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun confirmDateSelection() {
        // 日期选择器会更新selectedDate
        hideDatePicker()
    }

    fun showHistoryDialog() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = true)
    }

    fun hideHistoryDialog() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = false)
    }

    fun saveWeightRecord() {
        val weight = _uiState.value.weightInput.toFloatOrNull() ?: return
        
        viewModelScope.launch {
            // 保存体重记录
            val record = WeightRecord(
                weight = weight,
                recordDate = _uiState.value.selectedDate,
                note = _uiState.value.noteInput.takeIf { it.isNotBlank() }
            )
            weightRecordRepository.insertRecord(record)
            
            // 同时更新UserSettings中的体重，以便同步到首页和个人信息
            val currentSettings = userSettingsRepository.getSettings().firstOrNull()
            currentSettings?.let { settings ->
                val updatedSettings = settings.copy(userWeight = weight)
                userSettingsRepository.saveSettings(updatedSettings)
            }
            
            // 重置输入
            _uiState.value = _uiState.value.copy(
                weightInput = "",
                noteInput = ""
            )
        }
    }

    fun deleteWeightRecord(recordId: String) {
        viewModelScope.launch {
            weightRecordRepository.deleteRecordById(recordId.toLong())
        }
    }
}

data class WeightRecordUiState(
    val weightInput: String = "",
    val noteInput: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val currentWeight: Float? = null,
    val lastRecordDate: Long? = null,
    val weightHistory: List<WeightRecordItem> = emptyList(),
    val showDatePicker: Boolean = false,
    val showHistoryDialog: Boolean = false
)
