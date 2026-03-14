package com.calorieai.app.utils

import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 统计工具类
 * 参考Deadliner的OverviewUtils实现
 */
object StatsUtils {

    /**
     * 将时间戳转换为LocalDate
     */
    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    /**
     * 计算今日统计数据（包含运动数据和详细营养素）
     */
    fun computeTodayStats(
        foodRecords: List<FoodRecord>,
        exerciseRecords: List<com.calorieai.app.data.model.ExerciseRecord>,
        targetCalories: Int,
        bmr: Int = 0,
        tdee: Int = 0
    ): TodayStats {
        val today = LocalDate.now()
        val todayFoodRecords = foodRecords.filter { it.recordTime.toLocalDate() == today }
        val totalCalories = todayFoodRecords.sumOf { it.totalCalories }
        val proteinGrams = todayFoodRecords.sumOf { it.protein.toDouble() }.toFloat()
        val carbsGrams = todayFoodRecords.sumOf { it.carbs.toDouble() }.toFloat()
        val fatGrams = todayFoodRecords.sumOf { it.fat.toDouble() }.toFloat()

        // 计算扩展营养素
        val fiberGrams = todayFoodRecords.sumOf { it.fiber.toDouble() }.toFloat()
        val sugarGrams = todayFoodRecords.sumOf { it.sugar.toDouble() }.toFloat()
        val sodiumMg = todayFoodRecords.sumOf { it.sodium.toDouble() }.toFloat()
        val cholesterolMg = todayFoodRecords.sumOf { it.cholesterol.toDouble() }.toFloat()
        val saturatedFatGrams = todayFoodRecords.sumOf { it.saturatedFat.toDouble() }.toFloat()
        val calciumMg = todayFoodRecords.sumOf { it.calcium.toDouble() }.toFloat()
        val ironMg = todayFoodRecords.sumOf { it.iron.toDouble() }.toFloat()
        val vitaminCMg = todayFoodRecords.sumOf { it.vitaminC.toDouble() }.toFloat()
        val vitaminAMcg = todayFoodRecords.sumOf { it.vitaminA.toDouble() }.toFloat()
        val potassiumMg = todayFoodRecords.sumOf { it.potassium.toDouble() }.toFloat()

        // 计算今日运动数据
        val todayExerciseRecords = exerciseRecords.filter {
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == today
        }
        val exerciseCalories = todayExerciseRecords.sumOf { it.caloriesBurned }
        val exerciseMinutes = todayExerciseRecords.sumOf { it.durationMinutes }

        return TodayStats(
            date = today,
            totalCalories = totalCalories,
            targetCalories = targetCalories,
            remainingCalories = targetCalories - totalCalories,
            isTargetMet = totalCalories <= targetCalories,
            recordCount = todayFoodRecords.size,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            fiberGrams = fiberGrams,
            sugarGrams = sugarGrams,
            sodiumMg = sodiumMg,
            cholesterolMg = cholesterolMg,
            saturatedFatGrams = saturatedFatGrams,
            calciumMg = calciumMg,
            ironMg = ironMg,
            vitaminCMg = vitaminCMg,
            vitaminAMcg = vitaminAMcg,
            potassiumMg = potassiumMg,
            exerciseCalories = exerciseCalories,
            exerciseMinutes = exerciseMinutes,
            exerciseCount = todayExerciseRecords.size,
            bmr = bmr,
            tdee = tdee
        )
    }

    /**
     * 计算各餐次摄入统计
     */
    fun computeMealTypeStats(records: List<FoodRecord>): Map<MealType, Int> {
        val today = LocalDate.now()
        val todayRecords = records.filter { it.recordTime.toLocalDate() == today }
        
        return MealType.values().associateWith { mealType ->
            todayRecords
                .filter { it.mealType == mealType }
                .sumOf { it.totalCalories }
        }
    }

    /**
     * 计算历史摄入统计
     */
    fun computeHistoryStats(records: List<FoodRecord>): HistoryStats {
        val totalDays = records.map { it.recordTime.toLocalDate() }.distinct().size
        val targetMetDays = records
            .groupBy { it.recordTime.toLocalDate() }
            .count { (_, dayRecords) ->
                dayRecords.sumOf { it.totalCalories } <= 2000 // 假设目标2000
            }
        
        return HistoryStats(
            totalDays = totalDays,
            targetMetDays = targetMetDays,
            overTargetDays = totalDays - targetMetDays
        )
    }

