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
import com.calorieai.app.ui.components.rememberFabAwareBottomPadding
import com.calorieai.app.ui.feedback.rememberAppHapticController
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
    val haptics = rememberAppHapticController()
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // 杩愬姩瀵硅瘽妗嗘樉绀虹姸鎬?
    var showExerciseDialog by remember { mutableStateOf(false) }
    
    // 椤甸潰鑾峰緱鐒︾偣鏃跺埛鏂版暟鎹紙浠庡叾浠栭〉闈㈣繑鍥炴椂锛?
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
    
    // AI灏忓姪鎵嬬姸鎬?
    var aiWidgetState by remember { mutableStateOf(com.calorieai.app.ui.components.AIWidgetState.FLOATING) }
    val listBottomSafePadding = rememberFabAwareBottomPadding(
        fabVisible = true,
        extraPadding = if (uiState.showAIWidget) 104.dp else 36.dp
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("CalorieAI") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.confirm()
                    onNavigateToAdd(selectedDate.toString())
                },
                modifier = Modifier.pointerInput(uiState.enableQuickAdd, selectedDate) {
                    detectTapGestures(
                        onLongPress = {
                            if (uiState.enableQuickAdd) {
                                haptics.longPress()
                                onNavigateToAIAdd(selectedDate.toString())
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "娣诲姞")
            }
        }
        ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = listBottomSafePadding
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ExpandableCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        calorieData = uiState.calorieData,
                        targetCalories = uiState.dailyGoal
                    )
                }

                item {
                    TodayOverviewCard(
                        totalCalories = uiState.totalCalories,
                        dailyGoal = uiState.dailyGoal,
                        bmr = uiState.bmr,
                        exerciseCalories = uiState.exerciseCalories,
                        selectedDate = selectedDate
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.records.isEmpty() && uiState.exerciseRecords.isEmpty()) {
                    item { EmptyState() }
                } else {
                    if (uiState.records.isNotEmpty()) {
                        item {
                            Text(
                                text = "楗璁板綍",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        itemsIndexed(
                            items = uiState.records,
                            key = { _, record -> "food_" + record.id }
                        ) { index, record ->
                            FoodRecordItem(
                                record = record,
                                onClick = { onNavigateToResult(record.id) },
                                onStarClick = { viewModel.toggleStarred(record) },
                                onDeleteClick = { viewModel.deleteRecord(record) }
                            )
                        }
                    }

                    if (uiState.exerciseRecords.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "杩愬姩璁板綍",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        itemsIndexed(
                            items = uiState.exerciseRecords,
                            key = { _, record -> "exercise_" + record.id }
                        ) { index, record ->
                            ExerciseRecordItem(
                                record = record,
                                onDeleteClick = { viewModel.deleteExerciseRecord(record) }
                            )
                        }
                    }
                }
            }
        
        // AI鑱婂ぉ灏忕獥鍙ｏ紙鏍规嵁璁剧疆鏄剧ず/闅愯棌锛? 鍥哄畾鍦ㄥ彸涓嬭
        // 娉ㄦ剰锛欶AB 楂樺害绾︿负 56dp + 16dp margin = 72dp锛屾墍浠ュ簳閮?padding 璁剧疆涓?88dp 閬垮厤閲嶅彔
        if (uiState.showAIWidget) {
            // 閬僵灞?- 杩蜂綘绐楀彛鐘舵€佹椂鏄剧ず锛堢偣鍑诲彲鍏抽棴锛?
            if (aiWidgetState == com.calorieai.app.ui.components.AIWidgetState.MINI) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { aiWidgetState = com.calorieai.app.ui.components.AIWidgetState.FLOATING }
                )
            }

            // AI Widget 瀹瑰櫒 - 鍥哄畾鍦ㄥ彸涓嬭锛岄伩寮€ FAB
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
                        .padding(end = 16.dp, bottom = 88.dp) // 88dp 閬垮紑 FAB
                )
            }
        }
    }

        // 杩愬姩娑堣€楀璇濇
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
    val netCalories = totalCalories - bmr - exerciseCalories // 鐑噺宸€硷紙姝ｄ负鐩堜綑锛岃礋涓虹己鍙ｏ級
    
    // 鏍规嵁鏄惁鏄粖澶╂樉绀轰笉鍚岀殑鏍囬
    val today = java.time.LocalDate.now()
    val title = when (selectedDate) {
        today -> "浠婃棩鎽勫叆"
        today.minusDays(1) -> "鏄ㄦ棩鎽勫叆"
        today.plusDays(1) -> "鏄庢棩鐩爣"
        else -> "鎽勫叆缁熻"
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
            // 鏍囬
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 涓昏鏁版嵁琛岋細宸叉憚鍏?| 鐩爣 | 鍓╀綑
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieInfo(
                    value = totalCalories.toString(),
                    label = "宸叉憚鍏",
                    color = MaterialTheme.colorScheme.primary,
                    highlighted = true
                )
                CalorieInfo(
                    value = dailyGoal.toString(),
                    label = "鐩爣",
                    color = MaterialTheme.colorScheme.onSurface,
                    highlighted = false
                )
                CalorieInfo(
                    value = remaining.toString(),
                    label = "鍓╀綑",
                    color = if (remaining < 0) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.secondary,
                    highlighted = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 杩涘害鏉?
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
            
            // 榧撳姳鏍囪 - 浣跨敤remember閬垮厤姣忔閲嶇粍閮介噸鏂拌绠?
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
            
            // 浠ｈ阿鍜岃繍鍔ㄦ暟鎹紙浠呬粖澶╂樉绀猴級
            if (selectedDate == today && bmr > 0) {
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalorieInfoSmall(
                        value = bmr.toString(),
                        label = "鍩虹浠ｈ阿",
                        icon = "馃敟",
                        highlighted = false
                    )
                    CalorieInfoSmall(
                        value = "+${exerciseCalories}",
                        label = "杩愬姩娑堣€",
                        icon = "馃挭",
                        highlighted = true
                    )
                    CalorieInfoSmall(
                        value = "${if (netCalories >= 0) "+" else ""}$netCalories",
                        label = "鐑噺宸€",
                        icon = "鈿栵笍",
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
    
    // 婊戝姩鍋忕Щ閲?
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipe = 280f // 鏈€澶ф粦鍔ㄨ窛绂伙紝纭繚"380鍗冨崱"绛夐暱鏂囨湰鑳藉畬鍏ㄦ樉绀?
    
    // 鍔ㄧ敾鍖栧亸绉婚噺
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
        // 鑳屾櫙灞傦細缂栬緫鍜屽垹闄ゆ寜閽紙浠呭湪婊戝姩鏃舵樉绀猴級
        if (animatedOffsetX < -5f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 缂栬緫鎸夐挳
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
                        contentDescription = "缂栬緫",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 鍒犻櫎鎸夐挳
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
                        contentDescription = "鍒犻櫎",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 鍓嶆櫙灞傦細鍗＄墖鍐呭锛堝彲婊戝姩锛?
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(animatedOffsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 婊戝姩缁撴潫鏃讹紝鏍规嵁浣嶇疆鍐冲畾鏄惁灞曞紑鎴栨敹璧?
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
                // 鏍囬琛?
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
                        text = "${record.totalCalories} 鍗冨崱",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (record.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "宸叉敹钘",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onStarClick() }
                        )
                    }
                }

                // 淇℃伅琛?
                Text(
                    text = "${getMealTypeText(record.mealType)} 路 ${dateFormat.format(Date(record.recordTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // 鍒犻櫎纭瀵硅瘽妗?
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("鍒犻櫎璁板綍") },
            text = { Text("纭畾瑕佸垹闄よ繖鏉¤褰曞悧锛") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("鍒犻櫎", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("鍙栨秷")
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
            text = "鏆傛棤璁板綍",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "鐐瑰嚮鍙充笅瑙掓寜閽坊鍔犻鐗",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun getMealTypeText(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "鏃╅"
        MealType.BREAKFAST_SNACK -> "鏃╁姞椁"
        MealType.LUNCH -> "鍗堥"
        MealType.LUNCH_SNACK -> "鍗堝姞椁"
        MealType.DINNER -> "鏅氶"
        MealType.DINNER_SNACK -> "鏅氬姞椁"
        MealType.SNACK -> "鍔犻"
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
    
    // 瑙ｆ瀽鑷畾涔夎繍鍔ㄥ悕绉?
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
            // 杩愬姩鍥炬爣
            Text(
                text = record.exerciseType.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // 杩愬姩淇℃伅
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.durationMinutes}鍒嗛挓 路 ${record.caloriesBurned}鍗冨崱",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 娑堣€楃儹閲?
            Text(
                text = "-${record.caloriesBurned}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
    
    // 鍒犻櫎纭瀵硅瘽妗?
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("鍒犻櫎杩愬姩璁板綍") },
            text = { Text("纭畾瑕佸垹闄よ繖鏉¤繍鍔ㄨ褰曞悧锛") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("鍒犻櫎", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("鍙栨秷")
                }
            }
        )
    }
}



