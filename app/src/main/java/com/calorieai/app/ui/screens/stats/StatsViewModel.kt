package com.calorieai.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.domain.stats.StatsSnapshotUseCase
import com.calorieai.app.domain.stats.StatsTrendUseCase
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
    private val recipePlanRepository: RecipePlanRepository,
    private val statsSnapshotUseCase: StatsSnapshotUseCase,
    private val statsTrendUseCase: StatsTrendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    private var statsLoadJob: Job? = null
    private var snapshotInitialized = false
    private var latestFoodRecords: List<FoodRecord> = emptyList()
    private var latestExerciseRecords: List<ExerciseRecord> = emptyList()
    private var latestSettings: UserSettings? = null

    init {
        loadStats()
    }

    private fun loadStats() {
        statsLoadJob?.cancel()
        statsLoadJob = viewModelScope.launch {
            combine(
                foodRecordRepository.getAllRecords().distinctUntilChanged(),
                exerciseRecordRepository.getAllRecords().distinctUntilChanged(),
                weightRecordRepository.getAllRecords().distinctUntilChanged(),
                waterRecordRepository.getAllRecords().distinctUntilChanged(),
                favoriteRecipeRepository.getAllFavorites().distinctUntilChanged(),
                pantryIngredientRepository.getAll().distinctUntilChanged(),
                recipePlanRepository.getAll().distinctUntilChanged(),
                userSettingsRepository.getSettings().distinctUntilChanged()
            ) { recordsArray: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                StatsSourceBundle(
                    foodRecords = recordsArray[0] as List<FoodRecord>,
                    exerciseRecords = recordsArray[1] as List<ExerciseRecord>,
                    weightRecords = recordsArray[2] as List<WeightRecord>,
                    waterRecords = recordsArray[3] as List<WaterRecord>,
                    favoriteRecipes = recordsArray[4] as List<FavoriteRecipe>,
                    pantryIngredients = recordsArray[5] as List<PantryIngredient>,
                    recipePlans = recordsArray[6] as List<RecipePlan>,
                    settings = recordsArray[7] as UserSettings?
                )
            }.collectLatest { sources ->
                val foodRecords = sources.foodRecords
                val exerciseRecords = sources.exerciseRecords
                val weightRecords = sources.weightRecords
                val favoriteRecipes = sources.favoriteRecipes
                val pantryIngredients = sources.pantryIngredients
                val recipePlans = sources.recipePlans
                val settings = sources.settings
                latestFoodRecords = foodRecords
                latestExerciseRecords = exerciseRecords
                latestSettings = settings
                snapshotInitialized = true
                val targetCalories = settings?.dailyCalorieGoal ?: 2000
                val latestWeight = weightRecords.maxByOrNull { it.recordDate }
                val userWeight = latestWeight?.weight ?: settings?.userWeight
                val currentState = _uiState.value
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
                val weeklyGoalDays = settings?.weeklyRecordGoalDays ?: 5

                val basic = withContext(Dispatchers.Default) {
                    statsSnapshotUseCase.buildBasicSnapshot(
                        foodRecords = foodRecords,
                        exerciseRecords = exerciseRecords,
                        favoriteRecipes = favoriteRecipes,
                        pantryIngredients = pantryIngredients,
                        recipePlans = recipePlans,
                        selectedOverviewDate = currentState.selectedOverviewDate,
                        selectedMonthOffset = currentState.selectedMonthOffset,
                        targetCalories = targetCalories,
                        bmr = bmr,
                        tdee = tdee,
                        userWeight = userWeight,
                        weeklyGoalDays = weeklyGoalDays
                    )
                }

                val trendData = statsTrendUseCase.computeTrendData(
                    currentState.trendTimeDimension,
                    currentState.trendStartDate,
                    currentState.trendEndDate
                )

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
                    waterTrendData = waterMetrics.waterTrendData,
                    showWaterFeatures = settings?.showWaterFeatures ?: true
                )
            }
        }
    }

    /**
     * 鍒囨崲鏈堜唤锛堜笂鏈堟€荤粨锛?
     */
    fun changeMonth(offset: Int) {
        _uiState.value = _uiState.value.copy(selectedMonthOffset = offset)
        refreshMonthSummary()
    }

    /**
     * 鍒锋柊鏈堜唤鎬荤粨
     */
    private fun refreshMonthSummary() {
        viewModelScope.launch {
            if (!ensureSnapshotLoaded()) {
                return@launch
            }
            val foodRecords = latestFoodRecords
            val exerciseRecords = latestExerciseRecords
            val settings = latestSettings
            val offset = _uiState.value.selectedMonthOffset
            val summary = withContext(Dispatchers.Default) {
                StatsUtils.computeMonthSummary(foodRecords, exerciseRecords, offset, settings?.userWeight)
            }
            _uiState.value = _uiState.value.copy(lastMonthSummary = summary)
        }
    }

    /**
     * 璁剧疆瓒嬪娍鍒嗘瀽鏃ユ湡鑼冨洿
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
     * 鍒囨崲瓒嬪娍鏃堕棿缁村害
     */
    fun setTrendTimeDimension(dimension: TimeDimension) {
        _uiState.value = _uiState.value.copy(trendTimeDimension = dimension)
        refreshTrendData()
    }

    /**
     * 鍒锋柊瓒嬪娍鏁版嵁
     */
    private fun refreshTrendData() {
        viewModelScope.launch {
            val trendData = statsTrendUseCase.computeTrendData(
                _uiState.value.trendTimeDimension,
                _uiState.value.trendStartDate,
                _uiState.value.trendEndDate
            )
            _uiState.value = _uiState.value.copy(trendChartData = trendData)
        }
    }

    /**
     * 閲嶇疆瓒嬪娍鏃ユ湡鑼冨洿
     */
    fun resetTrendDateRange() {
        _uiState.value = _uiState.value.copy(
            trendStartDate = null,
            trendEndDate = null,
            trendTimeDimension = TimeDimension.DAY
        )
        refreshTrendData()
    }

    /**
     * 璁＄畻鍛ㄥ钩鍧囬ギ姘撮噺
     */
    private suspend fun computeWeeklyWaterAverage(): Float {
        val weeklyTotal = waterRecordRepository.getWeeklyTotalAmount()
        return weeklyTotal / 7f
    }

    /**
     * 璁＄畻楗按瓒嬪娍鏁版嵁锛堟渶杩?0澶╋級
     */
    private suspend fun computeWaterTrendData(): List<WaterTrendData> {
        val today = LocalDate.now()
        val startDate = today.minusDays(29)
        
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        val waterRecords = waterRecordRepository.getRecordsBetweenSync(startMillis, endMillis)
        
        // 鎸夋棩鏈熷垎缁勫苟姹傚拰
        val recordsByDate = waterRecords.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { (_, records) -> records.sumOf { it.amount } }
        
        // 鐢熸垚30澶╃殑鏁版嵁
        return (0 until 30).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            WaterTrendData(
                date = date,
                amount = recordsByDate[date] ?: 0
            )
        }
    }

    /**
     * 璁剧疆姒傝缁熻鏃ユ湡
     */
    fun setOverviewDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedOverviewDate = date)
        refreshOverviewStats()
    }

    /**
     * 鍒锋柊姒傝缁熻鏁版嵁
     */
    private fun refreshOverviewStats() {
        viewModelScope.launch {
            if (!ensureSnapshotLoaded()) {
                return@launch
            }
            val foodRecords = latestFoodRecords
            val exerciseRecords = latestExerciseRecords
            val settings = latestSettings
            val targetCalories = settings?.dailyCalorieGoal ?: 2000
            val selectedDate = _uiState.value.selectedOverviewDate
            val weeklyGoalDays = settings?.weeklyRecordGoalDays ?: 5
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

            val refreshBundle = withContext(Dispatchers.Default) {
                statsSnapshotUseCase.buildOverviewSnapshot(
                    foodRecords = foodRecords,
                    exerciseRecords = exerciseRecords,
                    selectedDate = selectedDate,
                    targetCalories = targetCalories,
                    bmr = bmr,
                    tdee = tdee,
                    weeklyGoalDays = weeklyGoalDays,
                    streakDays = _uiState.value.streakDays
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

    private fun ensureSnapshotLoaded(): Boolean {
        return snapshotInitialized
    }
}

private data class WaterMetrics(
    val todayWaterAmount: Int,
    val weeklyWaterAverage: Float,
    val monthlyWaterTotal: Int,
    val waterTrendData: List<WaterTrendData>
)

private data class StatsSourceBundle(
    val foodRecords: List<FoodRecord>,
    val exerciseRecords: List<ExerciseRecord>,
    val weightRecords: List<WeightRecord>,
    val waterRecords: List<WaterRecord>,
    val favoriteRecipes: List<FavoriteRecipe>,
    val pantryIngredients: List<PantryIngredient>,
    val recipePlans: List<RecipePlan>,
    val settings: UserSettings?
)

/**
 * 姣忔棩椁愭璁板綍鏁版嵁锛堢敤浜庣儹鍔涘浘锛?
 * level: 0=鏃犺褰? 1~10=璁板綍寮哄害
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
    val selectedMonthOffset: Int = 0, // 榛樿鏄剧ず褰撳墠鏈堜唤
    val trendStartDate: LocalDate? = null,
    val trendEndDate: LocalDate? = null,
    val trendTimeDimension: TimeDimension = TimeDimension.DAY,
    val trendChartData: TrendChartData = TrendChartData(emptyList(), emptyList(), emptyList(), emptyList()),
    val isLoading: Boolean = true,
    // 姒傝鏃ユ湡閫夋嫨
    val selectedOverviewDate: LocalDate = LocalDate.now(),
    // 鐢ㄦ埛韬綋鏁版嵁锛堢敤浜庤绠楄惀鍏荤礌鍙傝€冨€硷級
    val userWeight: Float = 70f,
    val userHeight: Float? = null,
    val userGender: String = "MALE",
    val userAge: Int = 30,
    val userActivityLevel: String = "MODERATE",
    // 姣忔棩椁愭璁板綍鏁版嵁锛堢敤浜庣儹鍔涘浘锛?
    val dailyMealRecords: List<DailyMealRecord> = emptyList(),
    // 璇︾粏姒傝 - 璁板綍淇℃伅琛?
    val foodRecordTableRows: List<FoodRecordTableRow> = emptyList(),
    val topFoodRows: List<TopFoodRow> = emptyList(),
    val achievementBadges: List<AchievementBadge> = emptyList(),
    val recipeStats: RecipeStats = RecipeStats(),
    // 浠婃棩楗按閲?
    val todayWaterAmount: Int = 0,
    // 鏈湀娲昏穬澶╂暟锛堟湰鏈堟湁璁板綍鐨勫ぉ鏁帮級
    val monthlyActiveDays: Int = 0,
    // 楗按鐩稿叧鏁版嵁
    val waterTargetAmount: Int = 2000, // 姣忔棩楗按鐩爣
    val weeklyWaterAverage: Float = 0f, // 鍛ㄥ钩鍧囬ギ姘撮噺
    val monthlyWaterTotal: Int = 0, // 鏈湀鎬婚ギ姘撮噺
    val waterTrendData: List<WaterTrendData> = emptyList(), // 楗按瓒嬪娍鏁版嵁
    val showWaterFeatures: Boolean = true
)

/**
 * 楗按瓒嬪娍鏁版嵁
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