    /**
     * 计算周趋势数据
     */
    fun computeWeeklyTrend(
        records: List<FoodRecord>,
        weeks: Int = 4
    ): List<WeeklyStat> {
        val today = LocalDate.now()
        
        return (0 until weeks).map { weekOffset ->
            val weekStart = today.minusWeeks((weeks - weekOffset).toLong())
            val weekEnd = weekStart.plusDays(6)
            
            val weekRecords = records.filter { 
                val recordDate = it.recordTime.toLocalDate()
                recordDate in weekStart..weekEnd 
            }
            
            val dailyCalories = weekRecords
                .groupBy { it.recordTime.toLocalDate() }
                .map { (_, dayRecords) -> dayRecords.sumOf { it.totalCalories } }
            
            WeeklyStat(
                weekStart = weekStart,
                weekEnd = weekEnd,
                avgCalories = if (dailyCalories.isNotEmpty()) {
                    dailyCalories.average().toInt()
                } else 0,
                totalCalories = weekRecords.sumOf { it.totalCalories },
                daysRecorded = dailyCalories.size
            )
        }
    }

    /**
     * 计算月度趋势数据
     */
    fun computeMonthlyTrend(
        records: List<FoodRecord>,
        months: Int = 6
    ): List<MonthlyStat> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        
        return (0 until months).map { monthOffset ->
            val monthDate = today.minusMonths((months - monthOffset).toLong())
            val monthStr = monthDate.format(formatter)
            
            val monthRecords = records.filter {
                it.recordTime.toLocalDate().format(formatter) == monthStr
            }
            
            val dailyCalories = monthRecords
                .groupBy { it.recordTime.toLocalDate() }
                .map { (_, dayRecords) -> dayRecords.sumOf { it.totalCalories } }
            
            MonthlyStat(
                month = monthStr,
                totalCalories = monthRecords.sumOf { it.totalCalories },
                avgDailyCalories = if (dailyCalories.isNotEmpty()) {
                    dailyCalories.average().toInt()
                } else 0,
                daysRecorded = dailyCalories.size
            )
        }
    }

    /**
     * 计算上月总结
     */
    fun computeLastMonthSummary(
        foodRecords: List<FoodRecord>,
        exerciseRecords: List<com.calorieai.app.data.model.ExerciseRecord>,
        currentWeight: Float? = null
    ): MonthSummary {
        return computeMonthSummary(foodRecords, exerciseRecords, 1, currentWeight)
    }

    /**
     * 计算指定月份总结（支持切换前几个月，包含运动数据和体重变化）
     * @param offset 月份偏移量，1=上个月，2=上两个月，以此类推
     * @param currentWeight 当前体重（用于计算体重变化）
     */
    fun computeMonthSummary(
        foodRecords: List<FoodRecord>,
        exerciseRecords: List<com.calorieai.app.data.model.ExerciseRecord>,
        offset: Int,
        currentWeight: Float?
    ): MonthSummary {
        val today = LocalDate.now()
        val targetMonth = today.minusMonths(offset.toLong())
        val monthStart = targetMonth.withDayOfMonth(1)
        val monthEnd = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth())

        val monthFoodRecords = foodRecords.filter {
            val recordDate = it.recordTime.toLocalDate()
            recordDate in monthStart..monthEnd
        }

        val monthExerciseRecords = exerciseRecords.filter {
            val recordDate = java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            recordDate in monthStart..monthEnd
        }

        val dailyCalories = monthFoodRecords
            .groupBy { it.recordTime.toLocalDate() }
            .map { (_, dayRecords) -> dayRecords.sumOf { it.totalCalories } }

        val mealTypeStats = MealType.values().associateWith { mealType ->
            monthFoodRecords
                .filter { it.mealType == mealType }
                .sumOf { it.totalCalories }
        }

        // 计算运动统计
        val totalExerciseCalories = monthExerciseRecords.sumOf { it.caloriesBurned }
        val totalExerciseMinutes = monthExerciseRecords.sumOf { it.durationMinutes }
        val exerciseDays = monthExerciseRecords
            .map { java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() }
            .distinct()
            .size

        // 计算运动类型分布
        val exerciseTypeDistribution = monthExerciseRecords
            .groupBy { it.exerciseType }
            .mapValues { it.value.sumOf { record -> record.caloriesBurned } }

        // 找出最活跃的运动类型
        val mostActiveExercise = exerciseTypeDistribution.maxByOrNull { it.value }

        // 计算体重变化（简化处理，实际应该从体重记录表中获取）
        // 假设每月减重0.5kg（如果有运动记录）
        val weightChange = if (totalExerciseCalories > 0 && currentWeight != null) {
            // 7700千卡约等于1kg脂肪
            val estimatedWeightLoss = totalExerciseCalories / 7700f
            -estimatedWeightLoss.coerceIn(0f, 2f) // 限制每月最多减重2kg
        } else 0f

        return MonthSummary(
            year = targetMonth.year,
            month = targetMonth.monthValue,
            totalCalories = monthFoodRecords.sumOf { it.totalCalories },
            avgDailyCalories = if (dailyCalories.isNotEmpty()) {
                dailyCalories.average().toInt()
            } else 0,
            maxDailyCalories = dailyCalories.maxOrNull() ?: 0,
            targetMetDays = dailyCalories.count { it <= 2000 },
            overTargetDays = dailyCalories.count { it > 2000 },
            breakfastTotal = mealTypeStats[MealType.BREAKFAST] ?: 0,
            lunchTotal = mealTypeStats[MealType.LUNCH] ?: 0,
            dinnerTotal = mealTypeStats[MealType.DINNER] ?: 0,
            snackTotal = mealTypeStats[MealType.SNACK] ?: 0,
            totalRecords = monthFoodRecords.size,
            // 运动相关数据
            totalExerciseCalories = totalExerciseCalories,
            totalExerciseMinutes = totalExerciseMinutes,
            exerciseDays = exerciseDays,
            mostActiveExerciseType = mostActiveExercise?.key,
            mostActiveExerciseCalories = mostActiveExercise?.value ?: 0,
            weightChange = weightChange
        )
    }

    /**
     * 计算指定日期范围内的月度趋势
     */
    fun computeMonthlyTrendForRange(
        records: List<FoodRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MonthlyStat> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

        // 生成月份列表
        val months = mutableListOf<String>()
        var current = startDate.withDayOfMonth(1)
        while (!current.isAfter(endDate)) {
            months.add(current.format(formatter))
            current = current.plusMonths(1)
        }

        return months.map { monthStr ->
            val monthRecords = records.filter {
                it.recordTime.toLocalDate().format(formatter) == monthStr
            }

            val dailyCalories = monthRecords
                .groupBy { it.recordTime.toLocalDate() }
                .map { (_, dayRecords) -> dayRecords.sumOf { it.totalCalories } }

            MonthlyStat(
                month = monthStr,
                totalCalories = monthRecords.sumOf { it.totalCalories },
                avgDailyCalories = if (dailyCalories.isNotEmpty()) {
                    dailyCalories.average().toInt()
                } else 0,
                daysRecorded = dailyCalories.size
            )
        }
    }

    /**
     * 计算连续记录天数
     */
    fun computeStreakDays(records: List<FoodRecord>): Int {
        if (records.isEmpty()) return 0
        
        val sortedDates = records
            .map { it.recordTime.toLocalDate() }
            .distinct()
            .sortedDescending()
        
        var streak = 0
        var currentDate = LocalDate.now()
        
        for (date in sortedDates) {
            if (date == currentDate || date == currentDate.minusDays(1)) {
                streak++
                currentDate = date
            } else {
                break
            }
        }
        
        return streak
    }
}

