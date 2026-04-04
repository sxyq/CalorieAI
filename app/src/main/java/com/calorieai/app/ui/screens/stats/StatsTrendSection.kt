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

/**
 * 趋势分析内容
 */
@Composable
internal fun TrendAnalysisContent(
    uiState: StatsUiState,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onTimeDimensionChange: (TimeDimension) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // 标题栏：包含时间维度选择和日期范围选择
        TrendAnalysisHeader(
            uiState = uiState,
            onTimeDimensionChange = onTimeDimensionChange,
            onDateRangeSelected = onDateRangeSelected
        )

        // 热量摄入趋势图表
        CalorieTrendChart(
            data = uiState.trendChartData,
            timeDimension = uiState.trendTimeDimension,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 运动消耗趋势图表
        ExerciseTrendChart(
            data = uiState.trendChartData,
            timeDimension = uiState.trendTimeDimension,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 体重变化趋势图表
        WeightTrendChart(
            data = uiState.trendChartData,
            timeDimension = uiState.trendTimeDimension,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        BMITrendChart(
            data = uiState.trendChartData,
            timeDimension = uiState.trendTimeDimension,
            userHeight = uiState.userHeight,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 饮水趋势图表
        WaterTrendChart(
            waterData = uiState.waterTrendData,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * 周趋势卡片 - 使用折线图
 */
@Composable
internal fun WeeklyTrendCard(stats: List<WeeklyStat>) {
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
                text = "周摄入趋势",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 折线图展示
            if (stats.isNotEmpty()) {
                val chartData = stats.map { stat ->
                    "${stat.weekStart.format(DateTimeFormatter.ofPattern("MM/dd"))}" to 
                        stat.avgCalories.toFloat()
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 详细数据
            stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stat.weekStart.format(DateTimeFormatter.ofPattern("MM/dd"))} - ${stat.weekEnd.format(DateTimeFormatter.ofPattern("MM/dd"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${stat.avgCalories}千卡/天",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 月度趋势卡片 - 使用折线图
 */
@Composable
internal fun MonthlyTrendCard(stats: List<MonthlyStat>) {
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
                text = "月度趋势",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 折线图展示
            if (stats.isNotEmpty()) {
                val chartData = stats.map { stat ->
                    stat.month to stat.avgDailyCalories.toFloat()
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    lineColor = android.graphics.Color.parseColor("#4CAF50")
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 详细数据
            stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.month,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${stat.avgDailyCalories}千卡/天",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 趋势分析标题栏（包含时间维度选择和日期范围选择）
 */
@Composable
internal fun TrendAnalysisHeader(
    uiState: StatsUiState,
    onTimeDimensionChange: (TimeDimension) -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 第一行：标题和日期选择按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "趋势分析",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // 日期范围选择按钮
                IconButton(
                    onClick = { showDatePicker = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "选择日期范围",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 显示当前日期范围
            if (uiState.trendStartDate != null && uiState.trendEndDate != null) {
                Text(
                    text = "${uiState.trendStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} 至 ${uiState.trendEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 时间维度选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeDimension.values().forEach { dimension ->
                    val label = when (dimension) {
                        TimeDimension.DAY -> "按天"
                        TimeDimension.WEEK -> "按周"
                        TimeDimension.MONTH -> "按月"
                    }
                    FilterChip(
                        selected = uiState.trendTimeDimension == dimension,
                        onClick = { onTimeDimensionChange(dimension) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                onDateRangeSelected(start, end)
                showDatePicker = false
            },
            onReset = {
                onDateRangeSelected(LocalDate.now().minusMonths(1), LocalDate.now())
                showDatePicker = false
            }
        )
    }
}

/**
 * 热量摄入趋势图表
 */
@Composable
internal fun CalorieTrendChart(
    data: com.calorieai.app.ui.components.charts.TrendChartData,
    timeDimension: TimeDimension,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "热量摄入趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 统计值
                val totalCalories = data.calorieIntake.sum().toInt()
                Text(
                    text = "总计: ${totalCalories}千卡",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 折线图
            if (data.dates.isNotEmpty() && data.calorieIntake.isNotEmpty()) {
                val chartData = data.dates.zip(data.calorieIntake).map { (date, value) ->
                    val label = when (timeDimension) {
                        TimeDimension.DAY -> date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        TimeDimension.WEEK -> "${date.monthValue}/${date.dayOfMonth}"
                        TimeDimension.MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy/MM"))
                    }
                    label to value
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineColor = android.graphics.Color.parseColor("#2196F3")
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 运动消耗趋势图表
 */
@Composable
internal fun ExerciseTrendChart(
    data: com.calorieai.app.ui.components.charts.TrendChartData,
    timeDimension: TimeDimension,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "运动消耗趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 统计值
                val totalExercise = data.exerciseCalories.sum().toInt()
                Text(
                    text = "总计: ${totalExercise}千卡",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 折线图
            if (data.dates.isNotEmpty() && data.exerciseCalories.isNotEmpty()) {
                val chartData = data.dates.zip(data.exerciseCalories).map { (date, value) ->
                    val label = when (timeDimension) {
                        TimeDimension.DAY -> date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        TimeDimension.WEEK -> "${date.monthValue}/${date.dayOfMonth}"
                        TimeDimension.MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy/MM"))
                    }
                    label to value
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineColor = android.graphics.Color.parseColor("#4CAF50")
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 体重变化趋势图表
 */
@Composable
internal fun WeightTrendChart(
    data: com.calorieai.app.ui.components.charts.TrendChartData,
    timeDimension: TimeDimension,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "体重变化趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 提示
                Text(
                    text = "单位: kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 折线图（体重数据暂为空）
            val hasWeightData = data.weightData.any { it != null }

            if (hasWeightData) {
                val chartData = data.dates.zip(data.weightData).mapNotNull { (date, value) ->
                    value?.let {
                        val label = when (timeDimension) {
                            TimeDimension.DAY -> date.format(DateTimeFormatter.ofPattern("MM/dd"))
                            TimeDimension.WEEK -> "${date.monthValue}/${date.dayOfMonth}"
                            TimeDimension.MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy/MM"))
                        }
                        label to it
                    }
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineColor = android.graphics.Color.parseColor("#FF9800")
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无体重数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "请先在记录页添加体重",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * BMI趋势图表（由体重趋势 + 身高计算）
 */
@Composable
internal fun BMITrendChart(
    data: com.calorieai.app.ui.components.charts.TrendChartData,
    timeDimension: TimeDimension,
    userHeight: Float?,
    modifier: Modifier = Modifier
) {
    val heightM = (userHeight ?: 0f) / 100f
    val hasHeight = heightM > 0f

    val bmiPoints = if (hasHeight) {
        data.dates.zip(data.weightData).mapNotNull { (date, weight) ->
            weight?.let {
                val bmi = it / (heightM * heightM)
                val label = when (timeDimension) {
                    TimeDimension.DAY -> date.format(DateTimeFormatter.ofPattern("MM/dd"))
                    TimeDimension.WEEK -> "${date.monthValue}/${date.dayOfMonth}"
                    TimeDimension.MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy/MM"))
                }
                label to bmi
            }
        }
    } else {
        emptyList()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = Color(0xFF9C27B0),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BMI趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val latestBmi = bmiPoints.lastOrNull()?.second
                Text(
                    text = latestBmi?.let { "最新: ${String.format("%.1f", it)}" } ?: "暂无数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                !hasHeight -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "请先在个人信息中设置身高后查看BMI趋势",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                bmiPoints.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无体重记录，无法计算BMI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LineChartView(
                        data = bmiPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        lineColor = android.graphics.Color.parseColor("#9C27B0")
                    )
                }
            }
        }
    }
}

/**
 * 饮水趋势图表
 */
@Composable
internal fun WaterTrendChart(
    waterData: List<WaterTrendData>,
    modifier: Modifier = Modifier
) {
    val totalWater = waterData.sumOf { it.amount }
    val avgWater = if (waterData.isNotEmpty()) totalWater / waterData.size else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = Color(0xFF26C6DA),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "饮水趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "日均: ${avgWater}ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (waterData.isNotEmpty() && waterData.any { it.amount > 0 }) {
                val chartData = waterData.map { data ->
                    data.date.format(DateTimeFormatter.ofPattern("MM/dd")) to data.amount.toFloat()
                }

                LineChartView(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineColor = android.graphics.Color.parseColor("#26C6DA")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WaterTrendStat(
                        label = "总饮水",
                        value = "${totalWater}ml",
                        icon = "💧"
                    )
                    WaterTrendStat(
                        label = "日均",
                        value = "${avgWater}ml",
                        icon = "📊"
                    )
                    val goalDays = waterData.count { it.amount >= 2000 }
                    WaterTrendStat(
                        label = "达标天数",
                        value = "${goalDays}天",
                        icon = "🎯"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF26C6DA).copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无饮水数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "开始记录您的饮水习惯吧",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WaterTrendStat(
    label: String,
    value: String,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF26C6DA)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 饮水月度总结卡片 - 用于上月总结页面
 */
@Composable
internal fun WaterMonthlySummaryCard(
    monthlyTotal: Int,
    weeklyAverage: Float,
    targetAmount: Int
) {
    val daysInMonth = 30 // 简化处理
    val dailyAverage = if (daysInMonth > 0) monthlyTotal / daysInMonth else 0
    val goalAchievementRate = if (targetAmount > 0) (dailyAverage * 100 / targetAmount).coerceAtMost(100) else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26C6DA).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💧",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "饮水统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主要数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WaterMonthlyStat(
                    icon = "📊",
                    value = String.format("%,d", monthlyTotal),
                    label = "总饮水(ml)"
                )
                WaterMonthlyStat(
                    icon = "📈",
                    value = "${dailyAverage}",
                    label = "日均(ml)"
                )
                WaterMonthlyStat(
                    icon = "🎯",
                    value = "${goalAchievementRate}%",
                    label = "达标率"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 进度条显示达标率
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "目标达成度",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goalAchievementRate}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26C6DA)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { goalAchievementRate / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF26C6DA),
                    trackColor = Color(0xFF26C6DA).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
internal fun WaterMonthlyStat(
    icon: String,
    value: String,
    label: String
) {
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
            color = Color(0xFF26C6DA)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 日期范围选择器对话框
 */
@Composable
internal fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onReset: () -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(3)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期范围") },
        text = {
            Column {
                Text(
                    text = "开始日期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 简化版日期选择，使用预设选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "近7天" to LocalDate.now().minusDays(7),
                        "近30天" to LocalDate.now().minusDays(30),
                        "近3月" to LocalDate.now().minusMonths(3),
                        "近6月" to LocalDate.now().minusMonths(6)
                    ).forEach { (label, date) ->
                        FilterChip(
                            selected = startDate == date,
                            onClick = { startDate = date },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "结束日期: ${endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(startDate, endDate) }) {
                Text("确定")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("重置")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

/**
 * 上月总结内容 - 优化版，添加多种图表
 */
