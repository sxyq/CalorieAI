package com.calorieai.app.ui.screens.stats

import com.calorieai.app.ui.components.HeatmapData
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

internal object StatsHeatmapMapper {
    private const val MAX_LEVEL = 10
    private const val MIN_ACTIVE_LEVEL = 2

    fun toHeatmapData(dailyMealRecords: List<DailyMealRecord>): List<HeatmapData> {
        return mapAndNormalize(dailyMealRecords)
    }

    fun toMonthlyHeatmapData(
        dailyMealRecords: List<DailyMealRecord>,
        yearMonth: YearMonth
    ): List<HeatmapData> {
        return mapAndNormalize(
            dailyMealRecords
            .asSequence()
            .filter { it.date.year == yearMonth.year && it.date.month == yearMonth.month }
            .toList()
        )
    }

    fun toHeatmapDataWithTodayOverride(
        dailyMealRecords: List<DailyMealRecord>,
        todayRecordCount: Int,
        today: LocalDate = LocalDate.now()
    ): List<HeatmapData> {
        if (dailyMealRecords.isEmpty() && todayRecordCount <= 0) return emptyList()

        val byDate = dailyMealRecords.associateBy { it.date }.toMutableMap()
        if (todayRecordCount > 0) {
            val boostedLevel = todayRecordCount.coerceIn(1, MAX_LEVEL)
            val existing = byDate[today]
            byDate[today] = if (existing == null) {
                DailyMealRecord(
                    date = today,
                    level = boostedLevel
                )
            } else {
                existing.copy(level = maxOf(existing.level, boostedLevel))
            }
        }
        return mapAndNormalize(byDate.values.toList())
    }

    private fun mapAndNormalize(dailyMealRecords: List<DailyMealRecord>): List<HeatmapData> {
        if (dailyMealRecords.isEmpty()) return emptyList()

        val rawByDate = dailyMealRecords
            .sortedBy { it.date }
            .associate { record ->
                val rawLevel = when {
                    record.level > 0 -> record.level.toFloat()
                    record.mealTypes.isNotEmpty() -> 1f
                    else -> 0f
                }.coerceIn(0f, MAX_LEVEL.toFloat())
                record.date to rawLevel
            }

        val normalizedLevels = normalizeToVisibleLevels(rawByDate.values.toList())
        return rawByDate.entries.mapIndexed { index, (date, _) ->
            HeatmapData(date = date, value = normalizedLevels[index].toFloat())
        }
    }

    private fun normalizeToVisibleLevels(rawLevels: List<Float>): List<Int> {
        val activeValues = rawLevels.filter { it > 0f }.sorted()
        if (activeValues.isEmpty()) {
            return List(rawLevels.size) { 0 }
        }
        val denominator = (activeValues.size - 1).coerceAtLeast(1).toFloat()

        return rawLevels.map { raw ->
            if (raw <= 0f) {
                0
            } else {
                val lower = activeValues.lowerBound(raw)
                val upper = activeValues.upperBound(raw)
                val medianRank = ((lower + upper - 1).coerceAtLeast(0)).toFloat() / 2f
                val ratio = (medianRank / denominator).coerceIn(0f, 1f)
                val normalized = (1 + (ratio * (MAX_LEVEL - 1)).roundToInt()).coerceIn(1, MAX_LEVEL)
                normalized.coerceAtLeast(MIN_ACTIVE_LEVEL)
            }
        }
    }

    private fun List<Float>.lowerBound(target: Float): Int {
        var left = 0
        var right = size
        while (left < right) {
            val mid = (left + right) ushr 1
            if (this[mid] < target) {
                left = mid + 1
            } else {
                right = mid
            }
        }
        return left
    }

    private fun List<Float>.upperBound(target: Float): Int {
        var left = 0
        var right = size
        while (left < right) {
            val mid = (left + right) ushr 1
            if (this[mid] <= target) {
                left = mid + 1
            } else {
                right = mid
            }
        }
        return left
    }
}
