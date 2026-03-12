package com.calorieai.app.ui.screens.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.AnimatedListItem
import com.calorieai.app.ui.components.fadingTopEdge
import com.calorieai.app.utils.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 统计页面（参考Deadliner风格）
 * 三标签设计：概览统计 / 趋势分析 / 上月总结
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val tabs = listOf("概览统计", "趋势分析", "上月总结")
    val tabIcons = listOf(Icons.Default.Analytics, Icons.Default.Monitor, Icons.Default.Dashboard)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("概览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 趋势分析页面显示日期筛选按钮
                    if (selectedTab == 1) {
                        IconButton(onClick = { showDateRangePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "日期筛选")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标签栏
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tabIcons[index], contentDescription = null) },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            // 内容区域
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(200))
                },
                label = "StatsContent"
            ) { tab ->
                when (tab) {
                    0 -> OverviewStatsContent(uiState)
                    1 -> TrendAnalysisContent(
                        uiState = uiState,
                        onDateRangeSelected = { start, end ->
                            viewModel.setTrendDateRange(start, end)
                        }
                    )
                    2 -> MonthlySummaryContent(
                        uiState = uiState,
                        onMonthChange = { offset ->
                            viewModel.changeMonth(offset)
                        }
                    )
                }
            }
        }
    }

    // 日期范围选择器
    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onConfirm = { start, end ->
                viewModel.setTrendDateRange(start, end)
                showDateRangePicker = false
            },
            onReset = {
                viewModel.resetTrendDateRange()
                showDateRangePicker = false
            }
        )
    }
}

/**
 * 概览统计内容
 */
@Composable
private fun OverviewStatsContent(uiState: StatsUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadingTopEdge()
    ) {
        // 今日统计卡片
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 0) {
                    TodayStatsCard(stats = stats)
                }
            }
        }

        // 餐次统计
        item {
            AnimatedListItem(index = 1) {
                MealTypeStatsCard(stats = uiState.mealTypeStats)
            }
        }

        // 历史统计
        item {
            uiState.historyStats?.let { stats ->
                AnimatedListItem(index = 2) {
                    HistoryStatsCard(stats = stats)
                }
            }
        }

        // 连续记录
        item {
            AnimatedListItem(index = 3) {
                StreakCard(streakDays = uiState.streakDays)
            }
        }
    }
}

/**
 * 今日统计卡片（优化布局）
 */
@Composable
private fun TodayStatsCard(stats: TodayStats) {
    val progress = (stats.totalCalories.toFloat() / stats.targetCalories).coerceIn(0f, 1f)
    val remaining = stats.remainingCalories

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                text = "今日摄入状态",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 三列布局：已摄入 | 目标 | 剩余
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "已摄入",
                    value = stats.totalCalories.toString(),
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "目标",
                    value = stats.targetCalories.toString(),
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatItem(
                    label = "剩余",
                    value = remaining.toString(),
                    unit = "千卡",
                    color = if (remaining < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    progress > 1f -> MaterialTheme.colorScheme.error
                    progress > 0.8f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // 达标提示
            if (stats.isTargetMet) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✓ 今日热量控制良好",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color
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
            style = MaterialTheme.typography.headlineMedium,
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
 * 餐次统计卡片
 */
@Composable
private fun MealTypeStatsCard(stats: Map<com.calorieai.app.data.model.MealType, Int>) {
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
                text = "各餐次摄入统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 简化的条形图展示
            stats.forEach { (mealType, calories) ->
                MealBar(
                    label = when (mealType) {
                        com.calorieai.app.data.model.MealType.BREAKFAST -> "早餐"
                        com.calorieai.app.data.model.MealType.LUNCH -> "午餐"
                        com.calorieai.app.data.model.MealType.DINNER -> "晚餐"
                        com.calorieai.app.data.model.MealType.SNACK -> "加餐"
                    },
                    calories = calories,
                    maxCalories = stats.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 餐次条形图
 */
@Composable
private fun MealBar(
    label: String,
    calories: Int,
    maxCalories: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (calories > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(calories.toFloat() / maxCalories)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${calories}千卡",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 历史统计卡片
 */
@Composable
private fun HistoryStatsCard(stats: HistoryStats?) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "摄入状态统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "达标天数",
                    value = stats?.targetMetDays.toString(),
                    unit = "天",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "超标天数",
                    value = stats?.overTargetDays.toString(),
                    unit = "天",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    label = "记录天数",
                    value = stats?.totalDays.toString(),
                    unit = "天",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 连续记录卡片
 */
@Composable
private fun StreakCard(streakDays: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "连续记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "保持好习惯，继续加油！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "${streakDays}天",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 趋势分析内容
 */
@Composable
private fun TrendAnalysisContent(
    uiState: StatsUiState,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // 日期范围显示
        if (uiState.trendStartDate != null && uiState.trendEndDate != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.trendStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} 至 ${uiState.trendEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 周趋势卡片
        WeeklyTrendCard(uiState.weeklyStats)

        // 月度趋势卡片
        MonthlyTrendCard(uiState.monthlyStats)
    }
}

/**
 * 周趋势卡片
 */
@Composable
private fun WeeklyTrendCard(stats: List<WeeklyStat>) {
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

            // 简化的折线展示
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 月度趋势卡片
 */
@Composable
private fun MonthlyTrendCard(stats: List<MonthlyStat>) {
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 日期范围选择器对话框
 */
@Composable
private fun DateRangePickerDialog(
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
 * 上月总结内容
 */
@Composable
private fun MonthlySummaryContent(
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

        // 顶部大图卡片
        SummaryHeaderCard(summary)

        // 统计指标网格
        SummaryMetricsGrid(summary)
    }
}

/**
 * 月份选择器
 */
@Composable
private fun MonthSelector(
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
private fun SummaryHeaderCard(summary: MonthSummary?) {
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
                    text = "${summary?.year ?: "2024"}年",
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
private fun SummaryMetricsGrid(summary: MonthSummary?) {
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
        }
    }
}

/**
 * 总结指标项
 */
@Composable
private fun SummaryMetricItem(
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
