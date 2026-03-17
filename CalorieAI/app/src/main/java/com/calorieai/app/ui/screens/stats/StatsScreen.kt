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

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        )
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("概览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 日期筛选按钮已移至趋势分析页面内部
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
                    0 -> OverviewStatsContent(
                        uiState = uiState,
                        onDateSelected = { date ->
                            viewModel.setOverviewDate(date)
                        }
                    )
                    1 -> TrendAnalysisContent(
                        uiState = uiState,
                        onDateRangeSelected = { start, end ->
                            viewModel.setTrendDateRange(start, end)
                        },
                        onTimeDimensionChange = { dimension ->
                            viewModel.setTrendTimeDimension(dimension)
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
    }
}

/**
 * 概览统计内容
 */
@Composable
private fun OverviewStatsContent(
    uiState: StatsUiState,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadingTopEdge()
    ) {
        // 日期选择器 - 使用新的滑动日期选择器
        item {
            ElegantDateSelector(
                selectedDate = uiState.selectedOverviewDate,
                onDateSelected = onDateSelected
            )
        }

        // 今日统计卡片
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 0) {
                    TodayStatsCard(stats = stats)
                }
            }
        }

        // 今日运动统计卡片
        item {
            uiState.todayStats?.let { stats ->
                if (stats.exerciseCount > 0) {
                    AnimatedListItem(index = 1) {
                        ExerciseStatsCard(stats = stats)
                    }
                }
            }
        }

        // 今日饮水统计卡片
        item {
            AnimatedListItem(index = 2) {
                WaterStatsCard(
                    todayAmount = uiState.todayWaterAmount,
                    targetAmount = uiState.waterTargetAmount,
                    weeklyAverage = uiState.weeklyWaterAverage
                )
            }
        }

        // 餐次统计
        item {
            AnimatedListItem(index = 3) {
                MealTypeStatsCard(stats = uiState.mealTypeStats)
            }
        }

        // 历史统计
        item {
            uiState.historyStats?.let { stats ->
                AnimatedListItem(index = 4) {
                    HistoryStatsCard(stats = stats)
                }
            }
        }

        // 连续记录
        item {
            AnimatedListItem(index = 5) {
                StreakCard(streakDays = uiState.streakDays)
            }
        }

        // 详细营养素统计表
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 6) {
                    DetailedNutritionStatsCard(stats = stats)
                }
            }
        }
    }
}

/**
 * 今日统计卡片（带饼状图）
 */
