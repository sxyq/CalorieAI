package com.calorieai.app.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.ui.components.AIChatWidget
import com.calorieai.app.ui.components.AIWidgetMode
import com.calorieai.app.ui.components.ExerciseDialog
import com.calorieai.app.ui.components.ExpandableCalendarView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.itemsIndexed
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: (String) -> Unit,
    onNavigateToAIAdd: (String) -> Unit = {},
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToAIChat: (String) -> Unit = {},
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
    
    // AI小助手状态
    var aiWidgetState by remember { mutableStateOf(com.calorieai.app.ui.components.AIWidgetState.FLOATING) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("CalorieAI") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(selectedDate.toString()) },
                modifier = Modifier.pointerInput(uiState.enableQuickAdd, selectedDate) {
                    detectTapGestures(
                        onLongPress = {
                            if (uiState.enableQuickAdd) {
                                onNavigateToAIAdd(selectedDate.toString())
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 可展开日历
            ExpandableCalendarView(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                calorieData = uiState.calorieData,
                targetCalories = uiState.dailyGoal
            )
            
            // 今日概览（优化布局）
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 饮食记录
                    if (uiState.records.isNotEmpty()) {
                        item {
                            Text(
                                text = "饮食记录",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        itemsIndexed(
                            items = uiState.records,
                            key = { _, record -> "food_" + record.id } // 使用key优化列表性能
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
                            Text(
                                text = "运动记录",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
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
        
        // AI聊天小窗口（根据设置显示/隐藏）- 固定在右下角
        // 注意：FAB 高度约为 56dp + 16dp margin = 72dp，所以底部 padding 设置为 88dp 避免重叠
        if (uiState.showAIWidget) {
            // 遮罩层 - 迷你窗口状态时显示（点击可关闭）
            if (aiWidgetState == com.calorieai.app.ui.components.AIWidgetState.MINI) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { aiWidgetState = com.calorieai.app.ui.components.AIWidgetState.FLOATING }
                )
            }

            // AI Widget 容器 - 固定在右下角，避开 FAB
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.BottomEnd
            ) {
                AIChatWidget(
                    onExpandToFullScreen = onNavigateToAIChat,
                    mode = AIWidgetMode.RECIPE_ASSISTANT,
                    widgetState = aiWidgetState,
                    onWidgetStateChange = { aiWidgetState = it },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 88.dp) // 88dp 避开 FAB
                )
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
}

@Composable
fun TodayOverviewCard(
    totalCalories: Int,
    dailyGoal: Int,
    bmr: Int,
    exerciseCalories: Int,
    selectedDate: java.time.LocalDate
) {
    val isDark = isSystemInDarkTheme()
    val progress = (totalCalories.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val remaining = dailyGoal - totalCalories
    val netCalories = totalCalories - bmr - exerciseCalories // 热量差值（正为盈余，负为缺口）
    
    // 根据是否是今天显示不同的标题
    val today = java.time.LocalDate.now()
    val title = when (selectedDate) {
        today -> "今日摄入"
        today.minusDays(1) -> "昨日摄入"
        today.plusDays(1) -> "明日目标"
        else -> "摄入统计"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .liquidGlass(
                shape = MaterialTheme.shapes.extraLarge,
                tint = if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                },
                blurRadius = 40f
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 主要数据行：已摄入 | 目标 | 剩余
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieInfo(
                    value = totalCalories.toString(),
                    label = "已摄入",
                    color = MaterialTheme.colorScheme.primary,
                    highlighted = true
                )
                CalorieInfo(
                    value = dailyGoal.toString(),
                    label = "目标",
                    color = MaterialTheme.colorScheme.onSurface,
                    highlighted = false
                )
                CalorieInfo(
                    value = remaining.toString(),
                    label = "剩余",
                    color = if (remaining < 0) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.secondary,
                    highlighted = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    progress > 1f -> MaterialTheme.colorScheme.error
                    progress > 0.8f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 鼓励标语 - 使用remember避免每次重组都重新计算
            val encouragement by remember(totalCalories, dailyGoal) {
                derivedStateOf { com.calorieai.app.utils.getRandomEncouragement() }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = encouragement.emoji,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = encouragement.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 代谢和运动数据（仅今天显示）
            if (selectedDate == today && bmr > 0) {
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalorieInfoSmall(
                        value = bmr.toString(),
                        label = "基础代谢",
                        icon = "🔥",
                        highlighted = false
                    )
                    CalorieInfoSmall(
                        value = "+${exerciseCalories}",
                        label = "运动消耗",
                        icon = "💪",
                        highlighted = true
                    )
                    CalorieInfoSmall(
                        value = "${if (netCalories >= 0) "+" else ""}$netCalories",
                        label = "热量差值",
                        icon = "⚖️",
                        color = when {
                            netCalories > 500 -> MaterialTheme.colorScheme.error
                            netCalories < -500 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        highlighted = true
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
    color: androidx.compose.ui.graphics.Color,
    highlighted: Boolean
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    isDark && highlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                    isDark -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)
                    highlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
                    else -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.82f)
                }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalorieInfoSmall(
    value: String,
    label: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    highlighted: Boolean
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isDark && highlighted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    isDark -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f)
                    highlighted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f)
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FoodRecordItem(
    record: FoodRecord,
    onClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 滑动偏移量
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipe = 280f // 最大滑动距离，确保"380千卡"等长文本能完全显示
    
    // 动画化偏移量
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "swipe"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        // 背景层：编辑和删除按钮（仅在滑动时显示）
        if (animatedOffsetX < -5f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑按钮
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            offsetX = 0f
                            onClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 删除按钮
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable {
                            offsetX = 0f
                            showDeleteDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 前景层：卡片内容（可滑动）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(animatedOffsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动结束时，根据位置决定是否展开或收起
                            offsetX = if (offsetX < -maxSwipe / 2) {
                                -maxSwipe
                            } else {
                                0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-maxSwipe, 0f)
                        }
                    )
                }
                .interactiveScale(interactionSource)
                .liquidGlass(
                    shape = MaterialTheme.shapes.large,
                    tint = if (isDark) {
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.86f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (offsetX < -10f) {
                            offsetX = 0f
                        } else {
                            onClick()
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 标题行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.foodName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${record.totalCalories} 千卡",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (record.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "已收藏",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onStarClick() }
                        )
                    }
                }

                // 信息行
                Text(
                    text = "${getMealTypeText(record.mealType)} · ${dateFormat.format(Date(record.recordTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条记录吗？") },
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

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val isDark = isSystemInDarkTheme()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 解析自定义运动名称
    val displayName = if (record.notes?.startsWith("CUSTOM:") == true) {
        record.notes.substringAfter("CUSTOM:").substringBeforeLast(":")
    } else {
        record.exerciseType.displayName
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = MaterialTheme.shapes.large,
                tint = if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.86f)
                } else {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = { },
                onLongClick = { showDeleteDialog = true }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 运动图标
            Text(
                text = record.exerciseType.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // 运动信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.durationMinutes}分钟 · ${record.caloriesBurned}千卡",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 消耗热量
            Text(
                text = "-${record.caloriesBurned}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
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


