package com.calorieai.app.domain.stats

import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.getSimplifiedMealTypeName
import com.calorieai.app.ui.screens.stats.AchievementBadge
import com.calorieai.app.ui.screens.stats.DailyMealRecord
import com.calorieai.app.ui.screens.stats.FoodRecordTableRow
import com.calorieai.app.ui.screens.stats.RecipeStats
import com.calorieai.app.ui.screens.stats.TopFoodRow
import com.calorieai.app.utils.HistoryStats
import com.calorieai.app.utils.MonthSummary
import com.calorieai.app.utils.MonthlyStat
import com.calorieai.app.utils.StatsUtils
import com.calorieai.app.utils.TodayStats
import com.calorieai.app.utils.WeeklyStat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

@Singleton
class StatsSnapshotUseCase @Inject constructor() {
    fun buildBasicSnapshot(
        foodRecords: List<FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        favoriteRecipes: List<FavoriteRecipe>,
        pantryIngredients: List<PantryIngredient>,
        recipePlans: List<RecipePlan>,
        selectedOverviewDate: LocalDate,
        selectedMonthOffset: Int,
        targetCalories: Int,
        bmr: Int,
        tdee: Int,
        userWeight: Float?,
        weeklyGoalDays: Int
    ): StatsBasicSnapshot {
        val indexed = indexFoodRecordsByDate(foodRecords)
        val weeklyActiveDays = computeWeeklyActiveDays(indexed)
        val weeklyRecordCount = computeWeeklyRecordCount(indexed)
        val streakDays = StatsUtils.computeStreakDays(foodRecords)
        val dailyMealRecords = computeDailyMealRecords(indexed)

        return StatsBasicSnapshot(
            todayStats = StatsUtils.computeTodayStats(
                foodRecords = foodRecords,
                exerciseRecords = exerciseRecords,
                targetCalories = targetCalories,
                bmr = bmr,
                tdee = tdee
            ),
            mealTypeStats = StatsUtils.computeMealTypeStats(foodRecords),
            historyStats = StatsUtils.computeHistoryStats(foodRecords),
            weeklyStats = StatsUtils.computeWeeklyTrend(foodRecords),
            monthlyStats = StatsUtils.computeMonthlyTrend(foodRecords),
            streakDays = streakDays,
            weeklyGoalDays = weeklyGoalDays,
            weeklyActiveDays = weeklyActiveDays,
            weeklyRecordCount = weeklyRecordCount,
            monthSummary = StatsUtils.computeMonthSummary(
                foodRecords = foodRecords,
                exerciseRecords = exerciseRecords,
                offset = selectedMonthOffset,
                currentWeight = userWeight
            ),
            dailyMealRecords = dailyMealRecords,
            monthlyActiveDays = computeMonthlyActiveDays(dailyMealRecords),
            foodRecordTableRows = buildFoodRecordTableRows(indexed[selectedOverviewDate].orEmpty()),
            topFoodRows = computeTopFoodRows(foodRecords, recentDays = 14),
            achievementBadges = computeAchievementBadges(
                streakDays = streakDays,
                weeklyActiveDays = weeklyActiveDays,
                weeklyGoalDays = weeklyGoalDays,
                weeklyRecordCount = weeklyRecordCount
            ),
            recipeStats = computeRecipeStats(
                favorites = favoriteRecipes,
                pantryIngredients = pantryIngredients,
                plans = recipePlans
            )
        )
    }

