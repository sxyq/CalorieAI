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
import com.calorieai.app.ui.components.charts.*
import com.calorieai.app.ui.components.fadingTopEdge
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.utils.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 姒傝缁熻鍐呭
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
        // 鏃ユ湡閫夋嫨鍣?- 浣跨敤鏂扮殑婊戝姩鏃ユ湡閫夋嫨鍣?
        item {
            ElegantDateSelector(
                selectedDate = uiState.selectedOverviewDate,
                onDateSelected = onDateSelected
            )
        }

        // 浠婃棩缁熻鍗＄墖
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 0) {
                    TodayStatsCard(stats = stats)
                }
            }
        }

        // 浠婃棩杩愬姩缁熻鍗＄墖
        item {
            uiState.todayStats?.let { stats ->
                if (stats.exerciseCount > 0) {
                    AnimatedListItem(index = 1) {
                        ExerciseStatsCard(stats = stats)
                    }
                }
            }
        }

        // 浠婃棩楗按缁熻鍗＄墖
        if (uiState.showWaterFeatures) {
            item {
                AnimatedListItem(index = 2) {
                    WaterStatsCard(
                        todayAmount = uiState.todayWaterAmount,
                        targetAmount = uiState.waterTargetAmount,
                        weeklyAverage = uiState.weeklyWaterAverage
                    )
                }
            }
        }

        // 椁愭缁熻
        item {
            AnimatedListItem(index = 3) {
                MealTypeStatsCard(stats = uiState.mealTypeStats)
            }
        }

        // 鍘嗗彶缁熻
        item {
            uiState.historyStats?.let { stats ->
                AnimatedListItem(index = 4) {
                    HistoryStatsCard(stats = stats)
                }
            }
        }

        // 杩炵画璁板綍
        item {
            AnimatedListItem(index = 5) {
                StreakCard(streakDays = uiState.streakDays)
            }
        }

        // 鑿滆氨鐩稿叧缁熻
        item {
            AnimatedListItem(index = 6) {
                RecipeDataStatsCard(recipeStats = uiState.recipeStats)
            }
        }

        // 璇︾粏钀ュ吇绱犵粺璁¤〃
        item {
            uiState.todayStats?.let { stats ->
                AnimatedListItem(index = 7) {
                    DetailedNutritionStatsCard(stats = stats)
                }
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
                text = "鑿滆氨鏁版嵁缁熻",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "宸叉湁椋熸潗",
                    value = recipeStats.pantryCount.toString(),
                    unit = "椤",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "鏀惰棌鑿滆氨",
                    value = recipeStats.favoriteCount.toString(),
                    unit = "鏉",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "浣跨敤鎬绘鏁",
                    value = recipeStats.favoriteUseCount.toString(),
                    unit = "娆",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "鍗冲皢杩囨湡",
                    value = recipeStats.pantryExpiringSoonCount.toString(),
                    unit = "椤",
                    color = if (recipeStats.pantryExpiringSoonCount > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                StatItem(
                    label = "宸蹭娇鐢ㄦ敹钘",
                    value = recipeStats.usedFavoriteCount.toString(),
                    unit = "鏉",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "鑿滃崟璁″垝",
                    value = recipeStats.recipePlanCount.toString(),
                    unit = "涓",
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
 * 浠婃棩缁熻鍗＄墖锛堝甫楗肩姸鍥撅級
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
                text = "浠婃棩鎽勫叆鐘舵€",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 楗肩姸鍥惧睍绀鸿惀鍏荤礌鍒嗗竷
            if (stats.totalCalories > 0) {
                val nutritionData = listOf(
                    "铔嬬櫧璐" to stats.proteinGrams * 4f,
                    "纰虫按" to stats.carbsGrams * 4f,
                    "鑴傝偑" to stats.fatGrams * 9f
                ).filter { it.second > 0 }

                if (nutritionData.isNotEmpty()) {
                    PieChartView(
                        data = nutritionData,
                        colors = ChartColors.NUTRITION,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        centerText = "${stats.totalCalories}\n鍗冨崱"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 钀ュ吇绱犺鎯?
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionItem("铔嬬櫧璐", stats.proteinGrams, "g", ChartColors.NUTRITION[0])
                    NutritionItem("纰虫按", stats.carbsGrams, "g", ChartColors.NUTRITION[1])
                    NutritionItem("鑴傝偑", stats.fatGrams, "g", ChartColors.NUTRITION[2])
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 涓夊垪甯冨眬锛氬凡鎽勫叆 | 鐩爣 | 鍓╀綑
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "宸叉憚鍏",
                    value = stats.totalCalories.toString(),
                    unit = "鍗冨崱",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "鐩爣",
                    value = stats.targetCalories.toString(),
                    unit = "鍗冨崱",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatItem(
                    label = "鍓╀綑",
                    value = remaining.toString(),
                    unit = "鍗冨崱",
                    color = if (remaining < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // 鍩虹浠ｈ阿鏁版嵁锛堝鏋滄湁锛?
            if (stats.bmr > 0) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "鍩虹浠ｈ阿",
                        value = stats.bmr.toString(),
                        unit = "鍗冨崱",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatItem(
                        label = "鎬绘秷鑰",
                        value = stats.tdee.toString(),
                        unit = "鍗冨崱",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    val netCalories = stats.totalCalories - stats.exerciseCalories - stats.bmr
                    StatItem(
                        label = "鐑噺宸",
                        value = netCalories.toString(),
                        unit = "鍗冨崱",
                        color = when {
                            netCalories > 500 -> MaterialTheme.colorScheme.error
                            netCalories < -500 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 杩涘害鏉?
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

            // 杈炬爣鎻愮ず
            if (stats.isTargetMet) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "鉁?浠婃棩鐑噺鎺у埗鑹ソ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * 浠婃棩杩愬姩缁熻鍗＄墖
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
            // 鏍囬
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "馃挭",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "浠婃棩杩愬姩",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${stats.exerciseCount} 娆",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 杩愬姩鏁版嵁
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "娑堣€楃儹閲",
                    value = stats.exerciseCalories.toString(),
                    unit = "鍗冨崱",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "杩愬姩鏃堕暱",
                    value = stats.exerciseMinutes.toString(),
                    unit = "鍒嗛挓",
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatItem(
                    label = "鍑€鎽勫叆",
                    value = (stats.totalCalories - stats.exerciseCalories).toString(),
                    unit = "鍗冨崱",
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
 * 浠婃棩楗按缁熻鍗＄墖
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
            // 鏍囬琛?
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
                        text = "浠婃棩楗按",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isGoalMet) {
                    Text(
                        text = "鉁?宸茶揪鏍",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 杩涘害鏉?
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

            // 缁熻鏁版嵁
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "宸查ギ姘",
                    value = todayAmount.toString(),
                    unit = "ml",
                    color = androidx.compose.ui.graphics.Color(0xFF26C6DA)
                )
                StatItem(
                    label = "鐩爣",
                    value = targetAmount.toString(),
                    unit = "ml",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatItem(
                    label = "杩橀渶",
                    value = remaining.toString(),
                    unit = "ml",
                    color = if (remaining > 0) MaterialTheme.colorScheme.primary 
                           else androidx.compose.ui.graphics.Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 鍛ㄥ钩鍧?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7鏃ュ钩鍧? ",
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
 * 钀ュ吇绱犻」
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
 * 缁熻椤?
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
 * 椁愭缁熻鍗＄墖 - 鍙樉绀烘湁鏁版嵁鐨勯娆★紝浣跨敤鐜颁唬缃戞牸甯冨眬
 */
@Composable
internal fun MealTypeStatsCard(stats: Map<com.calorieai.app.data.model.MealType, Int>) {
    // 鍚堝苟鍔犻鏁版嵁锛屽彧淇濈暀鏈夋暟鎹殑椁愭
    val activeStats = remember(stats) {
        val merged = mutableMapOf<com.calorieai.app.data.model.MealType, Int>()
        
        stats.forEach { (mealType, calories) ->
            if (calories > 0) { // 鍙鐞嗘湁鏁版嵁鐨勯娆?
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
        
        // 鎸夐『搴忔帓搴?
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
            // 鏍囬琛?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "浠婃棩鎽勫叆鍒嗗竷",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${totalCalories} 鍗冨崱",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeStats.isEmpty()) {
                // 娌℃湁鏁版嵁鏃舵樉绀烘彁绀?
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "鏆傛棤椁愭璁板綍",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 鍔ㄦ€佺綉鏍煎竷灞€ - 鏍规嵁鏁伴噺鍐冲畾姣忚鏄剧ず鍑犱釜
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
                            // 琛ラ綈绌轰綅淇濇寔瀵归綈
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
 * 椁愭缃戞牸椤?- 鐜颁唬鍖栧崱鐗囪璁?
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
    
    // 椁愭瀵瑰簲鐨勯鑹?
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
            // 椁愭鍚嶇О鍜屽崰姣?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 棰滆壊鎸囩ず鐐?
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
            
            // 鐑噺鏁板€?
            Text(
                text = "${calories}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (calories > 0) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "鍗冨崱",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 杩涘害鏉?
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
 * 椁愭鏉″舰鍥?
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
            text = "${calories}鍗冨崱",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 鍘嗗彶缁熻鍗＄墖
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
                text = "鎽勫叆鐘舵€佺粺璁",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "杈炬爣澶╂暟",
                    value = stats?.targetMetDays.toString(),
                    unit = "澶",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "瓒呮爣澶╂暟",
                    value = stats?.overTargetDays.toString(),
                    unit = "澶",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    label = "璁板綍澶╂暟",
                    value = stats?.totalDays.toString(),
                    unit = "澶",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 璇︾粏钀ュ吇绱犵粺璁″崱鐗?- 灞曠ず13绉嶈惀鍏荤礌鐨勬憚鍏ユ儏鍐?
 */
@Composable
internal fun DetailedNutritionStatsCard(
    stats: TodayStats,
    viewModel: StatsViewModel = hiltViewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 浠嶸iewModel鑾峰彇鐢ㄦ埛韬綋鏁版嵁
    val uiState by viewModel.uiState.collectAsState()

    // 鏋勫缓鐢ㄦ埛韬綋鏁版嵁
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

    // 璁＄畻涓€у寲钀ュ吇绱犲弬鑰冨€?
    val nutritionReferences by remember(userProfile) {
        derivedStateOf {
            NutritionCalculator.calculateAll(userProfile)
        }
    }

    // 鏋勫缓钀ュ吇绱犳暟鎹垪琛?
    val nutritionItems = listOf(
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "protein" }!!,
            currentValue = stats.proteinGrams,
            emoji = "馃挭"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "carbs" }!!,
            currentValue = stats.carbsGrams,
            emoji = "馃崬"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "fat" }!!,
            currentValue = stats.fatGrams,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "fiber" }!!,
            currentValue = stats.fiberGrams,
            emoji = "馃尵"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "sugar" }!!,
            currentValue = stats.sugarGrams,
            emoji = "馃嵂"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "sodium" }!!,
            currentValue = stats.sodiumMg,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "cholesterol" }!!,
            currentValue = stats.cholesterolMg,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "saturated_fat" }!!,
            currentValue = stats.saturatedFatGrams,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "calcium" }!!,
            currentValue = stats.calciumMg,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "iron" }!!,
            currentValue = stats.ironMg,
            emoji = "馃ォ"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "vitamin_c" }!!,
            currentValue = stats.vitaminCMg,
            emoji = "馃崐"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "vitamin_a" }!!,
            currentValue = stats.vitaminAMcg,
            emoji = "馃"
        ),
        NutritionItemData(
            reference = nutritionReferences.find { it.id == "potassium" }!!,
            currentValue = stats.potassiumMg,
            emoji = "馃崒"
        )
    )

    // 鍩虹钀ュ吇绱狅紙濮嬬粓鏄剧ず锛?
    val basicNutritionItems = nutritionItems.take(3)
    // 鎵╁睍钀ュ吇绱狅紙鍙睍寮€锛?
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
            // 鏍囬琛?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "馃",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "钀ュ吇绱犳憚鍏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 灞曞紑/鏀惰捣鎸夐挳
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "鏀惰捣" else "灞曞紑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 鍩虹钀ュ吇绱狅紙濮嬬粓鏄剧ず锛?
            basicNutritionItems.forEach { item ->
                NutritionProgressRow(item = item)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 鎵╁睍钀ュ吇绱狅紙鍙睍寮€锛?
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    extendedNutritionItems.forEach { item ->
                        NutritionProgressRow(item = item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // 鎻愮ず鏂囧瓧
            if (!isExpanded) {
                Text(
                    text = "鐐瑰嚮灞曞紑鏌ョ湅鍏ㄩ儴13绉嶈惀鍏荤礌",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * 钀ュ吇绱犺繘搴﹁
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

        // 杩涘害鏉?
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // 鐧惧垎姣旀彁绀?
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * 钀ュ吇绱犳暟鎹」
 */
internal data class NutritionItemData(
    val reference: NutritionReference,
    val currentValue: Float,
    val emoji: String
)

/**
 * 杩炵画璁板綍鍗＄墖
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
                    text = "杩炵画璁板綍",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "淇濇寔濂戒範鎯紝缁х画鍔犳补锛",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "${streakDays}澶",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
