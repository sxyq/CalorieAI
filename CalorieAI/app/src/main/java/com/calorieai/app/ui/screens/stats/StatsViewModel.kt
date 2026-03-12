package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
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

    // 当前选中的月份（用于上月总结）
    private val _selectedMonth = MutableStateFlow(LocalDate.now().minusMonths(1))
    val selectedMonth: StateFlow<LocalDate> = _selectedMonth.asStateFlow()

    // 趋势分析日期范围
    private val _trendStartDate = MutableStateFlow(LocalDate.now().minusMonths(1))
    val trendStartDate: StateFlow<LocalDate> = _trendStartDate.asStateFlow()

    private val _trendEndDate = MutableStateFlow(LocalDate.now())
    val trendEndDate: StateFlow<LocalDate> = _trendEndDate.asStateFlow()

    private var allRecords: List<FoodRecord> = emptyList()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            foodRecordRepository.getAllRecords().collect { records ->
                allRecords = records
                updateStats()
            }
        }
    }

    private fun updateStats() {
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettingsOnce()
            val targetCalories = settings?.dailyCalorieGoal ?: 2000

            val todayStats = StatsUtils.computeTodayStats(allRecords, targetCalories)
            val mealTypeStats = StatsUtils.computeMealTypeStats(allRecords)
            val historyStats = StatsUtils.computeHistoryStats(allRecords)
            val weeklyStats = StatsUtils.computeWeeklyTrend(allRecords)
            val monthlyStats = StatsUtils.computeMonthlyTrend(allRecords)

            // 根据选中的月份计算总结
            val monthSummary = StatsUtils.computeMonthSummary(
                allRecords,
                _selectedMonth.value.year,
                _selectedMonth.value.monthValue
            )

            // 根据日期范围计算趋势
            val trendStats = StatsUtils.computeTrendInRange(
                allRecords,
                _trendStartDate.value,
                _trendEndDate.value
            )

            val streakDays = StatsUtils.computeStreakDays(allRecords)

            _uiState.value = StatsUiState(
                todayStats = todayStats,
                mealTypeStats = mealTypeStats,
                historyStats = historyStats,
                weeklyStats = weeklyStats,
                monthlyStats = monthlyStats,
                lastMonthSummary = monthSummary,
                trendStats = trendStats,
                streakDays = streakDays,
                targetCalories = targetCalories
            )
        }
    }

    /**
     * 切换月份（上月总结）
     */
    fun changeMonth(monthsOffset: Int) {
        _selectedMonth.value = _selectedMonth.value.plusMonths(monthsOffset.toLong())
        updateStats()
    }

    /**
     * 设置趋势分析开始日期
     */
    fun setTrendStartDate(date: LocalDate) {
        if (!date.isAfter(_trendEndDate.value)) {
            _trendStartDate.value = date
            updateStats()
        }
    }

    /**
     * 设置趋势分析结束日期
     */
    fun setTrendEndDate(date: LocalDate) {
        if (!date.isBefore(_trendStartDate.value)) {
            _trendEndDate.value = date
            updateStats()
        }
    }
}

data class StatsUiState(
    val todayStats: TodayStats? = null,
    val mealTypeStats: Map<MealType, Int> = emptyMap(),
    val historyStats: HistoryStats? = null,
    val weeklyStats: List<WeeklyStat> = emptyList(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val lastMonthSummary: MonthSummary? = null,
    val trendStats: TrendStats? = null,
    val streakDays: Int = 0,
    val targetCalories: Int = 2000
)

// 趋势统计数据
data class TrendStats(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val avgCalories: Int,
    val totalCalories: Int,
    val daysRecorded: Int,
    val maxDailyCalories: Int,
    val minDailyCalories: Int
)
