package com.calorieai.app.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import java.time.LocalDate

private data class OverviewHeatmapPalette(
    val empty: Color,
    val levels: List<Color>
)

private fun overviewHeatmapPalette(isDark: Boolean): OverviewHeatmapPalette {
    return if (isDark) {
        OverviewHeatmapPalette(
            empty = Color(0xFF2D3440),
            levels = listOf(
                Color(0xFF2C455C),
                Color(0xFF1E566D),
                Color(0xFF1D6B83),
                Color(0xFF1E7C96),
                Color(0xFF218AA3),
                Color(0xFF2397A0),
                Color(0xFF23A38B),
                Color(0xFF3CB276),
                Color(0xFF62C261),
                Color(0xFF8AD850)
            )
        )
    } else {
        OverviewHeatmapPalette(
            empty = Color(0xFFE6EAF0),
            levels = listOf(
                Color(0xFFD7EEF8),
                Color(0xFFBEE4F5),
                Color(0xFFA8DBF2),
                Color(0xFF8FD1ED),
                Color(0xFF7CC8E6),
                Color(0xFF63BCDE),
                Color(0xFF4EAFD8),
                Color(0xFF3EA6D0),
                Color(0xFF238BC0),
                Color(0xFF0E5A8A)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onNavigateToStats: () -> Unit = {},
    onNavigateToWeightHistory: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeatmapCard(isDark, uiState) }
            item {
                MonthlySummaryCard(
                    isDark = isDark,
                    totalCalories = uiState.lastMonthSummary?.totalCalories ?: 0,
                    exerciseCalories = uiState.lastMonthSummary?.totalExerciseCalories ?: 0,
                    weightChange = null
                )
            }
            item {
                DataOverviewGrid(
                    isDark = isDark,
                    avgCalories = uiState.todayStats?.totalCalories ?: 0,
                    avgWater = uiState.todayWaterAmount,
                    currentWeight = uiState.userWeight,
                    avgExercise = uiState.todayStats?.exerciseMinutes ?: 0
                )
            }
            item {
                CheckInIncentiveCard(
                    isDark = isDark,
                    streakDays = uiState.streakDays,
                    weeklyGoalDays = uiState.weeklyGoalDays,
                    weeklyActiveDays = uiState.weeklyActiveDays,
                    weeklyRecordCount = uiState.weeklyRecordCount,
                    achievements = uiState.achievementBadges
                )
            }
            item {
                FoodRecordInfoTablesCard(
                    isDark = isDark,
                    tableRows = uiState.foodRecordTableRows,
                    topFoods = uiState.topFoodRows
                )
            }
            item {
                RecipeDataOverviewCard(
                    isDark = isDark,
                    recipeStats = uiState.recipeStats
                )
            }
            item {
                QuickAccessCard(
                    isDark = isDark,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToWeightHistory = onNavigateToWeightHistory,
                    onNavigateToGoals = onNavigateToGoals
                )
            }
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
    
    val activeDays = uiState.monthlyActiveDays
    val totalRecords = remember(dailyMealRecords) {
        dailyMealRecords.firstOrNull { it.date == LocalDate.now() }?.recordCount ?: 0
    }
    val displayActivityData = remember(activityData, totalRecords) {
        mergeTodayRecordToHeatmap(activityData, totalRecords)
    }
    val daysElapsed = LocalDate.now().dayOfMonth.coerceAtLeast(1)
    val activityRate = if (activeDays > 0) {
        (activeDays * 100 / daysElapsed).coerceIn(0, 100)
    } else {
        0
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

            HeatmapGrid(isDark, displayActivityData)

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

private fun mergeTodayRecordToHeatmap(
    baseData: List<List<Int>>,
    todayRecordCount: Int
): List<List<Int>> {
    if (baseData.isEmpty() || todayRecordCount <= 0) return baseData
    if (baseData.size < 7 || baseData.any { it.size < 20 }) return baseData

    val today = LocalDate.now()
    val dayIndex = today.dayOfWeek.value % 7
    val weekIndex = 19 // 20周热力图的最后一列即当前周

    val targetLevel = todayRecordCount.coerceIn(1, 10)
    val mutable = baseData.map { it.toMutableList() }.toMutableList()
    mutable[dayIndex][weekIndex] = maxOf(mutable[dayIndex][weekIndex], targetLevel)
    return mutable
}

// 从真实的每日餐次记录数据生成活跃度数据
// level: 0=无记录, 1~10=记录强度
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

    val today = java.time.LocalDate.now()
    // 以“本周周日”为锚点，确保最后一列是当前周，并且行索引对应真实星期
    val currentWeekStart = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val firstWeekStart = currentWeekStart.minusWeeks((weeks - 1).toLong())
    val totalDays = weeks * daysPerWeek
    val endDate = firstWeekStart.plusDays((totalDays - 1).toLong())

    dailyMealRecords.forEach { record ->
        val date = record.date
        if (date.isBefore(firstWeekStart) || date.isAfter(endDate) || date.isAfter(today)) {
            return@forEach
        }

        val daysBetween = (date.toEpochDay() - firstWeekStart.toEpochDay()).toInt()
        val weekIndex = daysBetween / daysPerWeek
        val dayIndex = date.dayOfWeek.value % 7 // 周日=0 ... 周六=6
        data[dayIndex][weekIndex] = record.level
    }

    return data
}

@Composable
private fun HeatmapLegend(isDark: Boolean) {
    val palette = overviewHeatmapPalette(isDark)
    // 0=无记录，1~10逐级增强
    val colors = listOf(palette.empty) + palette.levels

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
    val palette = overviewHeatmapPalette(isDark)
    val backgroundColor = if (intensity <= 0) {
        palette.empty
    } else {
        palette.levels[(intensity - 1).coerceIn(0, palette.levels.lastIndex)]
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
    val today = LocalDate.now()

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
                text = "本月总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "统计范围：${today.withDayOfMonth(1)} 至 $today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    isDark = isDark,
                    icon = Icons.Default.LocalFireDepartment,
                    value = if (totalCalories > 0) String.format("%,d", totalCalories) else "--",
                    label = "总热量(千卡)",
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    isDark = isDark,
                    icon = Icons.Default.DirectionsRun,
                    value = if (exerciseCalories > 0) String.format("%,d", exerciseCalories) else "--",
                    label = "运动消耗(千卡)",
                    color = MaterialTheme.colorScheme.secondary
                )
                SummaryItem(
                    isDark = isDark,
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
    isDark: Boolean,
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
                .background(color.copy(alpha = if (isDark) 0.28f else 0.15f)),
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
                .background(color.copy(alpha = if (isDark) 0.26f else 0.15f)),
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
private fun CheckInIncentiveCard(
    isDark: Boolean,
    streakDays: Int,
    weeklyGoalDays: Int,
    weeklyActiveDays: Int,
    weeklyRecordCount: Int,
    achievements: List<com.calorieai.app.ui.screens.stats.AchievementBadge>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 16.dp
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "连续记录成长",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "本周目标 ${weeklyActiveDays}/${weeklyGoalDays} 天 · 已连续记录 ${streakDays} 天 · 本周记录 ${weeklyRecordCount} 条",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            achievements.forEach { badge ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (badge.achieved) "已达成 (${badge.progress})" else "进度 ${badge.progress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (badge.achieved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodRecordInfoTablesCard(
    isDark: Boolean,
    tableRows: List<com.calorieai.app.ui.screens.stats.FoodRecordTableRow>,
    topFoods: List<com.calorieai.app.ui.screens.stats.TopFoodRow>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 16.dp
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "饮食记录信息栏",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "表1：当日餐次记录汇总",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            FoodRecordSummaryTable(rows = tableRows)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

            Text(
                text = "表2：近14天高频食物",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            TopFoodTable(rows = topFoods)
        }
    }
}

@Composable
private fun RecipeDataOverviewCard(
    isDark: Boolean,
    recipeStats: com.calorieai.app.ui.screens.stats.RecipeStats
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 16.dp
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "菜谱相关统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TableBodyCell("已有食材：${recipeStats.pantryCount}项", Modifier.weight(1f))
                TableBodyCell("收藏菜谱：${recipeStats.favoriteCount}条", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TableBodyCell("使用总次数：${recipeStats.favoriteUseCount}次", Modifier.weight(1f))
                TableBodyCell("即将过期：${recipeStats.pantryExpiringSoonCount}项", Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

            val mostUsed = recipeStats.mostUsedFavoriteName
                ?.takeIf { it.isNotBlank() }
                ?.let { "$it（${recipeStats.mostUsedFavoriteUseCount}次）" }
                ?: "暂无使用记录"
            Text(
                text = "最常用收藏菜谱：$mostUsed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoodRecordSummaryTable(
    rows: List<com.calorieai.app.ui.screens.stats.FoodRecordTableRow>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            TableHeaderCell("餐次", Modifier.weight(1.2f))
            TableHeaderCell("条数", Modifier.weight(0.8f))
            TableHeaderCell("热量", Modifier.weight(1f))
            TableHeaderCell("蛋白", Modifier.weight(1f))
        }
        if (rows.isEmpty()) {
            Text(
                text = "当日暂无饮食记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    TableBodyCell(row.mealType, Modifier.weight(1.2f))
                    TableBodyCell("${row.count}", Modifier.weight(0.8f))
                    TableBodyCell("${row.calories}", Modifier.weight(1f))
                    TableBodyCell("${row.protein.toInt()}g", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TopFoodTable(
    rows: List<com.calorieai.app.ui.screens.stats.TopFoodRow>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            TableHeaderCell("食物", Modifier.weight(1.6f))
            TableHeaderCell("次数", Modifier.weight(0.8f))
            TableHeaderCell("总热量", Modifier.weight(1f))
            TableHeaderCell("最近", Modifier.weight(1f))
        }
        if (rows.isEmpty()) {
            Text(
                text = "近14天暂无可统计食物",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    TableBodyCell(row.foodName, Modifier.weight(1.6f))
                    TableBodyCell("${row.count}", Modifier.weight(0.8f))
                    TableBodyCell("${row.totalCalories}", Modifier.weight(1f))
                    TableBodyCell(row.lastRecordDate.toString().substring(5), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Text(
        text = text,
        modifier = modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelSmall,
        color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun TableBodyCell(text: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Text(
        text = text,
        modifier = modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
    )
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
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.12f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuickAccessItem(
                icon = Icons.Default.History,
                title = "体重历史",
                subtitle = "追踪体重变化趋势",
                onClick = onNavigateToWeightHistory
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.12f),
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
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
                .background(
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = if (isDark) 0.24f else 0.1f
                    )
                ),
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
