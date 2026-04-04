package com.calorieai.app.ui.screens.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.NutritionCalculator
import com.calorieai.app.data.model.NutritionReference
import com.calorieai.app.data.model.UserBodyProfile
import com.calorieai.app.ui.components.AnimatedListItem
import com.calorieai.app.ui.components.charts.*
import com.calorieai.app.ui.components.fadingTopEdge
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.utils.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun MonthlySummaryContent(
    uiState: StatsUiState,
    onMonthChange: (Int) -> Unit
) {
    val summary = uiState.lastMonthSummary
    val currentOffset = uiState.selectedMonthOffset

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // 月份切换器
        MonthSelector(
            currentOffset = currentOffset,
            onMonthChange = onMonthChange
        )

        // 顶部大图卡片 - 总热量环形图
        SummaryHeaderCardWithChart(summary)

        // 关键指标卡片
        SummaryKeyMetricsCard(summary)

        // 餐次分布饼图
        MealTypeDistributionChart(summary)

        // 达标/超标天数对比图
        TargetAchievementChart(summary)

        // 运动统计卡片（如果有）
        if ((summary?.totalExerciseCalories ?: 0) > 0) {
            ExerciseSummaryCard(summary)
        }

        // 体重变化卡片（如果有）
        if ((summary?.weightChange ?: 0f) != 0f) {
            WeightChangeCard(summary)
        }

        // 饮水统计卡片
        WaterMonthlySummaryCard(
            monthlyTotal = uiState.monthlyWaterTotal,
            weeklyAverage = uiState.weeklyWaterAverage,
            targetAmount = uiState.waterTargetAmount
        )

        // 详细数据表格
        SummaryDetailTable(summary)
    }
}

/**
 * 月份选择器
 */
@Composable
internal fun MonthSelector(
    currentOffset: Int,
    onMonthChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一个月
            IconButton(
                onClick = { onMonthChange(currentOffset + 1) },
                enabled = currentOffset < 12
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上一个月")
            }

            // 当前显示的月份
            val targetMonth = java.time.YearMonth.now().minusMonths(currentOffset.toLong())
            Text(
                text = targetMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 下一个月
            IconButton(
                onClick = { onMonthChange(currentOffset - 1) },
                enabled = currentOffset > 1
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下一个月")
            }
        }
    }
}

/**
 * 总结头部卡片
 */
