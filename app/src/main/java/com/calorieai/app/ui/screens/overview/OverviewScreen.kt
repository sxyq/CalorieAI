package com.calorieai.app.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.ui.screens.stats.StatsViewModel
import com.calorieai.app.ui.screens.stats.StatsUiState
import com.calorieai.app.utils.HistoryStats
import com.calorieai.app.utils.TodayStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onNavigateToStats: () -> Unit = {},
    onNavigateToWeightHistory: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "概览",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 月度热力图卡片 - 使用真实数据
            HeatmapCard(isDark, uiState)

            // 月度总结卡片
            MonthlySummaryCard(
                isDark = isDark,
                totalCalories = uiState.lastMonthSummary?.totalCalories ?: 0,
                exerciseCalories = uiState.lastMonthSummary?.totalExerciseCalories ?: 0,
                weightChange = null
            )

            // 数据概览网格
            DataOverviewGrid(
                isDark = isDark,
                avgCalories = uiState.todayStats?.totalCalories ?: 0,
                avgWater = uiState.todayWaterAmount,
                currentWeight = uiState.userWeight,
                avgExercise = uiState.todayStats?.exerciseMinutes ?: 0
            )

            // 快捷入口
            QuickAccessCard(
                isDark = isDark,
                onNavigateToStats = onNavigateToStats,
                onNavigateToWeightHistory = onNavigateToWeightHistory,
                onNavigateToGoals = onNavigateToGoals
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeatmapCard(
    isDark: Boolean,
    uiState: StatsUiState
) {
    // 使用真实的每日餐次记录数据生成热力图
    val dailyMealRecords = uiState.dailyMealRecords
    
    // 计算活跃度数据（基于真实每日餐次记录）
    val activityData = remember(dailyMealRecords) {
        generateActivityDataFromDailyRecords(dailyMealRecords)
    }
    
    val activeDays = uiState.streakDays
    val todayStats = uiState.todayStats
    val totalRecords = todayStats?.recordCount ?: 0
    val activityRate = if (activeDays > 0) (activeDays * 100 / 30).coerceAtMost(100) else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月活跃度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "无",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    HeatmapLegend(isDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "全",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HeatmapGrid(isDark, activityData)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeatmapStat("连续打卡", "${activeDays}天")
                HeatmapStat("今日记录", "${totalRecords}条")
                HeatmapStat("活跃度", "${activityRate}%")
            }
        }
    }
}

// 从真实的每日餐次记录数据生成活跃度数据
// level: 0=无记录, 1=1个餐次, 2=2个餐次, 3=3个餐次, 4=4个及以上餐次
// 返回的矩阵是 [dayIndex][weekIndex] 格式，即每行是一周中的同一天（如所有周一），每列是一周
private fun generateActivityDataFromDailyRecords(
    dailyMealRecords: List<com.calorieai.app.ui.screens.stats.DailyMealRecord>
): List<List<Int>> {
    val weeks = 20
    val daysPerWeek = 7
    // 数据存储为每行代表星期几（0=周日, 1=周一...），每列代表第几周
    val data = MutableList(daysPerWeek) { MutableList(weeks) { 0 } }

    if (dailyMealRecords.isEmpty()) {
        return data
    }

    // 将记录按日期映射，    val recordsMap = dailyMealRecords.associateBy { it.date }
    val today = java.time.LocalDate.now()

    // 计算起始日期：从今天往前推 139 天，这样今天就是最后一个格子
    val startDate = today.minusDays(139)

    // 填充数据
    // 热力图显示：第0列是最早的一周，第19列是最新的一周
    // 第0行是周日，第6行是周六
    for (record in dailyMealRecords) {
        val date = record.date
        // 只处理起始日期到今天之间的记录
        if (!date.isBefore(startDate) && !date.isAfter(today)) {
            // 计算从起始日期开始的天数偏移
            val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, date).toInt()
            if (daysFromStart in 0 until 140) {
                // 计算周索引和列索引）和星期几索引（行索引）
                val weekIndex = daysFromStart / 7
                val dayIndex = daysFromStart % 7
                
                data[dayIndex][weekIndex] = record.level
            }
        }
    }

    return data
}

