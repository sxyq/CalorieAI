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
import com.calorieai.app.utils.MetabolicConstants
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
    private val weightRecordRepository: WeightRecordRepository,
    private val waterRecordRepository: com.calorieai.app.data.repository.WaterRecordRepository
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
                Triple(foodRecords, exerciseRecords, latestWeight)
            }.collect { (foodRecords, exerciseRecords, latestWeight) ->
                val settings = userSettingsRepository.getSettingsOnce()
                val targetCalories = settings?.dailyCalorieGoal ?: 2000
                val userWeight = latestWeight?.weight ?: settings?.userWeight
                
                // 计算 BMR 和 TDEE
                val bmr = if (settings != null && userWeight != null) {
                    MetabolicConstants.calculateBMR(
                        gender = settings.userGender ?: "MALE",
                        weight = userWeight,
                        height = settings.userHeight,
                        age = settings.userAge
                    )
                } else 0
                val tdee = if (bmr > 0 && settings != null) {
                    MetabolicConstants.calculateTDEE(bmr, settings.activityLevel)
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

                // 计算每日餐次记录数据（用于热力图）
                val dailyMealRecords = computeDailyMealRecords(foodRecords)

                // 计算统一趋势数据
                val trendData = computeTrendData(
                    foodRecords,
                    exerciseRecords,
                    _uiState.value.trendTimeDimension,
                    _uiState.value.trendStartDate,
                    _uiState.value.trendEndDate
                )

                _uiState.value = _uiState.value.copy(
                    todayStats = todayStats,
                    mealTypeStats = mealTypeStats,
                    historyStats = historyStats,
                    weeklyStats = weeklyStats,
                    monthlyStats = monthlyStats,
                    lastMonthSummary = summary,
                    streakDays = streakDays,
                    trendChartData = trendData,
                    isLoading = false,
                    // 更新用户身体数据
                    userWeight = userWeight ?: 70f,
                    userGender = settings?.userGender ?: "MALE",
                    userAge = settings?.userAge ?: 30,
                    userActivityLevel = settings?.activityLevel ?: "MODERATE",
                    // 每日餐次记录数据
                    dailyMealRecords = dailyMealRecords,
                    // 本月活跃天数（本月有记录的天数）
                    monthlyActiveDays = computeMonthlyActiveDays(dailyMealRecords),
                    // 饮水相关数据
                    todayWaterAmount = waterRecordRepository.getTodayTotalAmount(),
                    waterTargetAmount = settings?.dailyWaterGoal ?: 2000,
                    weeklyWaterAverage = computeWeeklyWaterAverage(),
                    monthlyWaterTotal = waterRecordRepository.getMonthlyTotalAmount(),
                    waterTrendData = computeWaterTrendData()
                )
            }
        }
    }

    /**
     * 计算趋势图表数据
     */
    private suspend fun computeTrendData(
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
            java.time.Instant.ofEpochMilli(it.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
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
            val trendData = computeTrendData(
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

    /**
     * 计算每日餐次记录数据（用于热力图）
     * 返回最近20周（140天）的每日餐次记录情况
     * level: 0=无记录, 1=1个餐次, 2=2个餐次, 3=3个餐次, 4=4个及以上餐次
     */
    private fun computeDailyMealRecords(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>
    ): List<DailyMealRecord> {
        val today = LocalDate.now()
        val daysToShow = 140 // 20周
        
        // 从今天往前推 139 天，然后对齐到周日（一周的开始）
        val rawStartDate = today.minusDays(139)
        val dayOfWeek = rawStartDate.dayOfWeek.value % 7
        val startDate = rawStartDate.minusDays(dayOfWeek.toLong())
        
        // 按日期分组记录
        val recordsByDate = foodRecords.groupBy { 
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        
        return (0 until daysToShow).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayRecords = recordsByDate[date] ?: emptyList()
            
            // 获取该日记录的所有餐次类型（去重）
            val mealTypes = dayRecords.map { it.mealType }.toSet()
            
            // 根据餐次数量确定等级
            val level = when {
                dayRecords.isEmpty() -> 0
                mealTypes.size == 1 -> 1
                mealTypes.size == 2 -> 2
                mealTypes.size == 3 -> 3
                else -> 4
            }
            
            DailyMealRecord(
                date = date,
                level = level,
                mealTypes = mealTypes
            )
        }
    }

    /**
     * 计算本月活跃天数（本月有记录的天数）
     */
    private fun computeMonthlyActiveDays(dailyMealRecords: List<DailyMealRecord>): Int {
        val today = LocalDate.now()
        val currentMonth = today.monthValue
        val currentYear = today.year
        
        return dailyMealRecords.count { record ->
            record.date.monthValue == currentMonth &&
            record.date.year == currentYear &&
            record.level > 0
        }
    }

    /**
     * 计算周平均饮水量
     */
    private suspend fun computeWeeklyWaterAverage(): Float {
        val weeklyTotal = waterRecordRepository.getWeeklyTotalAmount()
        return weeklyTotal / 7f
    }

    /**
     * 计算饮水趋势数据（最近30天）
     */
    private suspend fun computeWaterTrendData(): List<WaterTrendData> {
        val today = LocalDate.now()
        val startDate = today.minusDays(29)
        
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        val waterRecords = waterRecordRepository.getRecordsBetweenSync(startMillis, endMillis)
        
        // 按日期分组并求和
        val recordsByDate = waterRecords.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { (_, records) -> records.sumOf { it.amount } }
        
        // 生成30天的数据
        return (0 until 30).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            WaterTrendData(
                date = date,
                amount = recordsByDate[date] ?: 0
            )
        }
    }

    /**
     * 设置概览统计日期
     */
    fun setOverviewDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedOverviewDate = date)
        refreshOverviewStats()
    }

    /**
     * 刷新概览统计数据
     */
    private fun refreshOverviewStats() {
        viewModelScope.launch {
            val foodRecords = foodRecordRepository.getAllRecordsOnce()
            val exerciseRecords = exerciseRecordRepository.getAllRecordsOnce()
            val settings = userSettingsRepository.getSettingsOnce()
            val targetCalories = settings?.dailyCalorieGoal ?: 2000
            val selectedDate = _uiState.value.selectedOverviewDate

            // 计算选中日期的统计数据
            val dayStart = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            val dayFoodRecords = foodRecords.filter { it.recordTime in dayStart..dayEnd }
            val dayExerciseRecords = exerciseRecords.filter {
                java.time.Instant.ofEpochMilli(it.recordTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() == selectedDate
            }

            val bmr = if (settings != null) {
                MetabolicConstants.calculateBMR(
                    gender = settings.userGender ?: "MALE",
                    weight = settings.userWeight,
                    height = settings.userHeight,
                    age = settings.userAge
                )
            } else 0
            val tdee = if (bmr > 0 && settings != null) {
                MetabolicConstants.calculateTDEE(bmr, settings.activityLevel)
            } else 0

            // 手动构建TodayStats
            val totalCalories = dayFoodRecords.sumOf { it.totalCalories }
            val todayStats = TodayStats(
                date = selectedDate,
                totalCalories = totalCalories,
                targetCalories = targetCalories,
                remainingCalories = targetCalories - totalCalories,
                isTargetMet = totalCalories <= targetCalories,
                recordCount = dayFoodRecords.size,
                proteinGrams = dayFoodRecords.sumOf { it.protein.toDouble() }.toFloat(),
                carbsGrams = dayFoodRecords.sumOf { it.carbs.toDouble() }.toFloat(),
                fatGrams = dayFoodRecords.sumOf { it.fat.toDouble() }.toFloat(),
                fiberGrams = dayFoodRecords.sumOf { it.fiber.toDouble() }.toFloat(),
                sugarGrams = dayFoodRecords.sumOf { it.sugar.toDouble() }.toFloat(),
                sodiumMg = dayFoodRecords.sumOf { it.sodium.toDouble() }.toFloat(),
                cholesterolMg = dayFoodRecords.sumOf { it.cholesterol.toDouble() }.toFloat(),
                saturatedFatGrams = dayFoodRecords.sumOf { it.saturatedFat.toDouble() }.toFloat(),
                calciumMg = dayFoodRecords.sumOf { it.calcium.toDouble() }.toFloat(),
                ironMg = dayFoodRecords.sumOf { it.iron.toDouble() }.toFloat(),
                vitaminCMg = dayFoodRecords.sumOf { it.vitaminC.toDouble() }.toFloat(),
                vitaminAMcg = dayFoodRecords.sumOf { it.vitaminA.toDouble() }.toFloat(),
                potassiumMg = dayFoodRecords.sumOf { it.potassium.toDouble() }.toFloat(),
                exerciseCalories = dayExerciseRecords.sumOf { it.caloriesBurned },
                exerciseMinutes = dayExerciseRecords.sumOf { it.durationMinutes },
                exerciseCount = dayExerciseRecords.size,
                bmr = bmr,
                tdee = tdee
            )

            // 计算餐次统计
            val mealTypeStats = dayFoodRecords
                .groupBy { it.mealType }
                .mapValues { (_, records) -> records.sumOf { it.totalCalories } }

            _uiState.value = _uiState.value.copy(
                todayStats = todayStats,
                mealTypeStats = mealTypeStats
            )
        }
    }
}

