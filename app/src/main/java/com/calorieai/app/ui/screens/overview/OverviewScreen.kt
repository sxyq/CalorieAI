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
import com.calorieai.app.ui.components.CompactHeatmap
import com.calorieai.app.ui.components.HeatmapLegend
import com.calorieai.app.ui.screens.stats.StatsHeatmapMapper
import com.calorieai.app.ui.screens.stats.StatsViewModel
import com.calorieai.app.ui.screens.stats.StatsUiState
import com.calorieai.app.utils.HistoryStats
import com.calorieai.app.utils.TodayStats
import java.time.LocalDate

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
                        text = "姒傝",
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
                    avgExercise = uiState.todayStats?.exerciseMinutes ?: 0,
                    showWaterFeatures = uiState.showWaterFeatures
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
    val totalRecords = uiState.todayStats?.recordCount ?: 0
    val dailyMealRecords = uiState.dailyMealRecords
    val heatmapData = remember(dailyMealRecords, totalRecords) {
        StatsHeatmapMapper.toHeatmapDataWithTodayOverride(
            dailyMealRecords = dailyMealRecords,
            todayRecordCount = totalRecords
        )
    }

    val activeDays = uiState.monthlyActiveDays
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
                    text = "鏈湀娲昏穬搴",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                HeatmapLegend(
                    labels = listOf("None", "Full"),
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CompactHeatmap(
                data = heatmapData,
                weeks = 20,
                cellSize = 14,
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeatmapStat("杩炵画鎵撳崱", "${activeDays}澶")
                HeatmapStat("浠婃棩璁板綍", "${totalRecords}鏉")
                HeatmapStat("娲昏穬搴", "${activityRate}%")
            }
        }
    }
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
                text = "鏈湀鎬荤粨",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "缁熻鑼冨洿锛?{today.withDayOfMonth(1)} 鑷?$today",
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
                    label = "鎬荤儹閲?鍗冨崱)",
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    isDark = isDark,
                    icon = Icons.Default.DirectionsRun,
                    value = if (exerciseCalories > 0) String.format("%,d", exerciseCalories) else "--",
                    label = "杩愬姩娑堣€?鍗冨崱)",
                    color = MaterialTheme.colorScheme.secondary
                )
                SummaryItem(
                    isDark = isDark,
                    icon = Icons.Default.TrendingDown,
                    value = weightChange?.let { String.format("%.1f", it) } ?: "--",
                    label = "浣撻噸鍙樺寲(kg)",
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
    avgExercise: Int,
    showWaterFeatures: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showWaterFeatures) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.Restaurant,
                    value = if (avgCalories > 0) String.format("%,d", avgCalories) else "--",
                    label = "浠婃棩鎽勫叆",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.WaterDrop,
                    value = if (avgWater > 0) String.format("%,d", avgWater) else "--",
                    label = "浠婃棩楗按(ml)",
                    color = Color(0xFF26C6DA),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.MonitorWeight,
                    value = currentWeight?.let { String.format("%.1f", it) } ?: "--",
                    label = "褰撳墠浣撻噸(kg)",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.Timer,
                    value = if (avgExercise > 0) "$avgExercise" else "--",
                    label = "浠婃棩杩愬姩(鍒嗛挓)",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.Restaurant,
                    value = if (avgCalories > 0) String.format("%,d", avgCalories) else "--",
                    label = "浠婃棩鎽勫叆",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    isDark = isDark,
                    icon = Icons.Default.MonitorWeight,
                    value = currentWeight?.let { String.format("%.1f", it) } ?: "--",
                    label = "褰撳墠浣撻噸(kg)",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            DataCard(
                isDark = isDark,
                icon = Icons.Default.Timer,
                value = if (avgExercise > 0) "$avgExercise" else "--",
                label = "浠婃棩杩愬姩(鍒嗛挓)",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
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
                text = "杩炵画璁板綍鎴愰暱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "鏈懆鐩爣 ${weeklyActiveDays}/${weeklyGoalDays} 澶?路 宸茶繛缁褰?${streakDays} 澶?路 鏈懆璁板綍 ${weeklyRecordCount} 鏉",
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
                        text = if (badge.achieved) "宸茶揪鎴?(${badge.progress})" else "杩涘害 ${badge.progress}",
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
                text = "楗璁板綍淇℃伅鏍",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "琛?锛氬綋鏃ラ娆¤褰曟眹鎬",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            FoodRecordSummaryTable(rows = tableRows)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

            Text(
                text = "琛?锛氳繎14澶╅珮棰戦鐗",
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
                text = "鑿滆氨鐩稿叧缁熻",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TableBodyCell("已有食材：${recipeStats.pantryCount}项", Modifier.weight(1f))
                TableBodyCell("收藏菜谱：${recipeStats.favoriteCount}条", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TableBodyCell("浣跨敤鎬绘鏁帮細${recipeStats.favoriteUseCount}娆", Modifier.weight(1f))
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
            TableHeaderCell("椁愭", Modifier.weight(1.2f))
            TableHeaderCell("鏉℃暟", Modifier.weight(0.8f))
            TableHeaderCell("鐑噺", Modifier.weight(1f))
            TableHeaderCell("铔嬬櫧", Modifier.weight(1f))
        }
        if (rows.isEmpty()) {
            Text(
                text = "褰撴棩鏆傛棤楗璁板綍",
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
            TableHeaderCell("椋熺墿", Modifier.weight(1.6f))
            TableHeaderCell("娆℃暟", Modifier.weight(0.8f))
            TableHeaderCell("鎬荤儹閲", Modifier.weight(1f))
            TableHeaderCell("鏈€杩", Modifier.weight(1f))
        }
        if (rows.isEmpty()) {
            Text(
                text = "杩?4澶╂殏鏃犲彲缁熻椋熺墿",
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
                title = "璇︾粏缁熻",
                subtitle = "鏌ョ湅瀹屾暣鏁版嵁鍒嗘瀽",
                onClick = onNavigateToStats
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.12f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuickAccessItem(
                icon = Icons.Default.History,
                title = "浣撻噸鍘嗗彶",
                subtitle = "杩借釜浣撻噸鍙樺寲瓒嬪娍",
                onClick = onNavigateToWeightHistory
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.12f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuickAccessItem(
                icon = Icons.Default.Flag,
                title = "鍋ュ悍鐩爣",
                subtitle = "绠＄悊鎮ㄧ殑鍋ュ悍璁″垝",
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

