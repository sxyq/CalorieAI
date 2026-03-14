package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 可展开日历组件
 * 点击日期选择器可以展开/收起日历视图
 */
@Composable
fun ExpandableCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    calorieData: Map<LocalDate, Int> = emptyMap(),
    targetCalories: Int = 2000
) {
    var isExpanded by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 日期选择器头部（可点击展开/收起）
            CalendarHeader(
                selectedDate = selectedDate,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )

            // 展开的日历视图
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                CalendarContent(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        onDateSelected(date)
                        isExpanded = false
                    },
                    onMonthChange = { currentMonth = it },
                    calorieData = calorieData,
                    targetCalories = targetCalories
                )
            }
        }
    }
}

/**
 * 日历头部（显示当前选中的日期，可点击展开）
 */
@Composable
private fun CalendarHeader(
    selectedDate: LocalDate,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val weekDay = selectedDate.format(DateTimeFormatter.ofPattern("EEE"))
    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("MM-dd"))
    val today = LocalDate.now()
    val relativeLabel = when (selectedDate) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        today.minusDays(2) -> "前天"
        today.plusDays(1) -> "明天"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日期显示
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weekDay,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateStr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (relativeLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($relativeLabel)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 展开/收起图标
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "收起日历" else "展开日历",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 日历内容
 */
@Composable
private fun CalendarContent(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    calorieData: Map<LocalDate, Int>,
    targetCalories: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 月份导航
        MonthNavigation(
            currentMonth = currentMonth,
            onMonthChange = onMonthChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 星期标题
        WeekDayHeader()

        Spacer(modifier = Modifier.height(4.dp))

        // 日期网格
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            calorieData = calorieData,
            targetCalories = targetCalories
        )
    }
}

/**
 * 月份导航
 */
@Composable
private fun MonthNavigation(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上个月
        TextButton(
            onClick = { onMonthChange(currentMonth.minusMonths(1)) }
        ) {
            Text("<")
        }

        // 当前月份
        Text(
            text = currentMonth.format(monthFormatter),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 下个月
        TextButton(
            onClick = { onMonthChange(currentMonth.plusMonths(1)) }
        ) {
            Text(">")
        }
    }
}

/**
 * 星期标题
 */
@Composable
private fun WeekDayHeader() {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 日期网格
 */
@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    calorieData: Map<LocalDate, Int>,
    targetCalories: Int
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Column {
        var dayCount = 1
        for (week in 0..5) {
            if (dayCount > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // 空白格子
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else if (dayCount <= daysInMonth) {
                        val date = currentMonth.atDay(dayCount)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val isFuture = date.isAfter(LocalDate.now())
                        val calories = calorieData[date] ?: 0
                        val isOverTarget = calories > targetCalories

                        DayCell(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            isFuture = isFuture,
                            calories = calories,
                            isOverTarget = isOverTarget,
                            onClick = { if (!isFuture) onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCount++
                    } else {
                        // 空白格子
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 日期单元格
 */
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    calories: Int,
    isOverTarget: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val calorieIndicatorColor = when {
        calories == 0 -> androidx.compose.ui.graphics.Color.Transparent
        isOverTarget -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (!isFuture) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 日期数字
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            // 热量指示点（如果有记录）
            if (calories > 0) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(calorieIndicatorColor)
                )
            }
        }
    }
}