/**
 * 每日餐次记录数据（用于热力图）
 * level: 0=无记录, 1=1个餐次, 2=2个餐次, 3=3个餐次, 4=4个及以上餐次
 */
data class DailyMealRecord(
    val date: LocalDate,
    val level: Int, // 0-4
    val mealTypes: Set<MealType> = emptySet()
)

data class StatsUiState(
    val todayStats: TodayStats? = null,
    val mealTypeStats: Map<MealType, Int> = emptyMap(),
    val historyStats: HistoryStats? = null,
    val weeklyStats: List<WeeklyStat> = emptyList(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val lastMonthSummary: MonthSummary? = null,
    val streakDays: Int = 0,
    val selectedMonthOffset: Int = 0, // 默认显示当前月份
    val trendStartDate: LocalDate? = null,
    val trendEndDate: LocalDate? = null,
    val trendTimeDimension: TimeDimension = TimeDimension.DAY,
    val trendChartData: TrendChartData = TrendChartData(emptyList(), emptyList(), emptyList(), emptyList()),
    val isLoading: Boolean = true,
    // 概览日期选择
    val selectedOverviewDate: LocalDate = LocalDate.now(),
    // 用户身体数据（用于计算营养素参考值）
    val userWeight: Float = 70f,
    val userGender: String = "MALE",
    val userAge: Int = 30,
    val userActivityLevel: String = "MODERATE",
    // 每日餐次记录数据（用于热力图）
    val dailyMealRecords: List<DailyMealRecord> = emptyList(),
    // 今日饮水量
    val todayWaterAmount: Int = 0,
    // 本月活跃天数（本月有记录的天数）
    val monthlyActiveDays: Int = 0,
    // 饮水相关数据
    val waterTargetAmount: Int = 2000, // 每日饮水目标
    val weeklyWaterAverage: Float = 0f, // 周平均饮水量
    val monthlyWaterTotal: Int = 0, // 本月总饮水量
    val waterTrendData: List<WaterTrendData> = emptyList() // 饮水趋势数据
)

/**
 * 饮水趋势数据
 */
data class WaterTrendData(
    val date: LocalDate,
    val amount: Int
)
