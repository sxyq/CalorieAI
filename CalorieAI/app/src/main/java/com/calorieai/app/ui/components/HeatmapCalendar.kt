package com.calorieai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 热力图数据类
 */
data class HeatmapData(
    val date: LocalDate,
    val value: Float,
    val label: String? = null
)

/**
 * 热力图颜色配置 - Glass 主题
 */
data class HeatmapColorScheme(
    val emptyColor: Color = Color.Transparent,
    val level1Color: Color = GlassLightColors.Primary.copy(alpha = 0.2f),
    val level2Color: Color = GlassLightColors.Primary.copy(alpha = 0.4f),
    val level3Color: Color = GlassLightColors.Primary.copy(alpha = 0.6f),
    val level4Color: Color = GlassLightColors.Primary.copy(alpha = 0.8f),
    val level5Color: Color = GlassLightColors.Primary
) {
    fun getColorForLevel(level: Int): Color {
        return when (level) {
            0 -> emptyColor
            1 -> level1Color
            2 -> level2Color
            3 -> level3Color
            4 -> level4Color
            5 -> level5Color
            else -> emptyColor
        }
    }
}

/**
 * 月份热力图组件 - Glass 风格
 */
@Composable
fun MonthHeatmap(
    yearMonth: YearMonth,
    data: List<HeatmapData>,
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme = HeatmapColorScheme(),
    maxValue: Float? = null,
    onDayClick: ((LocalDate) -> Unit)? = null,
    showWeekdayLabels: Boolean = true,
    title: String? = null,
    isDark: Boolean = false
) {
    val calculatedMaxValue = maxValue ?: data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1f
    val dataMap = data.associateBy { it.date }
    
    val firstDayOfMonth = yearMonth.atDay(1)
    val startDate = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong())
    
    val days = (0 until 42).map { dayOffset ->
        startDate.plusDays(dayOffset.toLong())
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        if (showWeekdayLabels) {
            WeekdayLabels(isDark)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            days.chunked(7).forEach { weekDays ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekDays.forEach { date ->
                        val isCurrentMonth = date.month == yearMonth.month
                        val dayData = dataMap[date]
                        val level = if (dayData != null && isCurrentMonth) {
                            calculateHeatLevel(dayData.value, calculatedMaxValue)
                        } else 0
                        
                        HeatmapDayCell(
                            date = date,
                            level = if (isCurrentMonth) level else 0,
                            isCurrentMonth = isCurrentMonth,
                            colorScheme = colorScheme,
                            onClick = onDayClick?.let { { it(date) } },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * 紧凑热力图 - Glass 风格
 */
@Composable
fun CompactHeatmap(
    data: List<HeatmapData>,
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme = HeatmapColorScheme(),
    maxValue: Float? = null,
    onDayClick: ((LocalDate) -> Unit)? = null,
    isDark: Boolean = false
) {
    val calculatedMaxValue = maxValue ?: data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1f
    val dataMap = data.associateBy { it.date }
    
    val today = LocalDate.now()
    val startDate = today.minusWeeks(11).minusDays(today.dayOfWeek.value.toLong())
    
    val days = (0 until 84).map { dayOffset ->
        startDate.plusDays(dayOffset.toLong())
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonthLabels(days, isDark)
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.chunked(7).forEach { weekDays ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    weekDays.forEach { date ->
                        val dayData = dataMap[date]
                        val level = dayData?.let { 
                            calculateHeatLevel(it.value, calculatedMaxValue)
                        } ?: 0
                        
                        CompactHeatmapCell(
                            level = level,
                            colorScheme = colorScheme,
                            onClick = onDayClick?.let { { it(date) } },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * 星期标签行 - Glass 风格
 */
@Composable
private fun WeekdayLabels(isDark: Boolean) {
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
    val textColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 月份标签 - Glass 风格
 */
@Composable
private fun MonthLabels(days: List<LocalDate>, isDark: Boolean) {
    val months = days.map { it.month }.distinct()
    val textColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        months.forEach { month ->
            Text(
                text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 热力图日期单元格 - Glass 风格带悬停动效
 */
@Composable
private fun RowScope.HeatmapDayCell(
    date: LocalDate,
    level: Int,
    isCurrentMonth: Boolean,
    colorScheme: HeatmapColorScheme,
    onClick: (() -> Unit)?,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cellScale"
    )
    
    val backgroundColor = if (isCurrentMonth) {
        colorScheme.getColorForLevel(level)
    } else Color.Transparent
    
    val textColor = when {
        !isCurrentMonth -> Color.Transparent
        level >= 4 -> Color.White
        isDark -> GlassDarkColors.OnSurface
        else -> GlassLightColors.OnSurface
    }
    
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null && isCurrentMonth) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCurrentMonth) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = if (level >= 3) FontWeight.Medium else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

/**
 * 紧凑热力图单元格 - Glass 风格带悬停动效
 */
@Composable
private fun CompactHeatmapCell(
    level: Int,
    colorScheme: HeatmapColorScheme,
    onClick: (() -> Unit)?,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compactCellScale"
    )
    
    val backgroundColor = colorScheme.getColorForLevel(level)
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    )
}

/**
 * 热力图图例 - Glass 风格
 */
@Composable
fun HeatmapLegend(
    modifier: Modifier = Modifier,
    colorScheme: HeatmapColorScheme = HeatmapColorScheme(),
    labels: List<String> = listOf("无", "少", "中", "多", "很多"),
    isDark: Boolean = false
) {
    val textColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "少",
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 11.sp
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            (0..4).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorScheme.getColorForLevel(level))
                )
            }
        }
        
        Text(
            text = "多",
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

/**
 * 计算热力等级 (0-5)
 */
private fun calculateHeatLevel(value: Float, maxValue: Float): Int {
    if (maxValue <= 0) return 0
    val ratio = value / maxValue
    return when {
        ratio <= 0 -> 0
        ratio <= 0.2f -> 1
        ratio <= 0.4f -> 2
        ratio <= 0.6f -> 3
        ratio <= 0.8f -> 4
        else -> 5
    }
}

/**
 * 生成示例数据
 */
fun generateSampleHeatmapData(
    yearMonth: YearMonth,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f
): List<HeatmapData> {
    val daysInMonth = yearMonth.lengthOfMonth()
    return (1..daysInMonth).map { day ->
        HeatmapData(
            date = yearMonth.atDay(day),
            value = (valueRange.start + Math.random() * (valueRange.endInclusive - valueRange.start)).toFloat()
        )
    }
}
