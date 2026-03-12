package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            foodRecordRepository.getAllRecords().collect { records ->
                val settings = userSettingsRepository.getSettingsOnce()
                val targetCalories = settings?.dailyCalorieGoal ?: 2000

                val todayStats = StatsUtils.computeTodayStats(records, targetCalories)
                val mealTypeStats = StatsUtils.computeMealTypeStats(records)
                val historyStats = StatsUtils.computeHistoryStats(records)
                val weeklyStats = StatsUtils.computeWeeklyTrend(records)
                val monthlyStats = StatsUtils.computeMonthlyTrend(records)
                val streakDays = StatsUtils.computeStreakDays(records)

                // 根据当前选择的月份计算总结
                val currentMonthOffset = _uiState.value.selectedMonthOffset
                val summary = StatsUtils.computeMonthSummary(records, currentMonthOffset)

                _uiState.value = _uiState.value.copy(
                    todayStats = todayStats,
                    mealTypeStats = mealTypeStats,
                    historyStats = historyStats,
                    weeklyStats = weeklyStats,
                    monthlyStats = monthlyStats,
                    lastMonthSummary = summary,
                    streakDays = streakDays,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 切换月份（上月总结）
     */
    fun changeMonth(offset: Int) {
        _uiState.value = _uiState.value.copy(selectedMonthOffset = offset)
        refreshMonthSummary()
    }

    /**
     * 刷新月份总结
     */
    private fun refreshMonthSummary() {
        viewModelScope.launch {
            val records = foodRecordRepository.getAllRecordsOnce()
            val offset = _uiState.value.selectedMonthOffset
            val summary = StatsUtils.computeMonthSummary(records, offset)
            _uiState.value = _uiState.value.copy(lastMonthSummary = summary)
        }
    }

    /**
     * 设置趋势分析日期范围
     */
    fun setTrendDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            val records = foodRecordRepository.getAllRecordsOnce()
            val filteredRecords = records.filter {
                val recordDate = java.time.Instant.ofEpochMilli(it.recordTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                recordDate in startDate..endDate
            }

            val weeklyStats = StatsUtils.computeWeeklyTrend(filteredRecords)
            val monthlyStats = StatsUtils.computeMonthlyTrendForRange(filteredRecords, startDate, endDate)

            _uiState.value = _uiState.value.copy(
                trendStartDate = startDate,
                trendEndDate = endDate,
                weeklyStats = weeklyStats,
                monthlyStats = monthlyStats
            )
        }
    }

    /**
     * 重置趋势日期范围
     */
    fun resetTrendDateRange() {
        loadStats()
    }
}

data class StatsUiState(
    val todayStats: TodayStats? = null,
    val mealTypeStats: Map<MealType, Int> = emptyMap(),
    val historyStats: HistoryStats? = null,
    val weeklyStats: List<WeeklyStat> = emptyList(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val lastMonthSummary: MonthSummary? = null,
    val streakDays: Int = 0,
    val selectedMonthOffset: Int = 1, // 默认显示上个月
    val trendStartDate: LocalDate? = null,
    val trendEndDate: LocalDate? = null,
    val isLoading: Boolean = true
)
