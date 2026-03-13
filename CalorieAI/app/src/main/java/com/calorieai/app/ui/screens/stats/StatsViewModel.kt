package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.ui.components.charts.TimeDimension
import com.calorieai.app.ui.components.charts.TrendChartData
import com.calorieai.app.ui.screens.settings.calculateBMR
import com.calorieai.app.ui.screens.settings.calculateTDEE
import com.calorieai.app.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val weightRecordRepository: WeightRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                foodRecordRepository.getAllRecords(),
                exerciseRecordRepository.getAllRecords(),
                weightRecordRepository.getLatestRecord()
            ) { foodRecords, exerciseRecords, latestWeight ->
                val settings = userSettingsRepository.getSettingsOnce()
                val targetCalories = settings?.dailyCalorieGoal ?: 2000
                val userWeight = latestWeight?.weight ?: settings?.userWeight
                
                // 计算 BMR 和 TDEE
                val bmr = if (settings != null && userWeight != null) {
                    calculateBMR(
                        gender = settings.userGender ?: "MALE",
                        weight = userWeight,
                        height = settings.userHeight,
                        age = settings.userAge
                    )
                } else 0
                val tdee = if (bmr > 0 && settings != null) {
                    calculateTDEE(bmr, settings.activityLevel)
                } else 0

                val todayStats = StatsUtils.computeTodayStats(
                    foodRecords, 
                    exerciseRecords, 
                    targetCalories,
                    bmr,
                    tdee
                )
                val mealTypeStats = StatsUtils.computeMealTypeStats(foodRecords)
                val historyStats = StatsUtils.computeHistoryStats(foodRecords)
                val weeklyStats = StatsUtils.computeWeeklyTrend(foodRecords)
                val monthlyStats = StatsUtils.computeMonthlyTrend(foodRecords)
                val streakDays = StatsUtils.computeStreakDays(foodRecords)

                // 根据当前选择的月份计算总结
                val currentMonthOffset = _uiState.value.selectedMonthOffset
                val summary = StatsUtils.computeMonthSummary(foodRecords, exerciseRecords, currentMonthOffset, userWeight)

                // 计算统一趋势数据
                val trendData = computeTrendData(
                    foodRecords,
                    exerciseRecords,
                    _uiState.value.trendTimeDimension,
                    _uiState.value.trendStartDate,
                    _uiState.value.trendEndDate
                )

                _uiState.value.copy(
                    todayStats = todayStats,
                    mealTypeStats = mealTypeStats,
                    historyStats = historyStats,
                    weeklyStats = weeklyStats,
                    monthlyStats = monthlyStats,
                    lastMonthSummary = summary,
                    streakDays = streakDays,
                    trendChartData = trendData,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * 计算趋势图表数据（非挂起版本，用于非协程上下文）
     */
    private fun computeTrendData(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        timeDimension: TimeDimension,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): TrendChartData {
        val today = LocalDate.now()
        val actualStartDate = startDate ?: today.minusDays(30)
        val actualEndDate = endDate ?: today

        return when (timeDimension) {
            TimeDimension.DAY -> computeDailyTrendSync(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
            TimeDimension.WEEK -> computeWeeklyTrendDataSync(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
            TimeDimension.MONTH -> computeMonthlyTrendDataSync(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
        }
    }

    /**
     * 计算趋势图表数据（挂起版本，用于协程上下文）
     */
    private suspend fun computeTrendDataSuspend(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        timeDimension: TimeDimension,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): TrendChartData {
        val today = LocalDate.now()
        val actualStartDate = startDate ?: today.minusDays(30)
        val actualEndDate = endDate ?: today

        return when (timeDimension) {
            TimeDimension.DAY -> computeDailyTrend(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
            TimeDimension.WEEK -> computeWeeklyTrendData(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
            TimeDimension.MONTH -> computeMonthlyTrendData(foodRecords, exerciseRecords, actualStartDate, actualEndDate)
        }
    }

    /**
     * 计算每日趋势数据（同步版本）
     */
    private fun computeDailyTrendSync(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        var current = startDate
        while (!current.isAfter(endDate)) {
            dates.add(current)

            val dayStart = current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = current.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in dayStart..dayEnd }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in dayStart..dayEnd }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据（同步版本不获取体重数据）
            weightData.add(null)

            current = current.plusDays(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
    }

    /**
     * 计算每日趋势数据
     */
    private suspend fun computeDailyTrend(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        // 获取体重记录
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startMillis, endMillis)
        val weightMap = weightRecords.associateBy { 
            LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.recordDate), ZoneId.systemDefault())
        }

        var current = startDate
        while (!current.isAfter(endDate)) {
            dates.add(current)

            val dayStart = current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = current.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in dayStart..dayEnd }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in dayStart..dayEnd }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据
            weightData.add(weightMap[current]?.weight)

            current = current.plusDays(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
    }

    /**
     * 计算每周趋势数据（同步版本）
     */
    private fun computeWeeklyTrendDataSync(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val weekFields = WeekFields.of(java.util.Locale.getDefault())
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        var currentWeekStart = startDate.with(weekFields.dayOfWeek(), 1)
        while (!currentWeekStart.isAfter(endDate)) {
            val weekEnd = currentWeekStart.plusDays(6)
            dates.add(currentWeekStart)

            val weekStartMillis = currentWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val weekEndMillis = weekEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in weekStartMillis..weekEndMillis }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in weekStartMillis..weekEndMillis }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据（同步版本不获取体重数据）
            weightData.add(null)

            currentWeekStart = currentWeekStart.plusWeeks(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
    }

    /**
     * 计算每周趋势数据
     */
    private suspend fun computeWeeklyTrendData(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val weekFields = WeekFields.of(java.util.Locale.getDefault())
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        // 获取体重记录
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startMillis, endMillis)

        var currentWeekStart = startDate.with(weekFields.dayOfWeek(), 1)
        while (!currentWeekStart.isAfter(endDate)) {
            val weekEnd = currentWeekStart.plusDays(6)
            dates.add(currentWeekStart)

            val weekStartMillis = currentWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val weekEndMillis = weekEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in weekStartMillis..weekEndMillis }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in weekStartMillis..weekEndMillis }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据（取周平均值）
            val weekWeights = weightRecords.filter { 
                it.recordDate in weekStartMillis..weekEndMillis 
            }.map { it.weight }
            weightData.add(if (weekWeights.isNotEmpty()) weekWeights.average().toFloat() else null)

            currentWeekStart = currentWeekStart.plusWeeks(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
    }

    /**
     * 计算每月趋势数据（同步版本）
     */
    private fun computeMonthlyTrendDataSync(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        var currentMonth = startDate.withDayOfMonth(1)
        while (!currentMonth.isAfter(endDate)) {
            dates.add(currentMonth)

            val monthStartMillis = currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
            val monthEndMillis = monthEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in monthStartMillis..monthEndMillis }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in monthStartMillis..monthEndMillis }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据（同步版本不获取体重数据）
            weightData.add(null)

            currentMonth = currentMonth.plusMonths(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
    }

    /**
     * 计算每月趋势数据
     */
    private suspend fun computeMonthlyTrendData(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val dates = mutableListOf<LocalDate>()
        val calorieIntake = mutableListOf<Float>()
        val exerciseCalories = mutableListOf<Float>()
        val weightData = mutableListOf<Float?>()

        // 获取体重记录
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startMillis, endMillis)

        var currentMonth = startDate.withDayOfMonth(1)
        while (!currentMonth.isAfter(endDate)) {
            dates.add(currentMonth)

            val monthStartMillis = currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
            val monthEndMillis = monthEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // 热量摄入
            val intake = foodRecords
                .filter { it.recordTime in monthStartMillis..monthEndMillis }
                .sumOf { it.totalCalories }
                .toFloat()
            calorieIntake.add(intake)

            // 运动消耗
            val exercise = exerciseRecords
                .filter { it.recordTime in monthStartMillis..monthEndMillis }
                .sumOf { it.caloriesBurned }
                .toFloat()
            exerciseCalories.add(exercise)

            // 体重数据（取月平均值）
            val monthWeights = weightRecords.filter { 
                it.recordDate in monthStartMillis..monthEndMillis 
            }.map { it.weight }
            weightData.add(if (monthWeights.isNotEmpty()) monthWeights.average().toFloat() else null)

            currentMonth = currentMonth.plusMonths(1)
        }

        return TrendChartData(dates, calorieIntake, exerciseCalories, weightData)
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
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getAllRecordsOnce()
            val settings = userSettingsRepository.getSettingsOnce()
            val offset = _uiState.value.selectedMonthOffset
            val summary = StatsUtils.computeMonthSummary(foodRecords, exerciseRecords, offset, settings?.userWeight)
            _uiState.value = _uiState.value.copy(lastMonthSummary = summary)
        }
    }

    /**
     * 设置趋势分析日期范围
     */
    fun setTrendDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.value = _uiState.value.copy(
            trendStartDate = startDate,
            trendEndDate = endDate
        )
        refreshTrendData()
    }

    /**
     * 切换趋势时间维度
     */
    fun setTrendTimeDimension(dimension: TimeDimension) {
        _uiState.value = _uiState.value.copy(trendTimeDimension = dimension)
        refreshTrendData()
    }

    /**
     * 刷新趋势数据
     */
    private fun refreshTrendData() {
        viewModelScope.launch {
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getAllRecordsOnce()
            val trendData = computeTrendDataSuspend(
                foodRecords,
                exerciseRecords,
                _uiState.value.trendTimeDimension,
                _uiState.value.trendStartDate,
                _uiState.value.trendEndDate
            )
            _uiState.value = _uiState.value.copy(trendChartData = trendData)
        }
    }

    /**
     * 重置趋势日期范围
     */
    fun resetTrendDateRange() {
        _uiState.value = _uiState.value.copy(
            trendStartDate = null,
            trendEndDate = null,
            trendTimeDimension = TimeDimension.DAY
        )
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
    val trendTimeDimension: TimeDimension = TimeDimension.DAY,
    val trendChartData: TrendChartData = TrendChartData(emptyList(), emptyList(), emptyList(), emptyList()),
    val isLoading: Boolean = true
)
