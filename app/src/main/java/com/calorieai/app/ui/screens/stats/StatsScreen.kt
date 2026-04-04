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
