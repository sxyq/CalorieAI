package com.calorieai.app.ui.screens.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.ui.components.ExpandableCalendarView
import com.calorieai.app.ui.components.MenuScreen
import com.calorieai.app.ui.components.TopMenuButton
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { paddingValues ->
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
            } else if (uiState.records.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.records,
                        key = { it.id } // 使用key优化列表性能
                    ) { record ->
                        FoodRecordItem(
                            record = record,
                            onClick = { onNavigateToResult(record.id) },
                            onStarClick = { viewModel.toggleStarred(record) },
                            onDeleteClick = { viewModel.deleteRecord(record) }
                        )
                    }
                }
            }
        }
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
    val netCalories = totalCalories - bmr - exerciseCalories // 热量差值（正为盈余，负为缺口）
    
    // 根据是否是今天显示不同的标题
    val today = java.time.LocalDate.now()
    val title = when (selectedDate) {
        today -> "今日摄入"
        today.minusDays(1) -> "昨日摄入"
        today.plusDays(1) -> "明日目标"
        else -> "摄入统计"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题和添加运动按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 只有今天才显示添加运动按钮
                if (selectedDate == today) {
                    TextButton(
                        onClick = { /* TODO: 打开运动消耗添加对话框 */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加运动")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 主要数据行：已摄入 | 目标 | 剩余
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FoodRecordItem(
    record: FoodRecord,
    onClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            ),
        shape = MaterialTheme.shapes.large
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
                        tint = MaterialTheme.colorScheme.primary
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
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
        MealType.SNACK -> "加餐"
    }
}


