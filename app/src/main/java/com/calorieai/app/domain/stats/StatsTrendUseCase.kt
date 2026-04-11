package com.calorieai.app.domain.stats

import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.ui.components.charts.TimeDimension
import com.calorieai.app.ui.components.charts.TrendChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsTrendUseCase @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val weightRecordRepository: WeightRecordRepository
) {
    suspend fun computeTrendData(
        timeDimension: TimeDimension,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): TrendChartData = withContext(Dispatchers.Default) {
        val today = LocalDate.now()
        val safeEndDate = (endDate ?: today).coerceAtMost(today)
        val safeStartDate = (startDate ?: today.minusDays(30)).coerceAtMost(safeEndDate)
        val zoneId = ZoneId.systemDefault()
        val startMillis = safeStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = safeEndDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val calorieByDate = foodRecordRepository
            .getDailyCaloriesByDateRangeSync(startMillis, endMillis)
            .mapNotNull { row ->
                runCatching { LocalDate.parse(row.date) to row.totalCalories.toFloat() }.getOrNull()
            }
            .toMap()

        val exerciseByDate = exerciseRecordRepository
            .getDailyCaloriesBetweenSync(startMillis, endMillis)
            .mapNotNull { row ->
                runCatching { LocalDate.parse(row.date) to row.totalCalories.toFloat() }.getOrNull()
            }
            .toMap()

        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startMillis, endMillis)

        when (timeDimension) {
            TimeDimension.DAY -> computeDailyTrend(
                calorieByDate = calorieByDate,
                exerciseByDate = exerciseByDate,
                weightRecords = weightRecords,
                startDate = safeStartDate,
                endDate = safeEndDate
            )
            TimeDimension.WEEK -> computeWeeklyTrend(
                calorieByDate = calorieByDate,
                exerciseByDate = exerciseByDate,
                weightRecords = weightRecords,
                startDate = safeStartDate,
                endDate = safeEndDate
            )
            TimeDimension.MONTH -> computeMonthlyTrend(
                calorieByDate = calorieByDate,
                exerciseByDate = exerciseByDate,
                weightRecords = weightRecords,
                startDate = safeStartDate,
                endDate = safeEndDate
            )
        }
    }

    private fun computeDailyTrend(
        calorieByDate: Map<LocalDate, Float>,
        exerciseByDate: Map<LocalDate, Float>,
        weightRecords: List<WeightRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val zoneId = ZoneId.systemDefault()
        val weightByDate = weightRecords
            .groupBy {
                java.time.Instant.ofEpochMilli(it.recordDate)
                    .atZone(zoneId)
                    .toLocalDate()
            }
            .mapValues { (_, records) -> records.maxByOrNull { it.recordDate }?.weight }

        val dates = mutableListOf<LocalDate>()
        val intake = mutableListOf<Float>()
        val exercise = mutableListOf<Float>()
        val weights = mutableListOf<Float?>()

        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
            dates += cursor
            intake += (calorieByDate[cursor] ?: 0f)
            exercise += (exerciseByDate[cursor] ?: 0f)
            weights += weightByDate[cursor]
            cursor = cursor.plusDays(1)
        }
        return TrendChartData(dates, intake, exercise, weights)
    }

    private fun computeWeeklyTrend(
        calorieByDate: Map<LocalDate, Float>,
        exerciseByDate: Map<LocalDate, Float>,
        weightRecords: List<WeightRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        val weekFields = WeekFields.of(java.util.Locale.getDefault())
        fun weekStartOf(date: LocalDate): LocalDate = date.with(weekFields.dayOfWeek(), 1)

        val calorieByWeek = aggregateFloatByPeriod(calorieByDate, ::weekStartOf)
        val exerciseByWeek = aggregateFloatByPeriod(exerciseByDate, ::weekStartOf)
        val weightByWeek = aggregateWeightByPeriod(weightRecords, ::weekStartOf)

        val dates = mutableListOf<LocalDate>()
        val intake = mutableListOf<Float>()
        val exercise = mutableListOf<Float>()
        val weights = mutableListOf<Float?>()

        var cursor = weekStartOf(startDate)
        while (!cursor.isAfter(endDate)) {
            dates += cursor
            intake += (calorieByWeek[cursor] ?: 0f)
            exercise += (exerciseByWeek[cursor] ?: 0f)
            weights += weightByWeek[cursor]
            cursor = cursor.plusWeeks(1)
        }
        return TrendChartData(dates, intake, exercise, weights)
    }

    private fun computeMonthlyTrend(
        calorieByDate: Map<LocalDate, Float>,
        exerciseByDate: Map<LocalDate, Float>,
        weightRecords: List<WeightRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): TrendChartData {
        fun monthStartOf(date: LocalDate): LocalDate = date.withDayOfMonth(1)

        val calorieByMonth = aggregateFloatByPeriod(calorieByDate, ::monthStartOf)
        val exerciseByMonth = aggregateFloatByPeriod(exerciseByDate, ::monthStartOf)
        val weightByMonth = aggregateWeightByPeriod(weightRecords, ::monthStartOf)

        val dates = mutableListOf<LocalDate>()
        val intake = mutableListOf<Float>()
        val exercise = mutableListOf<Float>()
        val weights = mutableListOf<Float?>()

        var cursor = monthStartOf(startDate)
        while (!cursor.isAfter(endDate)) {
            dates += cursor
            intake += (calorieByMonth[cursor] ?: 0f)
            exercise += (exerciseByMonth[cursor] ?: 0f)
            weights += weightByMonth[cursor]
            cursor = cursor.plusMonths(1)
        }
        return TrendChartData(dates, intake, exercise, weights)
    }

    private fun aggregateFloatByPeriod(
        byDate: Map<LocalDate, Float>,
        periodStart: (LocalDate) -> LocalDate
    ): Map<LocalDate, Float> {
        val result = HashMap<LocalDate, Float>(byDate.size.coerceAtLeast(16))
        byDate.forEach { (date, value) ->
            val key = periodStart(date)
            result[key] = (result[key] ?: 0f) + value
        }
        return result
    }

    private fun aggregateWeightByPeriod(
        records: List<WeightRecord>,
        periodStart: (LocalDate) -> LocalDate
    ): Map<LocalDate, Float?> {
        if (records.isEmpty()) return emptyMap()
        val zoneId = ZoneId.systemDefault()
        val sumMap = HashMap<LocalDate, Float>()
        val countMap = HashMap<LocalDate, Int>()

        records.forEach { record ->
            val date = java.time.Instant.ofEpochMilli(record.recordDate)
                .atZone(zoneId)
                .toLocalDate()
            val key = periodStart(date)
            sumMap[key] = (sumMap[key] ?: 0f) + record.weight
            countMap[key] = (countMap[key] ?: 0) + 1
        }

        return sumMap.mapValues { (key, sum) ->
            val count = countMap[key] ?: 0
            if (count <= 0) null else (sum / count)
        }
    }
}