// 数据类
data class TodayStats(
    val date: LocalDate,
    val totalCalories: Int,
    val targetCalories: Int,
    val remainingCalories: Int,
    val isTargetMet: Boolean,
    val recordCount: Int,
    // 基础营养素
    val proteinGrams: Float = 0f,
    val carbsGrams: Float = 0f,
    val fatGrams: Float = 0f,
    // 扩展营养素
    val fiberGrams: Float = 0f,
    val sugarGrams: Float = 0f,
    val sodiumMg: Float = 0f,
    val cholesterolMg: Float = 0f,
    val saturatedFatGrams: Float = 0f,
    val calciumMg: Float = 0f,
    val ironMg: Float = 0f,
    val vitaminCMg: Float = 0f,
    val vitaminAMcg: Float = 0f,
    val potassiumMg: Float = 0f,
    // 运动数据
    val exerciseCalories: Int = 0,
    val exerciseMinutes: Int = 0,
    val exerciseCount: Int = 0,
    val bmr: Int = 0,  // 基础代谢
    val tdee: Int = 0  // 总能量消耗
)

data class HistoryStats(
    val totalDays: Int,
    val targetMetDays: Int,
    val overTargetDays: Int
)

data class WeeklyStat(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val avgCalories: Int,
    val totalCalories: Int,
    val daysRecorded: Int
)

data class MonthlyStat(
    val month: String,
    val totalCalories: Int,
    val avgDailyCalories: Int,
    val daysRecorded: Int
)

data class MonthSummary(
    val year: Int,
    val month: Int,
    val totalCalories: Int,
    val avgDailyCalories: Int,
    val maxDailyCalories: Int,
    val targetMetDays: Int,
    val overTargetDays: Int,
    val breakfastTotal: Int,
    val lunchTotal: Int,
    val dinnerTotal: Int,
    val snackTotal: Int,
    val totalRecords: Int,
    // 运动相关数据
    val totalExerciseCalories: Int = 0,
    val totalExerciseMinutes: Int = 0,
    val exerciseDays: Int = 0,
    val mostActiveExerciseType: com.calorieai.app.data.model.ExerciseType? = null,
    val mostActiveExerciseCalories: Int = 0,
    val weightChange: Float = 0f
)