    fun buildOverviewSnapshot(
        foodRecords: List<FoodRecord>,
        exerciseRecords: List<ExerciseRecord>,
        selectedDate: LocalDate,
        targetCalories: Int,
        bmr: Int,
        tdee: Int,
        weeklyGoalDays: Int,
        streakDays: Int
    ): StatsOverviewSnapshot {
        val indexed = indexFoodRecordsByDate(foodRecords)
        val dayFoodRecords = indexed[selectedDate].orEmpty()
        val zoneId = ZoneId.systemDefault()
        val dayExerciseRecords = exerciseRecords.filter {
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(zoneId)
                .toLocalDate() == selectedDate
        }
        val weeklyActiveDays = computeWeeklyActiveDays(indexed)
        val weeklyRecordCount = computeWeeklyRecordCount(indexed)

        val todayStats = TodayStats(
            date = selectedDate,
            totalCalories = dayFoodRecords.sumOf { it.totalCalories },
            targetCalories = targetCalories,
            remainingCalories = targetCalories - dayFoodRecords.sumOf { it.totalCalories },
            isTargetMet = dayFoodRecords.sumOf { it.totalCalories } <= targetCalories,
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

        return StatsOverviewSnapshot(
            todayStats = todayStats,
            mealTypeStats = mealTypeStats,
            weeklyGoalDays = weeklyGoalDays,
            weeklyActiveDays = weeklyActiveDays,
            weeklyRecordCount = weeklyRecordCount,
            foodRecordTableRows = buildFoodRecordTableRows(dayFoodRecords),
            topFoodRows = computeTopFoodRows(foodRecords, recentDays = 14),
            achievementBadges = computeAchievementBadges(
                streakDays = streakDays,
                weeklyActiveDays = weeklyActiveDays,
                weeklyGoalDays = weeklyGoalDays,
                weeklyRecordCount = weeklyRecordCount
            )
        )
    }

    private fun indexFoodRecordsByDate(records: List<FoodRecord>): Map<LocalDate, List<FoodRecord>> {
        val zoneId = ZoneId.systemDefault()
        return records.groupBy {
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(zoneId)
                .toLocalDate()
        }
    }

    private fun computeDailyMealRecords(
        recordsByDate: Map<LocalDate, List<FoodRecord>>
    ): List<DailyMealRecord> {
        val today = LocalDate.now()
        val defaultStartDate = today.withDayOfMonth(1)
        val startDateCandidate = recordsByDate.keys.minOrNull()?.withDayOfMonth(1) ?: defaultStartDate
        val startDate = if (startDateCandidate.isAfter(today)) defaultStartDate else startDateCandidate
        val daysToShow = ChronoUnit.DAYS.between(startDate, today).toInt().coerceAtLeast(0) + 1

        data class DayIntensity(
            val date: LocalDate,
            val mealTypes: Set<MealType>,
            val recordCount: Int,
            val totalCalories: Int
        )

        val intensityItems = (0 until daysToShow).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val dayRecords = recordsByDate[date].orEmpty()
            DayIntensity(
                date = date,
                mealTypes = dayRecords.map { it.mealType }.toSet(),
                recordCount = dayRecords.size,
                totalCalories = dayRecords.sumOf { it.totalCalories }
            )
        }

        val activeItems = intensityItems.filter { it.recordCount > 0 }
        if (activeItems.isEmpty()) {
            return intensityItems.map { item ->
                DailyMealRecord(date = item.date, level = 0, mealTypes = item.mealTypes)
            }
        }

        val maxRecordCount = activeItems.maxOf { it.recordCount }.coerceAtLeast(1)
        val maxCalories = activeItems.maxOf { it.totalCalories }.coerceAtLeast(1)
        val rawIntensityValues = activeItems.map { item ->
            val countRatio = item.recordCount.toFloat() / maxRecordCount.toFloat()
            val caloriesRatio = item.totalCalories.toFloat() / maxCalories.toFloat()
            caloriesRatio * 0.7f + countRatio * 0.3f
        }.sorted()

        fun levelFor(raw: Float): Int {
            val idx = rawIntensityValues.upperBound(raw)
            val denominator = (rawIntensityValues.size - 1).coerceAtLeast(1)
            val ratio = idx.toFloat() / denominator.toFloat()
            return (1 + floor(ratio * 9f).toInt()).coerceIn(1, 10)
        }

        val rawByDate = activeItems.associate { item ->
            val countRatio = item.recordCount.toFloat() / maxRecordCount.toFloat()
            val caloriesRatio = item.totalCalories.toFloat() / maxCalories.toFloat()
            item.date to (caloriesRatio * 0.7f + countRatio * 0.3f)
        }

        return intensityItems.map { item ->
            val level = rawByDate[item.date]?.let(::levelFor) ?: 0
            DailyMealRecord(
                date = item.date,
                level = level,
                mealTypes = item.mealTypes
            )
        }
    }

    private fun List<Float>.upperBound(value: Float): Int {
        var left = 0
        var right = size
        while (left < right) {
            val mid = (left + right) ushr 1
            if (this[mid] <= value) {
                left = mid + 1
            } else {
                right = mid
            }
        }
        return (left - 1).coerceAtLeast(0)
    }

    private fun computeMonthlyActiveDays(dailyMealRecords: List<DailyMealRecord>): Int {
        val today = LocalDate.now()
        return dailyMealRecords.count { record ->
            record.date.year == today.year &&
                record.date.monthValue == today.monthValue &&
                record.level > 0
        }
    }

    private fun computeWeeklyActiveDays(recordsByDate: Map<LocalDate, List<FoodRecord>>): Int {
        val today = LocalDate.now()
        val weekStart = today.with(WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1)
        val weekEnd = weekStart.plusDays(6)
        return recordsByDate.keys.count { it in weekStart..weekEnd }
    }

