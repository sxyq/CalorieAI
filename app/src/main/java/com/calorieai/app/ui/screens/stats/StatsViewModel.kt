package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.model.getSimplifiedMealTypeName
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.ui.components.charts.TimeDimension
import com.calorieai.app.ui.components.charts.TrendChartData
import com.calorieai.app.utils.MetabolicConstants
import com.calorieai.app.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val waterRecordRepository: com.calorieai.app.data.repository.WaterRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val pantryIngredientRepository: PantryIngredientRepository,
    private val recipePlanRepository: RecipePlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    private var statsLoadJob: Job? = null

    init {
        loadStats()
    }

    private fun loadStats() {
        statsLoadJob?.cancel()
        statsLoadJob = viewModelScope.launch {
            combine(
                foodRecordRepository.getAllRecords(),
                exerciseRecordRepository.getAllRecords(),
                weightRecordRepository.getAllRecords(),
                waterRecordRepository.getAllRecords(),
                favoriteRecipeRepository.getAllFavorites(),
                pantryIngredientRepository.getAll(),
                recipePlanRepository.getAll()
            ) { recordsArray: Array<List<Any>> ->
                @Suppress("UNCHECKED_CAST")
                StatsSourceBundle(
                    foodRecords = recordsArray[0] as List<FoodRecord>,
                    exerciseRecords = recordsArray[1] as List<ExerciseRecord>,
                    weightRecords = recordsArray[2] as List<WeightRecord>,
                    waterRecords = recordsArray[3] as List<WaterRecord>,
                    favoriteRecipes = recordsArray[4] as List<FavoriteRecipe>,
                    pantryIngredients = recordsArray[5] as List<PantryIngredient>,
                    recipePlans = recordsArray[6] as List<RecipePlan>
                )
            }.collectLatest { sources ->
                val foodRecords = sources.foodRecords
                val exerciseRecords = sources.exerciseRecords
                val weightRecords = sources.weightRecords
                val waterRecords = sources.waterRecords
                val favoriteRecipes = sources.favoriteRecipes
                val pantryIngredients = sources.pantryIngredients
                val recipePlans = sources.recipePlans
                val settings = userSettingsRepository.getSettingsOnce()
                val targetCalories = settings?.dailyCalorieGoal ?: 2000
                val latestWeight = weightRecords.maxByOrNull { it.recordDate }
                val userWeight = latestWeight?.weight ?: settings?.userWeight
                val currentState = _uiState.value

                val basic = withContext(Dispatchers.Default) {
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

                    val todayStats = StatsUtils.computeTodayStats(foodRecords, exerciseRecords, targetCalories, bmr, tdee)
                    val mealTypeStats = StatsUtils.computeMealTypeStats(foodRecords)
                    val historyStats = StatsUtils.computeHistoryStats(foodRecords)
                    val weeklyStats = StatsUtils.computeWeeklyTrend(foodRecords)
                    val monthlyStats = StatsUtils.computeMonthlyTrend(foodRecords)
                    val streakDays = StatsUtils.computeStreakDays(foodRecords)
                    val weeklyGoalDays = settings?.weeklyRecordGoalDays ?: 5
                    val weeklyActiveDays = computeWeeklyActiveDays(foodRecords)
                    val weeklyRecordCount = computeWeeklyRecordCount(foodRecords)
                    val summary = StatsUtils.computeMonthSummary(foodRecords, exerciseRecords, currentState.selectedMonthOffset, userWeight)
                    val dailyMealRecords = computeDailyMealRecords(
                        foodRecords = foodRecords,
                        exerciseRecords = exerciseRecords,
                        waterRecords = waterRecords,
                        weightRecords = weightRecords
                    )
                    val monthlyActiveDays = computeMonthlyActiveDays(dailyMealRecords)
                    val foodRecordTableRows = computeFoodRecordTableRows(foodRecords, currentState.selectedOverviewDate)
                    val topFoodRows = computeTopFoodRows(foodRecords, 14)
                    val achievementBadges = computeAchievementBadges(
                        streakDays = streakDays,
                        weeklyActiveDays = weeklyActiveDays,
                        weeklyGoalDays = weeklyGoalDays,
                        weeklyRecordCount = weeklyRecordCount
                    )
                    val recipeStats = computeRecipeStats(
                        favorites = favoriteRecipes,
                        pantryIngredients = pantryIngredients,
                        plans = recipePlans
                    )

                    BasicStatsBundle(
                        todayStats = todayStats,
                        mealTypeStats = mealTypeStats,
                        historyStats = historyStats,
                        weeklyStats = weeklyStats,
                        monthlyStats = monthlyStats,
                        streakDays = streakDays,
                        weeklyGoalDays = weeklyGoalDays,
                        weeklyActiveDays = weeklyActiveDays,
                        weeklyRecordCount = weeklyRecordCount,
                        monthSummary = summary,
                        dailyMealRecords = dailyMealRecords,
                        monthlyActiveDays = monthlyActiveDays,
                        foodRecordTableRows = foodRecordTableRows,
                        topFoodRows = topFoodRows,
                        achievementBadges = achievementBadges,
                        recipeStats = recipeStats
                    )
                }

                val trendData = withContext(Dispatchers.Default) {
                    computeTrendData(
                        foodRecords,
                        exerciseRecords,
                        currentState.trendTimeDimension,
                        currentState.trendStartDate,
                        currentState.trendEndDate
                    )
                }

                val waterMetrics = withContext(Dispatchers.IO) {
                    WaterMetrics(
                        todayWaterAmount = waterRecordRepository.getTodayTotalAmount(),
                        weeklyWaterAverage = computeWeeklyWaterAverage(),
                        monthlyWaterTotal = waterRecordRepository.getMonthlyTotalAmount(),
                        waterTrendData = computeWaterTrendData()
                    )
                }

                _uiState.value = _uiState.value.copy(
                    todayStats = basic.todayStats,
                    mealTypeStats = basic.mealTypeStats,
                    historyStats = basic.historyStats,
                    weeklyStats = basic.weeklyStats,
                    monthlyStats = basic.monthlyStats,
                    lastMonthSummary = basic.monthSummary,
                    streakDays = basic.streakDays,
                    weeklyGoalDays = basic.weeklyGoalDays,
                    weeklyActiveDays = basic.weeklyActiveDays,
                    weeklyRecordCount = basic.weeklyRecordCount,
                    trendChartData = trendData,
                    isLoading = false,
                    userWeight = userWeight ?: 70f,
                    userHeight = settings?.userHeight,
                    userGender = settings?.userGender ?: "MALE",
                    userAge = settings?.userAge ?: 30,
                    userActivityLevel = settings?.activityLevel ?: "MODERATE",
                    dailyMealRecords = basic.dailyMealRecords,
                    monthlyActiveDays = basic.monthlyActiveDays,
                    foodRecordTableRows = basic.foodRecordTableRows,
                    topFoodRows = basic.topFoodRows,
                    achievementBadges = basic.achievementBadges,
                    recipeStats = basic.recipeStats,
                    todayWaterAmount = waterMetrics.todayWaterAmount,
                    waterTargetAmount = settings?.dailyWaterGoal ?: 2000,
                    weeklyWaterAverage = waterMetrics.weeklyWaterAverage,
                    monthlyWaterTotal = waterMetrics.monthlyWaterTotal,
                    waterTrendData = waterMetrics.waterTrendData
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
        val rawEndDate = endDate ?: today
        val actualEndDate = if (rawEndDate.isAfter(today)) today else rawEndDate
        val rawStartDate = startDate ?: today.minusDays(30)
        val actualStartDate = if (rawStartDate.isAfter(actualEndDate)) actualEndDate else rawStartDate

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
            val summary = withContext(Dispatchers.Default) {
                StatsUtils.computeMonthSummary(foodRecords, exerciseRecords, offset, settings?.userWeight)
            }
            _uiState.value = _uiState.value.copy(lastMonthSummary = summary)
        }
    }

    /**
     * 设置趋势分析日期范围
     */
    fun setTrendDateRange(startDate: LocalDate, endDate: LocalDate) {
        val today = LocalDate.now()
        val safeEndDate = if (endDate.isAfter(today)) today else endDate
        val safeStartDate = if (startDate.isAfter(safeEndDate)) safeEndDate else startDate
        _uiState.value = _uiState.value.copy(
            trendStartDate = safeStartDate,
            trendEndDate = safeEndDate
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
            val trendData = withContext(Dispatchers.Default) {
                computeTrendData(
                    foodRecords,
                    exerciseRecords,
                    _uiState.value.trendTimeDimension,
                    _uiState.value.trendStartDate,
                    _uiState.value.trendEndDate
                )
            }
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
     * level: 0=无记录, 1~10=记录强度（优先按真实记录条数映射）
     */
    @Suppress("UNUSED_PARAMETER")
    private fun computeDailyMealRecords(
        foodRecords: List<com.calorieai.app.data.model.FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        waterRecords: List<WaterRecord>,
        weightRecords: List<WeightRecord>
    ): List<DailyMealRecord> {
        val today = LocalDate.now()
        val daysToShow = 140 // 20周
        val startDate = today.minusDays(daysToShow.toLong() - 1)
        
        // 食物记录按日期分组
        val foodRecordsByDate = foodRecords.groupBy {
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        return (0 until daysToShow).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayRecords = foodRecordsByDate[date] ?: emptyList()
            
            // 获取该日记录的所有餐次类型（去重）
            val mealTypes = dayRecords.map { it.mealType }.toSet()
            
            // 强度完全基于“食物记录真实提交条数”。
            val level = when {
                dayRecords.isEmpty() -> 0
                else -> {
                    val foodCount = dayRecords.size
                    val baseScore = when {
                        foodCount <= 0 -> 1
                        foodCount >= 10 -> 10
                        else -> foodCount
                    }
                    baseScore.coerceIn(1, 10)
                }
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
     * 计算本周活跃记录天数（按自然周：周一至周日）
     */
    private fun computeWeeklyActiveDays(foodRecords: List<FoodRecord>): Int {
        val today = LocalDate.now()
        val weekStart = today.with(WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1)
        val weekEnd = weekStart.plusDays(6)
        return foodRecords
            .map {
                java.time.Instant.ofEpochMilli(it.recordTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .filter { it in weekStart..weekEnd }
            .distinct()
            .size
    }

    /**
     * 计算本周记录条数
     */
    private fun computeWeeklyRecordCount(foodRecords: List<FoodRecord>): Int {
        val today = LocalDate.now()
        val weekStart = today.with(WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1)
        val weekEnd = weekStart.plusDays(6)
        return foodRecords.count {
            val date = java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date in weekStart..weekEnd
        }
    }

    /**
     * 按餐次生成当日记录信息表
     */
    private fun computeFoodRecordTableRows(
        foodRecords: List<FoodRecord>,
        date: LocalDate
    ): List<FoodRecordTableRow> {
        val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return foodRecords
            .filter { it.recordTime in dayStart..dayEnd }
            .groupBy { getSimplifiedMealTypeName(it.mealType) }
            .map { (mealType, records) ->
                FoodRecordTableRow(
                    mealType = mealType,
                    count = records.size,
                    calories = records.sumOf { it.totalCalories },
                    protein = records.sumOf { it.protein.toDouble() }.toFloat(),
                    carbs = records.sumOf { it.carbs.toDouble() }.toFloat(),
                    fat = records.sumOf { it.fat.toDouble() }.toFloat()
                )
            }
            .sortedByDescending { it.calories }
    }

    /**
     * 最近N天高频食物表
     */
    private fun computeTopFoodRows(
        foodRecords: List<FoodRecord>,
        recentDays: Long
    ): List<TopFoodRow> {
        val today = LocalDate.now()
        val start = today.minusDays(recentDays - 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return foodRecords
            .filter { it.recordTime >= start }
            .groupBy { it.foodName.trim() }
            .map { (foodName, records) ->
                val latest = records.maxByOrNull { it.recordTime }?.recordTime ?: 0L
                TopFoodRow(
                    foodName = foodName,
                    count = records.size,
                    totalCalories = records.sumOf { it.totalCalories },
                    lastRecordDate = java.time.Instant.ofEpochMilli(latest)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
            }
            .sortedWith(compareByDescending<TopFoodRow> { it.count }.thenByDescending { it.totalCalories })
            .take(6)
    }

    /**
     * 连续打卡激励：周目标 + 连续天数 + 成就体系
     */
    private fun computeAchievementBadges(
        streakDays: Int,
        weeklyActiveDays: Int,
        weeklyGoalDays: Int,
        weeklyRecordCount: Int
    ): List<AchievementBadge> {
        val goalProgress = "$weeklyActiveDays/$weeklyGoalDays 天"
        val streakProgress = "$streakDays 天"
        val recordProgress = "$weeklyRecordCount 条"

        return listOf(
            AchievementBadge(
                title = "周目标打卡",
                achieved = weeklyActiveDays >= weeklyGoalDays,
                progress = goalProgress
            ),
            AchievementBadge(
                title = "连续打卡 7 天",
                achieved = streakDays >= 7,
                progress = streakProgress
            ),
            AchievementBadge(
                title = "本周记录达人",
                achieved = weeklyRecordCount >= 21,
                progress = recordProgress
            )
        )
    }

    private fun computeRecipeStats(
        favorites: List<FavoriteRecipe>,
        pantryIngredients: List<PantryIngredient>,
        plans: List<RecipePlan>
    ): RecipeStats {
        val now = System.currentTimeMillis()
        val threeDaysMillis = 3L * 24L * 60L * 60L * 1000L
        val expiringSoon = pantryIngredients.count { item ->
            val expiresAt = item.expiresAt ?: return@count false
            expiresAt in now..(now + threeDaysMillis)
        }

        val totalUseCount = favorites.sumOf { it.useCount }
        val usedFavoritesCount = favorites.count { it.useCount > 0 }
        val mostUsedFavorite = favorites.maxByOrNull { it.useCount }

        return RecipeStats(
            pantryCount = pantryIngredients.size,
            pantryExpiringSoonCount = expiringSoon,
            favoriteCount = favorites.size,
            usedFavoriteCount = usedFavoritesCount,
            favoriteUseCount = totalUseCount,
            mostUsedFavoriteName = mostUsedFavorite?.foodName,
            mostUsedFavoriteUseCount = mostUsedFavorite?.useCount ?: 0,
            recipePlanCount = plans.size
        )
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

            val refreshBundle = withContext(Dispatchers.Default) {
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

                val mealTypeStats = dayFoodRecords
                    .groupBy { it.mealType }
                    .mapValues { (_, records) -> records.sumOf { it.totalCalories } }
                val weeklyGoalDays = settings?.weeklyRecordGoalDays ?: 5
                val weeklyActiveDays = computeWeeklyActiveDays(foodRecords)
                val weeklyRecordCount = computeWeeklyRecordCount(foodRecords)
                val badges = computeAchievementBadges(
                    streakDays = _uiState.value.streakDays,
                    weeklyActiveDays = weeklyActiveDays,
                    weeklyGoalDays = weeklyGoalDays,
                    weeklyRecordCount = weeklyRecordCount
                )

                OverviewRefreshBundle(
                    todayStats = todayStats,
                    mealTypeStats = mealTypeStats,
                    weeklyGoalDays = weeklyGoalDays,
                    weeklyActiveDays = weeklyActiveDays,
                    weeklyRecordCount = weeklyRecordCount,
                    foodRecordTableRows = computeFoodRecordTableRows(foodRecords, selectedDate),
                    topFoodRows = computeTopFoodRows(foodRecords, 14),
                    achievementBadges = badges
                )
            }

            _uiState.value = _uiState.value.copy(
                todayStats = refreshBundle.todayStats,
                mealTypeStats = refreshBundle.mealTypeStats,
                weeklyGoalDays = refreshBundle.weeklyGoalDays,
                weeklyActiveDays = refreshBundle.weeklyActiveDays,
                weeklyRecordCount = refreshBundle.weeklyRecordCount,
                foodRecordTableRows = refreshBundle.foodRecordTableRows,
                topFoodRows = refreshBundle.topFoodRows,
                achievementBadges = refreshBundle.achievementBadges
            )
        }
    }
}

private data class BasicStatsBundle(
    val todayStats: TodayStats,
    val mealTypeStats: Map<MealType, Int>,
    val historyStats: HistoryStats,
    val weeklyStats: List<WeeklyStat>,
    val monthlyStats: List<MonthlyStat>,
    val streakDays: Int,
    val weeklyGoalDays: Int,
    val weeklyActiveDays: Int,
    val weeklyRecordCount: Int,
    val monthSummary: MonthSummary,
    val dailyMealRecords: List<DailyMealRecord>,
    val monthlyActiveDays: Int,
    val foodRecordTableRows: List<FoodRecordTableRow>,
    val topFoodRows: List<TopFoodRow>,
    val achievementBadges: List<AchievementBadge>,
    val recipeStats: RecipeStats
)

private data class WaterMetrics(
    val todayWaterAmount: Int,
    val weeklyWaterAverage: Float,
    val monthlyWaterTotal: Int,
    val waterTrendData: List<WaterTrendData>
)

private data class OverviewRefreshBundle(
    val todayStats: TodayStats,
    val mealTypeStats: Map<MealType, Int>,
    val weeklyGoalDays: Int,
    val weeklyActiveDays: Int,
    val weeklyRecordCount: Int,
    val foodRecordTableRows: List<FoodRecordTableRow>,
    val topFoodRows: List<TopFoodRow>,
    val achievementBadges: List<AchievementBadge>
)

private data class StatsSourceBundle(
    val foodRecords: List<FoodRecord>,
    val exerciseRecords: List<ExerciseRecord>,
    val weightRecords: List<WeightRecord>,
    val waterRecords: List<WaterRecord>,
    val favoriteRecipes: List<FavoriteRecipe>,
    val pantryIngredients: List<PantryIngredient>,
    val recipePlans: List<RecipePlan>
)

/**
 * 每日餐次记录数据（用于热力图）
 * level: 0=无记录, 1~10=记录强度
 */
data class DailyMealRecord(
    val date: LocalDate,
    val level: Int, // 0-10
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
    val weeklyGoalDays: Int = 5,
    val weeklyActiveDays: Int = 0,
    val weeklyRecordCount: Int = 0,
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
    val userHeight: Float? = null,
    val userGender: String = "MALE",
    val userAge: Int = 30,
    val userActivityLevel: String = "MODERATE",
    // 每日餐次记录数据（用于热力图）
    val dailyMealRecords: List<DailyMealRecord> = emptyList(),
    // 详细概览 - 记录信息表
    val foodRecordTableRows: List<FoodRecordTableRow> = emptyList(),
    val topFoodRows: List<TopFoodRow> = emptyList(),
    val achievementBadges: List<AchievementBadge> = emptyList(),
    val recipeStats: RecipeStats = RecipeStats(),
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

data class FoodRecordTableRow(
    val mealType: String,
    val count: Int,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

data class TopFoodRow(
    val foodName: String,
    val count: Int,
    val totalCalories: Int,
    val lastRecordDate: LocalDate
)

data class AchievementBadge(
    val title: String,
    val achieved: Boolean,
    val progress: String
)

data class RecipeStats(
    val pantryCount: Int = 0,
    val pantryExpiringSoonCount: Int = 0,
    val favoriteCount: Int = 0,
    val usedFavoriteCount: Int = 0,
    val favoriteUseCount: Int = 0,
    val mostUsedFavoriteName: String? = null,
    val mostUsedFavoriteUseCount: Int = 0,
    val recipePlanCount: Int = 0
)
