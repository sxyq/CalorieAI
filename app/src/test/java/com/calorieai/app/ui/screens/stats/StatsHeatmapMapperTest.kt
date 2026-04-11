package com.calorieai.app.ui.screens.stats

import com.calorieai.app.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class StatsHeatmapMapperTest {
    @Test
    fun toHeatmapData_mapsAllDailyRecordsWithVisibleLevels() {
        val records = listOf(
            DailyMealRecord(LocalDate.of(2026, 4, 1), level = 2, mealTypes = setOf(MealType.BREAKFAST)),
            DailyMealRecord(LocalDate.of(2026, 4, 2), level = 8, mealTypes = setOf(MealType.DINNER))
        )

        val heatmap = StatsHeatmapMapper.toHeatmapData(records)

        assertEquals(2, heatmap.size)
        assertTrue(heatmap[0].value >= 2f)
        assertTrue(heatmap[1].value > heatmap[0].value)
    }

    @Test
    fun toMonthlyHeatmapData_filtersByMonth() {
        val records = listOf(
            DailyMealRecord(LocalDate.of(2026, 3, 31), level = 4),
            DailyMealRecord(LocalDate.of(2026, 4, 1), level = 7),
            DailyMealRecord(LocalDate.of(2026, 4, 15), level = 3),
            DailyMealRecord(LocalDate.of(2026, 5, 1), level = 9)
        )

        val april = StatsHeatmapMapper.toMonthlyHeatmapData(records, YearMonth.of(2026, 4))

        assertEquals(2, april.size)
        assertEquals(LocalDate.of(2026, 4, 1), april[0].date)
        assertEquals(LocalDate.of(2026, 4, 15), april[1].date)
        assertTrue(april.all { it.value > 0f })
    }

    @Test
    fun toHeatmapData_assignsMidLevelWhenAllActiveValuesEqual() {
        val records = listOf(
            DailyMealRecord(LocalDate.of(2026, 4, 1), level = 1),
            DailyMealRecord(LocalDate.of(2026, 4, 2), level = 1),
            DailyMealRecord(LocalDate.of(2026, 4, 3), level = 1)
        )

        val heatmap = StatsHeatmapMapper.toHeatmapData(records)
        val levels = heatmap.map { it.value }

        assertTrue(levels.distinct().size == 1)
        assertTrue(levels.first() >= 2f)
    }

    @Test
    fun toHeatmapDataWithTodayOverride_injectsTodayWhenHasRecords() {
        val today = LocalDate.of(2026, 4, 11)
        val records = listOf(
            DailyMealRecord(LocalDate.of(2026, 4, 10), level = 4)
        )

        val heatmap = StatsHeatmapMapper.toHeatmapDataWithTodayOverride(
            dailyMealRecords = records,
            todayRecordCount = 2,
            today = today
        )

        assertEquals(2, heatmap.size)
        assertTrue(heatmap.any { it.date == today && it.value > 0f })
    }
}
