package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            foodRecordRepository.getAllRecords().collect { records ->
                _uiState.value = calculateStats(records)
            }
        }
    }

    private fun calculateStats(records: List<FoodRecord>): StatsUiState {
        if (records.isEmpty()) {
            return StatsUiState(isLoading = false)
        }

        // 今日数据
        val todayRecords = getTodayRecords(records)
        val todayCalories = todayRecords.sumOf { it.totalCalories }

        // 本周数据（最近7天，包括今天）
        val weekRecords = getWeekRecords(records)
        val weekAverage = if (weekRecords.isNotEmpty()) {
            weekRecords.sumOf { it.totalCalories } / weekRecords.size
        } else 0

        // 本月数据（最近30天）
        val monthRecords = getMonthRecords(records)
        val monthAverage = if (monthRecords.isNotEmpty()) {
            monthRecords.sumOf { it.totalCalories } / monthRecords.size
        } else 0

        // 最高摄入
        val highestIntake = records.maxByOrNull { it.totalCalories }

        // 总记录数
        val totalRecords = records.size

        return StatsUiState(
            todayCalories = todayCalories,
            weekAverage = weekAverage,
            monthAverage = monthAverage,
            highestIntake = highestIntake?.totalCalories ?: 0,
            highestIntakeFood = highestIntake?.foodName ?: "",
            totalRecords = totalRecords,
            isLoading = false
        )
    }

    private fun getTodayRecords(records: List<FoodRecord>): List<FoodRecord> {
        val calendar = Calendar.getInstance()
        
        // 获取今天的开始时间（00:00:00）
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        // 获取今天的结束时间（23:59:59）
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        return records.filter { it.recordTime in startOfDay..endOfDay }
    }

    private fun getWeekRecords(records: List<FoodRecord>): List<FoodRecord> {
        val calendar = Calendar.getInstance()
        
        // 获取今天结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        
        // 回退7天
        calendar.add(Calendar.DAY_OF_YEAR, -6) // -6是因为包括今天
        val weekAgo = calendar.timeInMillis
        
        return records.filter { it.recordTime >= weekAgo }
    }

    private fun getMonthRecords(records: List<FoodRecord>): List<FoodRecord> {
        val calendar = Calendar.getInstance()
        
        // 获取今天结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        
        // 回退30天
        calendar.add(Calendar.DAY_OF_YEAR, -29) // -29是因为包括今天
        val monthAgo = calendar.timeInMillis
        
        return records.filter { it.recordTime >= monthAgo }
    }
}

data class StatsUiState(
    val todayCalories: Int = 0,
    val weekAverage: Int = 0,
    val monthAverage: Int = 0,
    val highestIntake: Int = 0,
    val highestIntakeFood: String = "",
    val totalRecords: Int = 0,
    val isLoading: Boolean = true
)
