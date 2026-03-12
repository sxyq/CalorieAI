package com.calorieai.app.ui.screens.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Monitor
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
                    1 -> TrendAnalysisContent(uiState)
                    2 -> MonthlySummaryContent(uiState)
                }
            }
        }
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
            .fadingTopEdge(height = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // 今日摄入状态统计卡片
        item {
            AnimatedListItem(index = 0) {
                TodayStatsCard(uiState.todayStats)
            }
        }

        // 各餐次摄入统计卡片
        item {
            AnimatedListItem(index = 1) {
                MealTypeStatsCard(uiState.mealTypeStats)
            }
        }

        // 历史摄入统计卡片
        item {
            AnimatedListItem(index = 2) {
                HistoryStatsCard(uiState.historyStats)
            }
        }

        // 连续记录天数
        item {
            AnimatedListItem(index = 3) {
                StreakCard(uiState.streakDays)
            }
        }
    }
}

/**
 * 今日统计卡片
 */
@Composable
private fun TodayStatsCard(stats: TodayStats?) {
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
                text = "今日摄入状态统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "今日已摄入",
                    value = stats?.totalCalories?.toString() ?: "0",
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "剩余可摄入",
                    value = stats?.remainingCalories?.toString() ?: "0",
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "今日目标",
                    value = stats?.targetCalories?.toString() ?: "2000",
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.tertiary
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
                    value = stats?.targetMetDays?.toString() ?: "0",
                    unit = "天",
                    color = androidx.compose.ui.graphics.Color(0xFF82ABA3)
                )
                StatItem(
                    label = "超标天数",
                    value = stats?.overTargetDays?.toString() ?: "0",
                    unit = "天",
                    color = androidx.compose.ui.graphics.Color(0xFFF77E66)
                )
                StatItem(
                    label = "记录天数",
                    value = stats?.totalDays?.toString() ?: "0",
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
private fun TrendAnalysisContent(uiState: StatsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
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
 * 上月总结内容
 */
@Composable
private fun MonthlySummaryContent(uiState: StatsUiState) {
    val summary = uiState.lastMonthSummary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // 顶部大图卡片
        SummaryHeaderCard(summary)

        // 统计指标网格
        SummaryMetricsGrid(summary)
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
                    text = "上月总结",
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
    val metrics = listOf(
        "总摄入" to "${summary?.totalCalories ?: 0}千卡",
        "日均摄入" to "${summary?.avgDailyCalories ?: 0}千卡",
        "最高单日" to "${summary?.maxDailyCalories ?: 0}千卡",
        "达标天数" to "${summary?.targetMetDays ?: 0}天",
        "超标天数" to "${summary?.overTargetDays ?: 0}天",
        "记录总数" to "${summary?.totalRecords ?: 0}条",
        "早餐总计" to "${summary?.breakfastTotal ?: 0}千卡",
        "午餐总计" to "${summary?.lunchTotal ?: 0}千卡",
        "晚餐总计" to "${summary?.dinnerTotal ?: 0}千卡",
        "加餐总计" to "${summary?.snackTotal ?: 0}千卡"
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMetrics.forEach { (label, value) ->
                    SummaryMetricCard(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 统计指标卡片
 */
@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
