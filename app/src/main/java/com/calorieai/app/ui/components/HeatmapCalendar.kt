package com.calorieai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class HeatmapData(
    val date: LocalDate,
    val value: Float,
    val label: String? = null
)

data class HeatmapColorScheme(
    val emptyColor: Color,
    val levels: List<Color>
) {
    fun colorForLevel(level: Int): Color {
        if (level <= 0 || levels.isEmpty()) return emptyColor
        return levels[(level - 1).coerceIn(0, levels.lastIndex)]
    }

    fun maxLevel(): Int = levels.size.coerceAtLeast(1)

    companion object {
        fun forTheme(isDark: Boolean): HeatmapColorScheme {
            return if (isDark) {
                HeatmapColorScheme(
                    emptyColor = Color(0xFF27303B),
                    levels = listOf(
                        Color(0xFF2E4860),
                        Color(0xFF2B5B78),
                        Color(0xFF266F8F),
                        Color(0xFF1F82A3),
                        Color(0xFF1892B0),
                        Color(0xFF129FA9),
                        Color(0xFF15AA93),
                        Color(0xFF3AB779),
                        Color(0xFF68C95C),
                        Color(0xFF98DD3F)
                    )
                )
            } else {
                HeatmapColorScheme(
                    emptyColor = Color(0xFFEAF0F5),
                    levels = listOf(
                        Color(0xFFDFF3FF),
                        Color(0xFFC9EAFF),
                        Color(0xFFB1DEFF),
                        Color(0xFF93D2FF),
                        Color(0xFF72C2F5),
                        Color(0xFF52B2EB),
                        Color(0xFF389EDC),
                        Color(0xFF2587C6),
                        Color(0xFF126EAE),
                        Color(0xFF045793)
                    )
                )
            }
        }
    }
}

private data class PreparedHeatmap(
    val maxValue: Float,
    val maxLevel: Int,
    val valueByDate: Map<LocalDate, Float>
) {
    fun levelFor(date: LocalDate): Int {
        return levelFromValue(
            value = valueByDate[date] ?: 0f,
            maxValue = maxValue,
            maxLevel = maxLevel
        )
    }
}