@Composable
private fun HeatmapLegend(isDark: Boolean) {
    // 4种颜色表示不同的餐次记录情况：
    // 0=无记录(灰色), 1=1个餐次(浅色), 2=2个餐次(中浅色), 3=3个餐次(中深色), 4=4个及以上(深色)
    val colors = if (isDark) {
        listOf(
            GlassDarkColors.SurfaceContainerHighest,                    // 0: 无记录
            GlassDarkColors.Primary.copy(alpha = 0.25f),               // 1: 1个餐次
            GlassDarkColors.Primary.copy(alpha = 0.5f),                // 2: 2个餐次
            GlassDarkColors.Primary.copy(alpha = 0.75f),               // 3: 3个餐次
            GlassDarkColors.Primary                                    // 4: 4个及以上
        )
    } else {
        listOf(
            GlassLightColors.SurfaceContainerHighest,                   // 0: 无记录
            GlassLightColors.Primary.copy(alpha = 0.25f),              // 1: 1个餐次
            GlassLightColors.Primary.copy(alpha = 0.5f),               // 2: 2个餐次
            GlassLightColors.Primary.copy(alpha = 0.75f),              // 3: 3个餐次
            GlassLightColors.Primary                                   // 4: 4个及以上
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun HeatmapGrid(
    isDark: Boolean,
    activityData: List<List<Int>> = emptyList()
) {
    val weeks = 20
    val daysPerWeek = 7
    
    // 如果没有数据，使用默认空数据
    val data = activityData.ifEmpty {
        List(daysPerWeek) { List(weeks) { 0 } }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(daysPerWeek) { dayIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(weeks) { weekIndex ->
                    val intensity = data.getOrNull(dayIndex)?.getOrNull(weekIndex) ?: 0
                    HeatmapCell(intensity, isDark)
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(intensity: Int, isDark: Boolean) {
    // 根据餐次记录数量显示不同颜色：
    // 0=无记录(灰色), 1=1个餐次(浅色), 2=2个餐次(中浅色), 3=3个餐次(中深色), 4=4个及以上(深色)
    val backgroundColor = when (intensity) {
        0 -> if (isDark) GlassDarkColors.SurfaceContainerHighest else GlassLightColors.SurfaceContainerHighest
        1 -> if (isDark) GlassDarkColors.Primary.copy(alpha = 0.25f) else GlassLightColors.Primary.copy(alpha = 0.25f)
        2 -> if (isDark) GlassDarkColors.Primary.copy(alpha = 0.5f) else GlassLightColors.Primary.copy(alpha = 0.5f)
        3 -> if (isDark) GlassDarkColors.Primary.copy(alpha = 0.75f) else GlassLightColors.Primary.copy(alpha = 0.75f)
        else -> if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    }

    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
    )
}

@Composable
private fun HeatmapStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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

@Composable
private fun MonthlySummaryCard(
    isDark: Boolean,
    totalCalories: Int,
    exerciseCalories: Int,
    weightChange: Float?
) {
    val currentMonth = java.time.LocalDate.now().monthValue
    val monthName = when(currentMonth) {
        1 -> "一月"
        2 -> "二月"
        3 -> "三月"
        4 -> "四月"
        5 -> "五月"
        6 -> "六月"
        7 -> "七月"
        8 -> "八月"
        9 -> "九月"
        10 -> "十月"
        11 -> "十一月"
        else -> "十二月"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "${monthName}总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = if (totalCalories > 0) String.format("%,d", totalCalories) else "--",
                    label = "总热量(千卡)",
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    icon = Icons.Default.DirectionsRun,
                    value = if (exerciseCalories > 0) String.format("%,d", exerciseCalories) else "--",
                    label = "运动消耗(千卡)",
                    color = MaterialTheme.colorScheme.secondary
                )
                SummaryItem(
                    icon = Icons.Default.TrendingDown,
                    value = weightChange?.let { String.format("%.1f", it) } ?: "--",
                    label = "体重变化(kg)",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DataOverviewGrid(
    isDark: Boolean,
    avgCalories: Int,
    avgWater: Int,
    currentWeight: Float?,
    avgExercise: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DataCard(
                isDark = isDark,
                icon = Icons.Default.Restaurant,
                value = if (avgCalories > 0) String.format("%,d", avgCalories) else "--",
                label = "今日摄入",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            DataCard(
                isDark = isDark,
                icon = Icons.Default.WaterDrop,
                value = if (avgWater > 0) String.format("%,d", avgWater) else "--",
                label = "今日饮水(ml)",
                color = Color(0xFF26C6DA),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DataCard(
                isDark = isDark,
                icon = Icons.Default.MonitorWeight,
                value = currentWeight?.let { String.format("%.1f", it) } ?: "--",
                label = "当前体重(kg)",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            DataCard(
                isDark = isDark,
                icon = Icons.Default.Timer,
                value = if (avgExercise > 0) "$avgExercise" else "--",
                label = "今日运动(分钟)",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DataCard(
    isDark: Boolean,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 16.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    isDark: Boolean,
    onNavigateToStats: () -> Unit,
    onNavigateToWeightHistory: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            QuickAccessItem(
                icon = Icons.Default.BarChart,
                title = "详细统计",
                subtitle = "查看完整数据分析",
                onClick = onNavigateToStats
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuickAccessItem(
                icon = Icons.Default.History,
                title = "体重历史",
                subtitle = "追踪体重变化趋势",
                onClick = onNavigateToWeightHistory
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuickAccessItem(
                icon = Icons.Default.Flag,
                title = "健康目标",
                subtitle = "管理您的健康计划",
                onClick = onNavigateToGoals
            )
        }
    }
}

@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