@Composable
internal fun SummaryHeaderCard(summary: MonthSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${summary?.year ?: "2026"}年",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "${summary?.month ?: 1}月",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "总结",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 统计指标网格
 */
@Composable
internal fun SummaryMetricsGrid(summary: MonthSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 第一行：总摄入 | 日均摄入
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetricItem(
                    label = "总摄入",
                    value = "${summary?.totalCalories ?: 0}",
                    unit = "千卡"
                )
                SummaryMetricItem(
                    label = "日均摄入",
                    value = "${summary?.avgDailyCalories ?: 0}",
                    unit = "千卡"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 第二行：达标天数 | 超标天数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetricItem(
                    label = "达标天数",
                    value = "${summary?.targetMetDays ?: 0}",
                    unit = "天",
                    color = MaterialTheme.colorScheme.tertiary
                )
                SummaryMetricItem(
                    label = "超标天数",
                    value = "${summary?.overTargetDays ?: 0}",
                    unit = "天",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 第三行：各餐次摄入
            Text(
                text = "各餐次摄入",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetricItem(
                    label = "早餐",
                    value = "${summary?.breakfastTotal ?: 0}",
                    unit = "千卡"
                )
                SummaryMetricItem(
                    label = "午餐",
                    value = "${summary?.lunchTotal ?: 0}",
                    unit = "千卡"
                )
                SummaryMetricItem(
                    label = "晚餐",
                    value = "${summary?.dinnerTotal ?: 0}",
                    unit = "千卡"
                )
                SummaryMetricItem(
                    label = "加餐",
                    value = "${summary?.snackTotal ?: 0}",
                    unit = "千卡"
                )
            }

            // 运动数据（如果有）
            if ((summary?.totalExerciseCalories ?: 0) > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "💪 运动统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryMetricItem(
                        label = "运动消耗",
                        value = "${summary?.totalExerciseCalories ?: 0}",
                        unit = "千卡",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    SummaryMetricItem(
                        label = "运动时长",
                        value = "${summary?.totalExerciseMinutes ?: 0}",
                        unit = "分钟",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    SummaryMetricItem(
                        label = "运动天数",
                        value = "${summary?.exerciseDays ?: 0}",
                        unit = "天",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // 最活跃运动类型
                summary?.mostActiveExerciseType?.let { exerciseType ->
                    Spacer(modifier = Modifier.height(8.dp))
                    val exerciseName = "${exerciseType.emoji} ${exerciseType.displayName}"
                    Text(
                        text = "最活跃：$exerciseName (${summary.mostActiveExerciseCalories}千卡)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // 体重变化（如果有运动数据）
            if ((summary?.weightChange ?: 0f) != 0f) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "⚖️ 体重变化",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val weightChange = summary?.weightChange ?: 0f
                val changeText = if (weightChange < 0) {
                    "减重 ${String.format("%.1f", -weightChange)} kg"
                } else {
                    "增重 ${String.format("%.1f", weightChange)} kg"
                }
                val changeColor = if (weightChange < 0) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                }

                Text(
                    text = changeText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = changeColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "基于运动消耗估算",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * 总结指标项
 */
@Composable
internal fun SummaryMetricItem(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 带图表的总结头部卡片 - 环形进度图
 */
@Composable
internal fun SummaryHeaderCardWithChart(summary: MonthSummary?) {
    val totalCalories = summary?.totalCalories ?: 0
    val avgDaily = summary?.avgDailyCalories ?: 0
    val targetCalories = 2000 // 假设目标2000千卡/天
    val daysInMonth = summary?.let { java.time.YearMonth.of(it.year, it.month).lengthOfMonth() } ?: 30
    val monthlyTarget = targetCalories * daysInMonth
    val progress = (totalCalories.toFloat() / monthlyTarget).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${summary?.year ?: "2026"}年${summary?.month ?: 1}月",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 环形进度图
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景圆环
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    strokeWidth = 12.dp
                )
                // 进度圆环
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 12.dp
                )
                // 中心文字
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalCalories",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "千卡",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 日均摄入
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "日均 $avgDaily 千卡",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 关键指标卡片
 */
@Composable
internal fun SummaryKeyMetricsCard(summary: MonthSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📊 关键指标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 记录天数
                KeyMetricWithIcon(
                    icon = "📝",
                    value = "${summary?.totalRecords ?: 0}",
                    label = "记录天数"
                )

                // 最高日摄入
                KeyMetricWithIcon(
                    icon = "🔥",
                    value = "${summary?.maxDailyCalories ?: 0}",
                    label = "最高日摄入"
                )

                // 达标率
                val totalDays = (summary?.targetMetDays ?: 0) + (summary?.overTargetDays ?: 0)
                val successRate = if (totalDays > 0) {
                    (summary?.targetMetDays ?: 0) * 100 / totalDays
                } else 0
                KeyMetricWithIcon(
                    icon = "🎯",
                    value = "$successRate%",
                    label = "达标率"
                )
            }
        }
    }
}

@Composable
internal fun KeyMetricWithIcon(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 餐次分布横向条形图
 */
@Composable
internal fun MealTypeDistributionChart(summary: MonthSummary?) {
    val meals = listOf(
        Triple("早餐", summary?.breakfastTotal ?: 0, MaterialTheme.colorScheme.primary),
        Triple("午餐", summary?.lunchTotal ?: 0, MaterialTheme.colorScheme.secondary),
        Triple("晚餐", summary?.dinnerTotal ?: 0, MaterialTheme.colorScheme.tertiary),
        Triple("加餐", summary?.snackTotal ?: 0, MaterialTheme.colorScheme.outline)
    )
    val maxValue = meals.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🍽️ 餐次分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            meals.forEach { (name, value, color) ->
                val percentage = value.toFloat() / maxValue
                MealBarItem(name = name, value = value, percentage = percentage, color = color)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
internal fun MealBarItem(name: String, value: Int, percentage: Float, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(50.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

/**
 * 达标/超标天数对比图
 */
@Composable
internal fun TargetAchievementChart(summary: MonthSummary?) {
    val targetMet = summary?.targetMetDays ?: 0
    val overTarget = summary?.overTargetDays ?: 0
    val total = (targetMet + overTarget).coerceAtLeast(1)
    val metPercentage = targetMet.toFloat() / total
    val overPercentage = overTarget.toFloat() / total

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🎯 目标达成",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 堆叠条形图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // 达标部分
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(metPercentage.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    // 超标部分
                    if (overPercentage > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(overPercentage)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.tertiary,
                    label = "达标",
                    value = "$targetMet 天"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.error,
                    label = "超标",
                    value = "$overTarget 天"
                )
            }
        }
    }
}

@Composable
internal fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 运动总结卡片
 */
@Composable
internal fun ExerciseSummaryCard(summary: MonthSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💪 运动统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseMetric(
                    icon = "🔥",
                    value = "${summary?.totalExerciseCalories ?: 0}",
                    label = "消耗千卡"
                )
                ExerciseMetric(
                    icon = "⏱️",
                    value = "${summary?.totalExerciseMinutes ?: 0}",
                    label = "运动分钟"
                )
                ExerciseMetric(
                    icon = "📅",
                    value = "${summary?.exerciseDays ?: 0}",
                    label = "运动天数"
                )
            }

            // 最活跃运动类型
            summary?.mostActiveExerciseType?.let { exerciseType ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "最活跃：${exerciseType.emoji} ${exerciseType.displayName} (${summary.mostActiveExerciseCalories}千卡)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
internal fun ExerciseMetric(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * 体重变化卡片
 */
@Composable
internal fun WeightChangeCard(summary: MonthSummary?) {
    val weightChange = summary?.weightChange ?: 0f
    val isLoss = weightChange < 0
    val changeText = if (isLoss) "减重 ${String.format("%.1f", -weightChange)}" else "增重 ${String.format("%.1f", weightChange)}"
    val changeColor = if (isLoss) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoss) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚖️ 体重变化",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLoss) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = changeText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = changeColor
            )

            Text(
                text = "kg",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLoss) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "基于运动消耗估算",
                style = MaterialTheme.typography.bodySmall,
                color = if (isLoss) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 详细数据表格
 */
@Composable
internal fun SummaryDetailTable(summary: MonthSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📋 详细数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 表格行
            DetailTableRow("总摄入热量", "${summary?.totalCalories ?: 0} 千卡")
            DetailTableRow("日均摄入", "${summary?.avgDailyCalories ?: 0} 千卡")
            DetailTableRow("最高日摄入", "${summary?.maxDailyCalories ?: 0} 千卡")
            DetailTableRow("记录天数", "${summary?.totalRecords ?: 0} 天")
            DetailTableRow("达标天数", "${summary?.targetMetDays ?: 0} 天")
            DetailTableRow("超标天数", "${summary?.overTargetDays ?: 0} 天")

            if ((summary?.totalExerciseCalories ?: 0) > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailTableRow("运动消耗", "${summary?.totalExerciseCalories ?: 0} 千卡")
                DetailTableRow("运动时长", "${summary?.totalExerciseMinutes ?: 0} 分钟")
            }
        }
    }
}

@Composable
internal fun DetailTableRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 优雅的日期选择器
 * 横向滑动选择日期，带有动画效果
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ElegantDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val weekDayFormatter = DateTimeFormatter.ofPattern("EEE")
    
    // 生成日期范围：前30天到今天
    val dates = remember {
        (-30..0).map { today.plusDays(it.toLong()) }
    }
    
    // 找到选中日期的索引
    val selectedIndex = dates.indexOf(selectedDate).takeIf { it >= 0 } ?: dates.size - 1
    
    // 懒加载列表状态
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, selectedIndex - 2))
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选择日期",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 显示完整日期
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 日期选择卡片 - 可滑动
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
            )
        ) {
            val coroutineScope = rememberCoroutineScope()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左箭头
                IconButton(
                    onClick = {
                        // 向前滚动5天
                        val targetIndex = maxOf(0, listState.firstVisibleItemIndex - 5)
                        coroutineScope.launch {
                            listState.animateScrollToItem(targetIndex)
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "向前",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 可滑动的日期列表
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(dates.size) { index ->
                        val date = dates[index]
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        DateItem(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { onDateSelected(date) },
                            dateFormatter = dateFormatter,
                            weekDayFormatter = weekDayFormatter
                        )
                    }
                }

                // 右箭头
                IconButton(
                    onClick = {
                        // 向后滚动5天
                        val targetIndex = minOf(dates.size - 1, listState.firstVisibleItemIndex + 5)
                        coroutineScope.launch {
                            listState.animateScrollToItem(targetIndex)
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "向后",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 更多日期按钮
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            var showDatePicker by remember { mutableStateOf(false) }

            TextButton(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "选择其他日期",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                    }
                ) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )

                    DatePicker(state = datePickerState)

                    // 监听日期变化
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(selected)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个日期项
 */
@Composable
internal fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    dateFormatter: DateTimeFormatter,
    weekDayFormatter: DateTimeFormatter
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "background"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "content"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 星期
        Text(
            text = date.format(weekDayFormatter),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 日期
        Text(
            text = date.format(dateFormatter),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )

        // 今天标记
        if (isToday) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}
