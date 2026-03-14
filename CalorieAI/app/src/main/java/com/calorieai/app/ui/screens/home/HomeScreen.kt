package com.calorieai.app.ui.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.ui.components.AIChatWidget
import com.calorieai.app.ui.components.ExerciseDialog
import com.calorieai.app.ui.components.ExpandableCalendarView
import com.calorieai.app.ui.components.MenuScreen
import com.calorieai.app.ui.components.TopMenuButton
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.round
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToAIChat: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // 运动对话框显示状态
    var showExerciseDialog by remember { mutableStateOf(false) }
    
    // 页面获得焦点时刷新数据（从其他页面返回时）
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CalorieAI") },
                    actions = {
                        // 统计按钮
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Default.BarChart, contentDescription = "统计")
                        }
                        // 顶部菜单按钮（三个点）
                        TopMenuButton(
                            onMenuItemClick = { menuScreen ->
                                when (menuScreen) {
                                    MenuScreen.Settings -> onNavigateToSettings()
                                    MenuScreen.Overview -> onNavigateToStats()
                                    MenuScreen.EditProfile -> onNavigateToProfile()
                                }
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                    )
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI聊天小窗口按钮（根据设置显示/隐藏）
                    if (uiState.showAIWidget) {
                        AIChatWidget(
                            onExpandToFullScreen = onNavigateToAIChat
                        )
                    }

                    // 添加按钮 - 玻璃效果
                    val fabInteractionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .interactiveScale(fabInteractionSource)
                            .liquidGlass(
                                shape = RoundedCornerShape(16.dp),
                                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                blurRadius = 15f,
                                borderAlpha = 0.35f
                            )
                            .clickable(
                                interactionSource = fabInteractionSource,
                                indication = null,
                                onClick = onNavigateToAdd
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 可展开日历 - 玻璃容器
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(20.dp),
                            tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                            blurRadius = 15f,
                            borderAlpha = 0.25f
                        )
                ) {
                    ExpandableCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            viewModel.selectDate(date)
                        },
                        modifier = Modifier.padding(12.dp),
                        calorieData = uiState.calorieData,
                        targetCalories = uiState.dailyGoal
                    )
                }
                
                // 今日概览 - 多色彩玻璃层
                TodayOverviewCard(
                    totalCalories = uiState.totalCalories,
                    dailyGoal = uiState.dailyGoal,
                    bmr = uiState.bmr,
                    exerciseCalories = uiState.exerciseCalories,
                    selectedDate = selectedDate
                )
                
                // 记录列表
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.records.isEmpty() && uiState.exerciseRecords.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 饮食记录
                        if (uiState.records.isNotEmpty()) {
                            item {
                                SectionHeader(title = "饮食记录", icon = "🍽️")
                            }
                            itemsIndexed(
                                items = uiState.records,
                                key = { _, record -> "food_" + record.id }
                            ) { index, record ->
                                com.calorieai.app.ui.components.AnimatedListItem(index = index) {
                                    FoodRecordItem(
                                        record = record,
                                        onClick = { onNavigateToResult(record.id) },
                                        onStarClick = { viewModel.toggleStarred(record) },
                                        onDeleteClick = { viewModel.deleteRecord(record) }
                                    )
                                }
                            }
                        }
                        
                        // 运动记录
                        if (uiState.exerciseRecords.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                SectionHeader(title = "运动记录", icon = "💪")
                            }
                            itemsIndexed(
                                items = uiState.exerciseRecords,
                                key = { _, record -> "exercise_" + record.id }
                            ) { index, record ->
                                com.calorieai.app.ui.components.AnimatedListItem(index = index + uiState.records.size) {
                                    ExerciseRecordItem(
                                        record = record,
                                        onDeleteClick = { viewModel.deleteExerciseRecord(record) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 运动消耗对话框
        ExerciseDialog(
            isVisible = showExerciseDialog,
            onDismiss = { showExerciseDialog = false },
            onAddExercise = { exerciseType, calories, notes, durationMinutes ->
                viewModel.addExercise(exerciseType, calories, notes, durationMinutes)
                showExerciseDialog = false
            }
        )
}

/**
 * 悬浮玻璃日期选择器
 */
@Composable
private fun FloatingDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dates = listOf(
        today.minusDays(2) to "前天",
        today.minusDays(1) to "昨天",
        today to "今天",
        today.plusDays(1) to "明天"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                blurRadius = 15f,
                borderAlpha = 0.3f
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dates.forEach { (date, label) ->
                val isSelected = date == selectedDate
                val interactionSource = remember { MutableInteractionSource() }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .interactiveScale(interactionSource)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onDateSelected(date)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "${date.monthValue}/${date.dayOfMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 分区标题
 */
@Composable
private fun SectionHeader(title: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TodayOverviewCard(
    totalCalories: Int,
    dailyGoal: Int,
    bmr: Int,
    exerciseCalories: Int,
    selectedDate: java.time.LocalDate
) {
    val progress = (totalCalories.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val remaining = dailyGoal - totalCalories
    val netCalories = totalCalories - bmr - exerciseCalories
    
    val today = java.time.LocalDate.now()
    val title = when (selectedDate) {
        today -> "今日摄入"
        today.minusDays(1) -> "昨日摄入"
        today.plusDays(1) -> "明日目标"
        else -> "摄入统计"
    }
    
    // 多色彩玻璃层 - 降低模糊
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .liquidGlass(
                shape = RoundedCornerShape(28.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                blurRadius = 15f,
                borderAlpha = 0.3f
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 主要数据行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieInfo(
                    value = totalCalories.toString(),
                    label = "已摄入",
                    color = MaterialTheme.colorScheme.primary
                )
                CalorieInfo(
                    value = dailyGoal.toString(),
                    label = "目标",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CalorieInfo(
                    value = remaining.toString(),
                    label = "剩余",
                    color = if (remaining < 0) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条 - 玻璃效果背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            when {
                                progress > 1f -> MaterialTheme.colorScheme.error
                                progress > 0.8f -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )
            }
            
            // 鼓励标语
            val encouragement = remember { com.calorieai.app.utils.getRandomEncouragement() }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = encouragement.emoji,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = encouragement.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 代谢和运动数据
            if (selectedDate == today && bmr > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalorieInfoSmall(
                        value = bmr.toString(),
                        label = "基础代谢",
                        icon = "🔥"
                    )
                    CalorieInfoSmall(
                        value = "+${exerciseCalories}",
                        label = "运动消耗",
                        icon = "💪"
                    )
                    CalorieInfoSmall(
                        value = "${if (netCalories >= 0) "+" else ""}$netCalories",
                        label = "热量差值",
                        icon = "⚖️",
                        color = when {
                            netCalories > 500 -> MaterialTheme.colorScheme.error
                            netCalories < -500 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieInfo(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CalorieInfoSmall(
    value: String,
    label: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FoodRecordItem(
    record: FoodRecord,
    onClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val density = LocalDensity.current
    val actionWidth = with(density) { 156.dp.toPx() } // 两个按钮的总宽度
    
    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = 0,
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 400.dp.toPx() } },
            animationSpec = tween()
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    0 at 0f // 正常位置
                    1 at -actionWidth // 滑动后显示按钮
                }
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // 背景按钮层
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.End
        ) {
            // 编辑按钮
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        onClick()
                        // 重置状态
                        kotlinx.coroutines.MainScope().launch {
                            anchoredDraggableState.animateTo(0)
                        }
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "编辑",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 删除按钮
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable {
                        onDeleteClick()
                        // 重置状态
                        kotlinx.coroutines.MainScope().launch {
                            anchoredDraggableState.animateTo(0)
                        }
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "删除",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 前景卡片层
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(anchoredDraggableState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
                .clip(RoundedCornerShape(20.dp))
                .interactiveScale(interactionSource)
                .liquidGlass(
                    shape = RoundedCornerShape(20.dp),
                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                    blurRadius = 12f,
                    borderAlpha = 0.3f
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (anchoredDraggableState.currentValue == 1) {
                            // 如果已展开，点击收起
                            kotlinx.coroutines.MainScope().launch {
                                anchoredDraggableState.animateTo(0)
                            }
                        } else {
                            onClick()
                        }
                    }
                )
                .padding(16.dp)
        ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 标题行
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = record.foodName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 收藏按钮
                        if (record.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "已收藏",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        // 热量
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${record.totalCalories} 千卡",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // 信息行
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 餐次标签
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = getMealTypeText(record.mealType),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = dateFormat.format(Date(record.recordTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // 营养成分
                        if (record.protein > 0 || record.carbs > 0 || record.fat > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${record.protein.toInt()}P · ${record.carbs.toInt()}C · ${record.fat.toInt()}F",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 玻璃效果空状态图标 - 降低模糊
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .liquidGlass(
                    shape = RoundedCornerShape(24.dp),
                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    blurRadius = 12f,
                    borderAlpha = 0.3f
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "暂无记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮添加食物",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun getMealTypeText(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.BREAKFAST_SNACK -> "早加餐"
        MealType.LUNCH -> "午餐"
        MealType.LUNCH_SNACK -> "午加餐"
        MealType.DINNER -> "晚餐"
        MealType.DINNER_SNACK -> "晚加餐"
        MealType.SNACK -> "加餐"
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ExerciseRecordItem(
    record: ExerciseRecord,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val displayName = if (record.notes?.startsWith("CUSTOM:") == true) {
        record.notes.substringAfter("CUSTOM:").substringBeforeLast(":")
    } else {
        record.exerciseType.displayName
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(20.dp))
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                blurRadius = 12f,
                borderAlpha = 0.3f
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { },
                onLongClick = { showDeleteDialog = true }
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 运动图标 - 玻璃背景
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.exerciseType.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // 运动信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${record.durationMinutes}分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 消耗热量
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "-${record.caloriesBurned} 千卡",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除运动记录") },
            text = { Text("确定要删除这条运动记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
