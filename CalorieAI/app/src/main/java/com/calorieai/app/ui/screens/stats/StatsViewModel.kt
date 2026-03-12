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
            // 获取所有记录
            foodRecordRepository.getAllRecords().collect { records ->
                // 获取用户设置（目标热量）
                val settings = userSettingsRepository.getSettingsOnce()
                val targetCalories = settings?.dailyCalorieGoal ?: 2000

                // 计算统计数据
                val todayStats = StatsUtils.computeTodayStats(records, targetCalories)
                val mealTypeStats = StatsUtils.computeMealTypeStats(records)
                val historyStats = StatsUtils.computeHistoryStats(records)
                val weeklyStats = StatsUtils.computeWeeklyTrend(records)
                val monthlyStats = StatsUtils.computeMonthlyTrend(records)
                val lastMonthSummary = StatsUtils.computeLastMonthSummary(records)
                val streakDays = StatsUtils.computeStreakDays(records)

                _uiState.value = StatsUiState(
                    todayStats = todayStats,
                    mealTypeStats = mealTypeStats,
                    historyStats = historyStats,
                    weeklyStats = weeklyStats,
                    monthlyStats = monthlyStats,
                    lastMonthSummary = lastMonthSummary,
                    streakDays = streakDays
                )
            }
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
    val streakDays: Int = 0
)
