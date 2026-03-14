package com.calorieai.app.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.calorieai.app.ui.components.liquidGlass
import androidx.compose.foundation.background
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.getExerciseTypeDisplayName
import java.text.SimpleDateFormat
import java.util.*

/**
 * 运动记录页面
 * 独立的运动记录界面，用于快速记录运动消耗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseRecordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("记录运动")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // 今日消耗卡片
            TodayCaloriesCard(
                todayCalories = uiState.todayCalories,
                exerciseCount = uiState.todayExerciseCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 运动类型选择
            Text(
                text = "运动类型",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExerciseTypeGrid(
                selectedType = uiState.selectedExerciseType,
                onTypeSelected = viewModel::selectExerciseType
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 时长输入
            Text(
                text = "运动时长",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.durationInput,
                onValueChange = viewModel::updateDuration,
                label = { Text("时长 (分钟)") },
                placeholder = { Text("例如：30") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null
                    )
                },
                suffix = {
                    Text("分钟")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 自定义卡路里消耗（可选）
            Text(
                text = "消耗热量 (可选)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.caloriesInput,
                onValueChange = viewModel::updateCalories,
                label = { Text("热量 (千卡)") },
                placeholder = { Text("留空则自动计算") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null
                    )
                },
                suffix = {
                    Text("千卡")
                }
            )

            // 自动计算提示
            if (uiState.caloriesInput.isBlank() && uiState.durationInput.isNotBlank()) {
                val estimatedCalories = uiState.selectedExerciseType.caloriesPerMinute * 
                    (uiState.durationInput.toIntOrNull() ?: 0)
                if (estimatedCalories > 0) {
                    Text(
                        text = "预计消耗约 ${estimatedCalories} 千卡",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 备注输入
            Text(
                text = "备注 (可选)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.noteInput,
                onValueChange = viewModel::updateNote,
                label = { Text("添加备注") },
                placeholder = { Text("例如：感觉状态不错") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveExerciseRecord()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = uiState.durationInput.isNotBlank() && 
                         uiState.durationInput.toIntOrNull() != null &&
                         uiState.durationInput.toIntOrNull()!! > 0
            ) {
                Text(
                    text = "保存运动记录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 历史记录入口
            OutlinedButton(
                onClick = { viewModel.showHistoryDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看今日记录")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }


    // 历史记录对话框
    if (uiState.showHistoryDialog) {
        ExerciseHistoryDialog(
            records = uiState.todayRecords,
            onDismiss = { viewModel.hideHistoryDialog() },
            onDeleteRecord = { viewModel.deleteRecord(it) }
        )
    }
}

/**
 * 今日消耗卡片
 */
@Composable
private fun TodayCaloriesCard(
    todayCalories: Int,
    exerciseCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = MaterialTheme.shapes.extraLarge,
                tint = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "今日运动消耗",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = todayCalories.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "千卡",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            if (exerciseCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已完成 $exerciseCount 次运动",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 运动类型网格选择器
 */
@Composable
private fun ExerciseTypeGrid(
    selectedType: ExerciseType,
    onTypeSelected: (ExerciseType) -> Unit
) {
    val exerciseTypes = ExerciseType.entries.toList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exerciseTypes.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { type ->
                    ExerciseTypeChip(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 填充剩余空间
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 运动类型芯片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseTypeChip(
    type: ExerciseType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text(
                text = getExerciseTypeDisplayName(type),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

/**
 * 历史记录对话框
 */
@Composable
private fun ExerciseHistoryDialog(
    records: List<ExerciseRecordItem>,
    onDismiss: () -> Unit,
    onDeleteRecord: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("今日运动记录") },
        text = {
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("今日暂无运动记录")
                }
            } else {
                Column {
                    records.forEach { record ->
                        ExerciseHistoryItem(
                            record = record,
                            onDelete = { onDeleteRecord(record.id) }
                        )
                        if (record != records.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 历史记录项
 */
@Composable
private fun ExerciseHistoryItem(
    record: ExerciseRecordItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.exerciseTypeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${record.duration}分钟 · ${record.calories}千卡",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.note.isNotBlank()) {
                Text(
                    text = record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * 运动记录项数据类
 */
data class ExerciseRecordItem(
    val id: String,
    val exerciseTypeName: String,
    val duration: Int,
    val calories: Int,
    val note: String,
    val timestamp: Long
)
