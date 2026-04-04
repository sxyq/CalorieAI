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
import androidx.compose.ui.graphics.luminance
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
import com.calorieai.app.ui.components.CompactHeatmap
import com.calorieai.app.ui.components.HeatmapData
import com.calorieai.app.ui.components.HeatmapLegend
import com.calorieai.app.ui.components.charts.*
import com.calorieai.app.ui.components.fadingTopEdge
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.utils.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 概览统计内容
 */
@Composable
internal fun OverviewStatsContent(
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

        item {
            AnimatedListItem(index = 6) {
                RecordHeatmapCard(dailyMealRecords = uiState.dailyMealRecords)
            }
        }

        // 菜谱相关统计
        item {
            AnimatedListItem(index = 7) {
                RecipeDataStatsCard(recipeStats = uiState.recipeStats)
            }
        }

        // 详细营养素统计表
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 8) {
                    DetailedNutritionStatsCard(stats = stats)
                }
            }
        }
    }
}

@Composable
internal fun RecordHeatmapCard(dailyMealRecords: List<DailyMealRecord>) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val heatmapData = remember(dailyMealRecords) {
        dailyMealRecords.map { day ->
            HeatmapData(
                date = day.date,
                value = day.level.toFloat()
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "记录热力图",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (heatmapData.isEmpty()) {
                Text(
                    text = "暂无记录数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                CompactHeatmap(
                    data = heatmapData,
                    weeks = 20,
                    cellSize = 11,
                    isDark = isDark
                )
                HeatmapLegend(
                    modifier = Modifier.align(Alignment.End),
                    labels = listOf("少", "多"),
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
internal fun RecipeDataStatsCard(recipeStats: RecipeStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "菜谱数据统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "已有食材",
                    value = recipeStats.pantryCount.toString(),
                    unit = "项",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "收藏菜谱",
                    value = recipeStats.favoriteCount.toString(),
                    unit = "条",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "使用总次数",
                    value = recipeStats.favoriteUseCount.toString(),
                    unit = "次",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "即将过期",
                    value = recipeStats.pantryExpiringSoonCount.toString(),
                    unit = "项",
                    color = if (recipeStats.pantryExpiringSoonCount > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                StatItem(
                    label = "已使用收藏",
                    value = recipeStats.usedFavoriteCount.toString(),
                    unit = "条",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "菜单计划",
                    value = recipeStats.recipePlanCount.toString(),
                    unit = "个",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val mostUsedText = recipeStats.mostUsedFavoriteName
                ?.takeIf { it.isNotBlank() }
                ?.let { "$it（${recipeStats.mostUsedFavoriteUseCount}次）" }
                ?: "暂无使用记录"
            Text(
                text = "最常用收藏菜谱：$mostUsedText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 今日统计卡片（带饼状图）
 */
@Composable
internal fun TodayStatsCard(stats: TodayStats) {
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
internal fun ExerciseStatsCard(stats: TodayStats) {
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
internal fun WaterStatsCard(
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
internal fun NutritionItem(
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
internal fun StatItem(
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
internal fun MealTypeStatsCard(stats: Map<com.calorieai.app.data.model.MealType, Int>) {
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
internal fun MealTypeGridItem(
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
internal fun MealBar(
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
internal fun HistoryStatsCard(stats: HistoryStats?) {
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
internal fun DetailedNutritionStatsCard(
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
internal fun NutritionProgressRow(item: NutritionItemData) {
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
internal data class NutritionItemData(
    val reference: NutritionReference,
    val currentValue: Float,
    val emoji: String
)

/**
 * 连续记录卡片
 */
@Composable
internal fun StreakCard(streakDays: Int) {
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
