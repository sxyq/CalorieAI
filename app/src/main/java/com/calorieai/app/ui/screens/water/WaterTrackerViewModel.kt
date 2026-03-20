package com.calorieai.app.ui.screens.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class WaterTrackerViewModel @Inject constructor(
    private val waterRecordRepository: WaterRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterTrackerUiState())
    val uiState: StateFlow<WaterTrackerUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettingsOnce() ?: UserSettings()
            val todayStart = getStartOfDay(System.currentTimeMillis())
            val todayEnd = getEndOfDay(System.currentTimeMillis())
            val todayRecords = waterRecordRepository.getRecordsBetweenSync(todayStart, todayEnd)
                .sortedByDescending { it.recordTime }

            _uiState.value = _uiState.value.copy(
                currentWater = todayRecords.sumOf { it.amount },
                dailyGoal = settings.dailyWaterGoal.coerceIn(1200, 5000),
                records = todayRecords
            )
        }
    }

    fun addWater(amount: Int) {
        val safeAmount = amount.coerceIn(10, 3000)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            waterRecordRepository.insert(
                WaterRecord(
                    amount = safeAmount,
                    recordTime = now,
                    recordDate = getStartOfDay(now)
                )
            )
            refreshData()
        }
    }

    fun updateDailyGoal(goal: Int) {
        val safeGoal = goal.coerceIn(1200, 5000)
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettingsOnce() ?: UserSettings()
            userSettingsRepository.saveSettings(settings.copy(dailyWaterGoal = safeGoal))
            _uiState.value = _uiState.value.copy(dailyGoal = safeGoal)
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}

data class WaterTrackerUiState(
    val currentWater: Int = 0,
    val dailyGoal: Int = 2000,
    val records: List<WaterRecord> = emptyList()
)