private fun prepareHeatmap(
    data: List<HeatmapData>,
    maxValue: Float?,
    maxLevel: Int
): PreparedHeatmap {
    val normalizedValues = data
        .groupBy { it.date }
        .mapValues { (_, entries) ->
            entries.maxOf { it.value }.coerceAtLeast(0f)
        }
    val resolvedMaxValue = (maxValue ?: normalizedValues.values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    return PreparedHeatmap(
        maxValue = resolvedMaxValue,
        maxLevel = maxLevel.coerceAtLeast(1),
        valueByDate = normalizedValues
    )
}

@Composable
fun MonthHeatmap(
    yearMonth: YearMonth,
    data: List<HeatmapData>,
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme? = null,
    maxValue: Float? = null,
    onDayClick: ((LocalDate) -> Unit)? = null,
    showWeekdayLabels: Boolean = true,
    title: String? = null,
    isDark: Boolean = false
) {
    val scheme = colorScheme ?: HeatmapColorScheme.forTheme(isDark)
    val prepared = remember(data, maxValue, scheme) {
        prepareHeatmap(
            data = data,
            maxValue = maxValue,
            maxLevel = scheme.maxLevel()
        )
    }
    val grid = remember(yearMonth) { buildMonthGrid(yearMonth) }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (showWeekdayLabels) {
            WeekdayLabels()
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            grid.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        val inMonth = date.month == yearMonth.month
                        val level = if (inMonth) prepared.levelFor(date) else 0
                        DayCell(
                            date = date,
                            level = level,
                            inMonth = inMonth,
                            scheme = scheme,
                            onDayClick = onDayClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactHeatmap(
    data: List<HeatmapData>,
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme? = null,
    maxValue: Float? = null,
    onDayClick: ((LocalDate) -> Unit)? = null,
    isDark: Boolean = false,
    weeks: Int = 12,
    cellSize: Int = 12
) {
    val scheme = colorScheme ?: HeatmapColorScheme.forTheme(isDark)
    val prepared = remember(data, maxValue, scheme) {
        prepareHeatmap(
            data = data,
            maxValue = maxValue,
            maxLevel = scheme.maxLevel()
        )
    }
    val today = LocalDate.now()
    val weekColumns = remember(today, weeks) {
        buildRecentDates(today = today, weeks = weeks).chunked(7)
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CompactMonthLabels(weekColumns)
        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            weekColumns.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { date ->
                        CompactCell(
                            date = date,
                            level = prepared.levelFor(date),
                            scheme = scheme,
                            onDayClick = onDayClick,
                            cellSize = cellSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapLegend(
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme? = null,
    labels: List<String> = listOf("少", "多"),
    isDark: Boolean = false
) {
    val scheme = colorScheme ?: HeatmapColorScheme.forTheme(isDark)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lowLabel = labels.getOrNull(0) ?: "少"
    val highLabel = labels.getOrNull(1) ?: "多"

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = lowLabel, fontSize = 11.sp, color = textColor)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            (0..scheme.maxLevel()).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(scheme.colorForLevel(level))
                        .border(
                            width = if (level == 0) 0.8.dp else 0.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
        Text(text = highLabel, fontSize = 11.sp, color = textColor)
    }
}

@Composable
private fun WeekdayLabels() {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CompactMonthLabels(weekColumns: List<List<LocalDate>>) {
    val labelByWeekIndex = remember(weekColumns) {
        buildMonthLabelByWeekIndex(weekColumns)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weekColumns.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                val label = labelByWeekIndex[index] ?: return@Box
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    date: LocalDate,
    level: Int,
    inMonth: Boolean,
    scheme: HeatmapColorScheme,
    onDayClick: ((LocalDate) -> Unit)?
) {
    val bgColor = if (inMonth) scheme.colorForLevel(level) else Color.Transparent
    val textColor = when {
        !inMonth -> Color.Transparent
        level >= (scheme.maxLevel() * 0.5f).toInt() -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (inMonth && level == 0) 0.8.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .let { base ->
                if (inMonth && onDayClick != null) base.clickable { onDayClick(date) } else base
            },
        contentAlignment = Alignment.Center
    ) {
        if (inMonth) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = if (level >= 3) FontWeight.Medium else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun CompactCell(
    date: LocalDate,
    level: Int,
    scheme: HeatmapColorScheme,
    onDayClick: ((LocalDate) -> Unit)?,
    cellSize: Int
) {
    Box(
        modifier = Modifier
            .size(cellSize.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(scheme.colorForLevel(level))
            .border(
                width = if (level == 0) 0.8.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(3.dp)
            )
            .let { base -> if (onDayClick != null) base.clickable { onDayClick(date) } else base }
    )
}

private fun buildMonthLabelByWeekIndex(weekColumns: List<List<LocalDate>>): Map<Int, String> {
    if (weekColumns.isEmpty()) return emptyMap()

    val labels = mutableMapOf<Int, String>()
    var previousMonthValue = -1
    weekColumns.forEachIndexed { index, week ->
        val anchorDate = week.firstOrNull() ?: return@forEachIndexed
        if (index == 0 || anchorDate.monthValue != previousMonthValue) {
            labels[index] = anchorDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            previousMonthValue = anchorDate.monthValue
        }
    }
    return labels
}

private fun levelFromValue(value: Float, maxValue: Float, maxLevel: Int): Int {
    if (value <= 0f || maxValue <= 0f) return 0
    val safeMaxLevel = maxLevel.coerceAtLeast(1)

    // 兼容 0~1 的比例输入：例如 0.35 表示 35% 强度
    if (maxValue <= 1f && value <= 1f) {
        val ratio = value.coerceIn(0f, 1f)
        return kotlin.math.ceil(ratio * safeMaxLevel).toInt().coerceIn(1, safeMaxLevel)
    }

    // 兼容离散等级输入：例如 1~10
    if (maxValue <= safeMaxLevel && value <= safeMaxLevel) {
        return kotlin.math.ceil(value).toInt().coerceIn(1, safeMaxLevel)
    }

    // 常规数值按比例映射
    val ratio = (value / maxValue).coerceIn(0f, 1f)
    return kotlin.math.ceil(ratio * safeMaxLevel).toInt().coerceIn(1, safeMaxLevel)
}

private fun buildMonthGrid(yearMonth: YearMonth): List<List<LocalDate>> {
    val firstDay = yearMonth.atDay(1)
    val leading = firstDay.dayOfWeek.toSundayBasedIndex()
    val start = firstDay.minusDays(leading.toLong())
    return (0 until 42).map { offset -> start.plusDays(offset.toLong()) }.chunked(7)
}

private fun buildRecentDates(today: LocalDate, weeks: Int): List<LocalDate> {
    val safeWeeks = weeks.coerceAtLeast(1)
    val sundayOfCurrentWeek = today.minusDays(today.dayOfWeek.toSundayBasedIndex().toLong())
    val start = sundayOfCurrentWeek.minusWeeks((safeWeeks - 1).toLong())
    return (0 until safeWeeks * 7).map { offset -> start.plusDays(offset.toLong()) }
}

private fun DayOfWeek.toSundayBasedIndex(): Int {
    return value % 7
}

fun generateSampleHeatmapData(
    yearMonth: YearMonth,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f
): List<HeatmapData> {
    val days = yearMonth.lengthOfMonth()
    return (1..days).map { day ->
        HeatmapData(
            date = yearMonth.atDay(day),
            value = (valueRange.start + Math.random() * (valueRange.endInclusive - valueRange.start)).toFloat()
        )
    }
}
