package com.calorieai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightHistoryViewModel @Inject constructor(
    private val weightRecordRepository: WeightRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _weightRecords = MutableStateFlow<List<WeightRecord>>(emptyList())
    val weightRecords: StateFlow<List<WeightRecord>> = _weightRecords.asStateFlow()

    init {
        loadWeightRecords()
    }

    private fun loadWeightRecords() {
        viewModelScope.launch {
            weightRecordRepository.getAllRecordsByDateAsc().collect { records ->
                _weightRecords.value = records
            }
        }
    }

    fun addWeightRecord(weight: Float, note: String?) {
        viewModelScope.launch {
            val record = WeightRecord(
                weight = weight,
                recordDate = System.currentTimeMillis(),
                note = note
            )
            weightRecordRepository.insert(record)
            
            // 同时更新UserSettings中的体重
            val currentSettings = userSettingsRepository.getSettings().firstOrNull()
            currentSettings?.let { settings ->
                val updatedSettings = settings.copy(userWeight = weight)
                userSettingsRepository.saveSettings(updatedSettings)
            }
        }
    }

    fun deleteWeightRecord(record: WeightRecord) {
        viewModelScope.launch {
            weightRecordRepository.delete(record)
        }
    }
}