    private fun computeWeeklyRecordCount(recordsByDate: Map<LocalDate, List<FoodRecord>>): Int {
        val today = LocalDate.now()
        val weekStart = today.with(WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1)
        val weekEnd = weekStart.plusDays(6)
        return recordsByDate
            .asSequence()
            .filter { (date, _) -> date in weekStart..weekEnd }
            .sumOf { (_, records) -> records.size }
    }

    private fun buildFoodRecordTableRows(dayFoodRecords: List<FoodRecord>): List<FoodRecordTableRow> {
        return dayFoodRecords
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

    private fun computeTopFoodRows(
        foodRecords: List<FoodRecord>,
        recentDays: Long
    ): List<TopFoodRow> {
        val startMillis = LocalDate.now()
            .minusDays(recentDays - 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val grouped = linkedMapOf<String, TopFoodAggregate>()

        foodRecords.forEach { record ->
            if (record.recordTime < startMillis) return@forEach
            val foodName = record.foodName.trim().ifBlank { return@forEach }
            val aggregate = grouped[foodName]
            if (aggregate == null) {
                grouped[foodName] = TopFoodAggregate(
                    count = 1,
                    totalCalories = record.totalCalories,
                    latestRecordTime = record.recordTime
                )
            } else {
                grouped[foodName] = aggregate.copy(
                    count = aggregate.count + 1,
                    totalCalories = aggregate.totalCalories + record.totalCalories,
                    latestRecordTime = maxOf(aggregate.latestRecordTime, record.recordTime)
                )
            }
        }

        return grouped.map { (foodName, aggregate) ->
            TopFoodRow(
                foodName = foodName,
                count = aggregate.count,
                totalCalories = aggregate.totalCalories,
                lastRecordDate = java.time.Instant.ofEpochMilli(aggregate.latestRecordTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            )
        }.sortedWith(compareByDescending<TopFoodRow> { it.count }.thenByDescending { it.totalCalories })
            .take(6)
    }

    private data class TopFoodAggregate(
        val count: Int,
        val totalCalories: Int,
        val latestRecordTime: Long
    )

    private fun computeAchievementBadges(
        streakDays: Int,
        weeklyActiveDays: Int,
        weeklyGoalDays: Int,
        weeklyRecordCount: Int
    ): List<AchievementBadge> {
        return listOf(
            AchievementBadge(
                title = "周目标打卡",
                achieved = weeklyActiveDays >= weeklyGoalDays,
                progress = "$weeklyActiveDays/$weeklyGoalDays 天"
            ),
            AchievementBadge(
                title = "连续打卡 7 天",
                achieved = streakDays >= 7,
                progress = "$streakDays 天"
            ),
            AchievementBadge(
                title = "本周记录达人",
                achieved = weeklyRecordCount >= 21,
                progress = "$weeklyRecordCount 条"
            )
        )
    }

    private fun computeRecipeStats(
        favorites: List<FavoriteRecipe>,
        pantryIngredients: List<PantryIngredient>,
        plans: List<RecipePlan>
    ): RecipeStats {
        val now = System.currentTimeMillis()
        val expiringThreshold = now + 3L * 24L * 60L * 60L * 1000L
        val expiringSoon = pantryIngredients.count { item ->
            val expiresAt = item.expiresAt ?: return@count false
            expiresAt in now..expiringThreshold
        }

        val mostUsedFavorite = favorites.maxByOrNull { it.useCount }
        return RecipeStats(
            pantryCount = pantryIngredients.size,
            pantryExpiringSoonCount = expiringSoon,
            favoriteCount = favorites.size,
            usedFavoriteCount = favorites.count { it.useCount > 0 },
            favoriteUseCount = favorites.sumOf { it.useCount },
            mostUsedFavoriteName = mostUsedFavorite?.foodName,
            mostUsedFavoriteUseCount = mostUsedFavorite?.useCount ?: 0,
            recipePlanCount = plans.size
        )
    }
}

data class StatsBasicSnapshot(
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

data class StatsOverviewSnapshot(
    val todayStats: TodayStats,
    val mealTypeStats: Map<MealType, Int>,
    val weeklyGoalDays: Int,
    val weeklyActiveDays: Int,
    val weeklyRecordCount: Int,
    val foodRecordTableRows: List<FoodRecordTableRow>,
    val topFoodRows: List<TopFoodRow>,
    val achievementBadges: List<AchievementBadge>
)
