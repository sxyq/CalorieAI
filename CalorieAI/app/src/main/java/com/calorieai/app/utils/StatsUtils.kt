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
     * 计算今日统计数据
     */
    fun computeTodayStats(
        records: List<FoodRecord>,
        targetCalories: Int
    ): TodayStats {
        val today = LocalDate.now()
        val todayRecords = records.filter { it.recordTime.toLocalDate() == today }
        val totalCalories = todayRecords.sumOf { it.totalCalories }
        
        return TodayStats(
            date = today,
            totalCalories = totalCalories,
            targetCalories = targetCalories,
            remainingCalories = targetCalories - totalCalories,
            isTargetMet = totalCalories <= targetCalories,
            recordCount = todayRecords.size
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
    fun computeLastMonthSummary(records: List<FoodRecord>): MonthSummary {
        return computeMonthSummary(records, 1)
    }

    /**
     * 计算指定月份总结（支持切换前几个月）
     * @param offset 月份偏移量，1=上个月，2=上两个月，以此类推
     */
    fun computeMonthSummary(records: List<FoodRecord>, offset: Int): MonthSummary {
        val today = LocalDate.now()
        val targetMonth = today.minusMonths(offset.toLong())
        val monthStart = targetMonth.withDayOfMonth(1)
        val monthEnd = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth())

        val monthRecords = records.filter {
            val recordDate = it.recordTime.toLocalDate()
            recordDate in monthStart..monthEnd
        }

        val dailyCalories = monthRecords
            .groupBy { it.recordTime.toLocalDate() }
            .map { (_, dayRecords) -> dayRecords.sumOf { it.totalCalories } }

        val mealTypeStats = MealType.values().associateWith { mealType ->
            monthRecords
                .filter { it.mealType == mealType }
                .sumOf { it.totalCalories }
        }

        return MonthSummary(
            year = targetMonth.year,
            month = targetMonth.monthValue,
            totalCalories = monthRecords.sumOf { it.totalCalories },
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
            totalRecords = monthRecords.size
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
    val recordCount: Int
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
    val totalRecords: Int
)