@Composable
private fun TodayStatsCard(stats: TodayStats) {
    val progress = (stats.totalCalories.toFloat() / stats.targetCalories).coerceIn(0f, 1f)
    val remaining = stats.remainingCalories

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                blurRadius = 40f
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

            Spacer(modifier = Modifier.height(16.dp))

            // 饼状图展示营养素分布
            if (stats.totalCalories > 0) {
                val nutritionData = listOf(
                    "蛋白质" to stats.proteinGrams * 4f,
                    "碳水" to stats.carbsGrams * 4f,
                    "脂肪" to stats.fatGrams * 9f
                ).filter { it.second > 0 }

                if (nutritionData.isNotEmpty()) {
                    PieChartView(
                        data = nutritionData,
                        colors = ChartColors.NUTRITION,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        centerText = "${stats.totalCalories}\n千卡"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 营养素详情
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionItem("蛋白质", stats.proteinGrams, "g", ChartColors.NUTRITION[0])
                    NutritionItem("碳水", stats.carbsGrams, "g", ChartColors.NUTRITION[1])
                    NutritionItem("脂肪", stats.fatGrams, "g", ChartColors.NUTRITION[2])
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
            }

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

            Spacer(modifier = Modifier.height(12.dp))
            
            // 基础代谢数据（如果有）
            if (stats.bmr > 0) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "基础代谢",
                        value = stats.bmr.toString(),
                        unit = "千卡",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatItem(
                        label = "总消耗",
                        value = stats.tdee.toString(),
                        unit = "千卡",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    val netCalories = stats.totalCalories - stats.exerciseCalories - stats.bmr
                    StatItem(
                        label = "热量差",
                        value = netCalories.toString(),
                        unit = "千卡",
                        color = when {
                            netCalories > 500 -> MaterialTheme.colorScheme.error
                            netCalories < -500 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

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
 * 今日运动统计卡片
 */
@Composable
private fun ExerciseStatsCard(stats: TodayStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
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
                        text = "💪",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日运动",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${stats.exerciseCount} 次",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 运动数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "消耗热量",
                    value = stats.exerciseCalories.toString(),
                    unit = "千卡",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "运动时长",
                    value = stats.exerciseMinutes.toString(),
                    unit = "分钟",
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatItem(
                    label = "净摄入",
                    value = (stats.totalCalories - stats.exerciseCalories).toString(),
                    unit = "千卡",
                    color = if (stats.totalCalories - stats.exerciseCalories <= stats.targetCalories) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 今日饮水统计卡片
 */
@Composable
private fun WaterStatsCard(
    todayAmount: Int,
    targetAmount: Int,
    weeklyAverage: Float
) {
    val progress = (todayAmount.toFloat() / targetAmount).coerceIn(0f, 1f)
    val remaining = (targetAmount - todayAmount).coerceAtLeast(0)
    val isGoalMet = todayAmount >= targetAmount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = androidx.compose.ui.graphics.Color(0xFF26C6DA).copy(alpha = 0.15f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF26C6DA),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "今日饮水",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isGoalMet) {
                    Text(
                        text = "✓ 已达标",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = androidx.compose.ui.graphics.Color(0xFF26C6DA),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "已饮水",
                    value = todayAmount.toString(),
                    unit = "ml",
                    color = androidx.compose.ui.graphics.Color(0xFF26C6DA)
                )
                StatItem(
                    label = "目标",
                    value = targetAmount.toString(),
                    unit = "ml",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatItem(
                    label = "还需",
                    value = remaining.toString(),
                    unit = "ml",
                    color = if (remaining > 0) MaterialTheme.colorScheme.primary 
                           else androidx.compose.ui.graphics.Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 周平均
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7日平均: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.0f", weeklyAverage)} ml",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color(0xFF26C6DA)
                )
            }
        }
    }
}

/**
 * 营养素项
 */
@Composable
private fun NutritionItem(
    label: String,
    value: Float,
    unit: String,
    color: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color(color),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${value.toInt()}$unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
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
 * 餐次统计卡片 - 只显示有数据的餐次，使用现代网格布局
 */
@Composable
private fun MealTypeStatsCard(stats: Map<com.calorieai.app.data.model.MealType, Int>) {
    // 合并加餐数据，只保留有数据的餐次
    val activeStats = remember(stats) {
        val merged = mutableMapOf<com.calorieai.app.data.model.MealType, Int>()
        
        stats.forEach { (mealType, calories) ->
            if (calories > 0) { // 只处理有数据的餐次
                val simplifiedType = when (mealType) {
                    com.calorieai.app.data.model.MealType.BREAKFAST_SNACK,
                    com.calorieai.app.data.model.MealType.LUNCH_SNACK,
                    com.calorieai.app.data.model.MealType.DINNER_SNACK,
                    com.calorieai.app.data.model.MealType.SNACK -> 
                        com.calorieai.app.data.model.MealType.SNACK
                    else -> mealType
                }
                merged[simplifiedType] = (merged[simplifiedType] ?: 0) + calories
            }
        }
        
        // 按顺序排序
        merged.toList().sortedBy { 
            com.calorieai.app.data.model.getSimplifiedMealTypeOrder(it.first) 
        }
    }
    
    val totalCalories = activeStats.sumOf { it.second }
    val maxCalories = activeStats.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日摄入分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${totalCalories} 千卡",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeStats.isEmpty()) {
                // 没有数据时显示提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无餐次记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 动态网格布局 - 根据数量决定每行显示几个
                val rows = when (activeStats.size) {
                    1 -> listOf(activeStats.take(1))
                    2 -> listOf(activeStats.take(2))
                    3 -> listOf(activeStats.take(2), activeStats.drop(2).take(1))
                    else -> listOf(activeStats.take(2), activeStats.drop(2).take(2))
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rows.forEach { rowStats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowStats.forEach { (mealType, calories) ->
                                MealTypeGridItem(
                                    mealType = mealType,
                                    calories = calories,
                                    maxCalories = maxCalories,
                                    totalCalories = totalCalories,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 补齐空位保持对齐
                            if (rowStats.size < 2 && activeStats.size > 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 餐次网格项 - 现代化卡片设计
 */
@Composable
private fun MealTypeGridItem(
    mealType: com.calorieai.app.data.model.MealType,
    calories: Int,
    maxCalories: Int,
    totalCalories: Int,
    modifier: Modifier = Modifier
) {
    val mealName = com.calorieai.app.data.model.getSimplifiedMealTypeName(mealType)
    val progress = if (maxCalories > 0) calories.toFloat() / maxCalories else 0f
    val percentage = if (totalCalories > 0) (calories * 100 / totalCalories) else 0
    
    // 餐次对应的颜色
    val mealColor = when (mealType) {
        com.calorieai.app.data.model.MealType.BREAKFAST -> Color(0xFFFF9F43)
        com.calorieai.app.data.model.MealType.LUNCH -> Color(0xFFFF6B6B)
        com.calorieai.app.data.model.MealType.DINNER -> Color(0xFF48DBFB)
        else -> Color(0xFF1DD1A1)
    }
    
    Box(
        modifier = modifier.liquidGlass(
            shape = RoundedCornerShape(16.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 餐次名称和占比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 颜色指示点
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(mealColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mealName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (calories > 0) {
                    Text(
                        text = "${percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 热量数值
            Text(
                text = "${calories}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (calories > 0) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "千卡",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = mealColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
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
 * 详细营养素统计卡片 - 展示13种营养素的摄入情况
 */
@Composable
private fun DetailedNutritionStatsCard(
    stats: TodayStats,
    viewModel: StatsViewModel = hiltViewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 从ViewModel获取用户身体数据
    val uiState by viewModel.uiState.collectAsState()

    // 构建用户身体数据
    val userProfile by remember(uiState.userWeight, uiState.userGender, uiState.userAge, uiState.userActivityLevel) {
        derivedStateOf {
            UserBodyProfile(
                weight = uiState.userWeight,
                gender = uiState.userGender,
                age = uiState.userAge,
                activityLevel = uiState.userActivityLevel
            )
        }
    }

    // 计算个性化营养素参考值
    val nutritionReferences by remember(userProfile) {
        derivedStateOf {
            NutritionCalculator.calculateAll(userProfile)
        }
    }

    // 构建营养素数据列表
    val nutritionItems = listOf(
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "protein" }!!,
            currentValue = stats.proteinGrams,
            emoji = "💪"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "carbs" }!!,
            currentValue = stats.carbsGrams,
            emoji = "🍞"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "fat" }!!,
            currentValue = stats.fatGrams,
            emoji = "🥑"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "fiber" }!!,
            currentValue = stats.fiberGrams,
            emoji = "🌾"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "sugar" }!!,
            currentValue = stats.sugarGrams,
            emoji = "🍯"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "sodium" }!!,
            currentValue = stats.sodiumMg,
            emoji = "🧂"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "cholesterol" }!!,
            currentValue = stats.cholesterolMg,
            emoji = "🥚"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "saturated_fat" }!!,
            currentValue = stats.saturatedFatGrams,
            emoji = "🧈"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "calcium" }!!,
            currentValue = stats.calciumMg,
            emoji = "🥛"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "iron" }!!,
            currentValue = stats.ironMg,
            emoji = "🥩"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "vitamin_c" }!!,
            currentValue = stats.vitaminCMg,
            emoji = "🍊"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "vitamin_a" }!!,
            currentValue = stats.vitaminAMcg,
            emoji = "🥕"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "potassium" }!!,
            currentValue = stats.potassiumMg,
            emoji = "🍌"
        )
    )

    // 基础营养素（始终显示）
    val basicNutritionItems = nutritionItems.take(3)
    // 扩展营养素（可展开）
    val extendedNutritionItems = nutritionItems.drop(3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🥗",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "营养素摄入",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 展开/收起按钮
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 基础营养素（始终显示）
            basicNutritionItems.forEach { item ->
                NutritionProgressRow(item = item)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 扩展营养素（可展开）
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    extendedNutritionItems.forEach { item ->
                        NutritionProgressRow(item = item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // 提示文字
            if (!isExpanded) {
                Text(
                    text = "点击展开查看全部13种营养素",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * 营养素进度行
 */
@Composable
private fun NutritionProgressRow(item: NutritionItemData) {
    val progress = (item.currentValue / item.reference.dailyRecommended).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val statusColor = when {
        progress < 0.3f -> MaterialTheme.colorScheme.error
        progress < 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.reference.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.currentValue.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "/${item.reference.dailyRecommended.toInt()}${item.reference.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 进度条
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // 百分比提示
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * 营养素数据项
 */
private data class NutritionItemData(
    val reference: NutritionReference,
    val currentValue: Float,
    val emoji: String
)

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
private fun TrendAnalysisHeader(
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
private fun CalorieTrendChart(
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
private fun ExerciseTrendChart(
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
private fun WeightTrendChart(
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
                    text = "功能开发中",
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
                            text = "体重记录功能即将上线",
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
 * 饮水趋势图表
 */
@Composable
private fun WaterTrendChart(
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
private fun WaterTrendStat(
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
private fun WaterMonthlySummaryCard(
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
private fun WaterMonthlyStat(
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
 * 上月总结内容 - 优化版，添加多种图表
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

/**
 * 带图表的总结头部卡片 - 环形进度图
 */
@Composable
private fun SummaryHeaderCardWithChart(summary: MonthSummary?) {
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
private fun SummaryKeyMetricsCard(summary: MonthSummary?) {
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
private fun KeyMetricWithIcon(icon: String, value: String, label: String) {
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
private fun MealTypeDistributionChart(summary: MonthSummary?) {
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
private fun MealBarItem(name: String, value: Int, percentage: Float, color: androidx.compose.ui.graphics.Color) {
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
private fun TargetAchievementChart(summary: MonthSummary?) {
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
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String, value: String) {
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
private fun ExerciseSummaryCard(summary: MonthSummary?) {
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
private fun ExerciseMetric(icon: String, value: String, label: String) {
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
private fun WeightChangeCard(summary: MonthSummary?) {
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
private fun SummaryDetailTable(summary: MonthSummary?) {
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
private fun DetailTableRow(label: String, value: String) {
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
private fun ElegantDateSelector(
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
private fun DateItem(
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
