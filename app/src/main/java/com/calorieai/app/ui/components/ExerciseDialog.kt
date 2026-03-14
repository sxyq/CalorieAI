package com.calorieai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.calorieai.app.data.model.ExerciseType

/**
 * 运动消耗对话框
 * 用于添加今日运动消耗
 */
@Composable
fun ExerciseDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddExercise: (ExerciseType, Int, String?, Int) -> Unit  // 新增durationMinutes参数
) {
    if (!isVisible) return

    var selectedExercise by remember { mutableStateOf<ExerciseType?>(null) }
    var customCalories by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var customExerciseName by remember { mutableStateOf("") }
    var isCustomExercise by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "添加运动消耗",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 运动类型选择
                Text(
                    text = "选择运动类型",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 常见运动类型 + 自定义选项
                val commonExercises = listOf(
                    ExerciseType.RUNNING,
                    ExerciseType.WALKING,
                    ExerciseType.CYCLING,
                    ExerciseType.SWIMMING,
                    ExerciseType.YOGA,
                    ExerciseType.WEIGHT_TRAINING,
                    ExerciseType.HIIT,
                    ExerciseType.BOXING
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(commonExercises) { exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            isSelected = selectedExercise == exercise && !isCustomExercise,
                            onClick = {
                                selectedExercise = exercise
                                isCustomExercise = false
                                showCustomInput = false
                            }
                        )
                    }
                    item {
                        // 自定义运动选项
                        CustomExerciseItem(
                            isSelected = isCustomExercise,
                            onClick = {
                                isCustomExercise = true
                                selectedExercise = ExerciseType.OTHER
                                showCustomInput = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 自定义运动名称输入
                if (showCustomInput) {
                    OutlinedTextField(
                        value = customExerciseName,
                        onValueChange = { customExerciseName = it },
                        label = { Text("运动名称") },
                        placeholder = { Text("例如：太极拳、广场舞...") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 时长输入
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("运动时长（分钟）") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 自定义每分钟消耗（可选）
                var customCaloriesPerMinute by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = customCaloriesPerMinute,
                    onValueChange = { customCaloriesPerMinute = it },
                    label = { Text("每分钟消耗（千卡/分钟）") },
                    placeholder = {
                        val defaultValue = selectedExercise?.let {
                            "${it.caloriesPerMinute}"
                        } ?: "0"
                        Text("默认: $defaultValue")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 总消耗热量（自动计算或手动输入）
                OutlinedTextField(
                    value = customCalories,
                    onValueChange = { customCalories = it },
                    label = { Text("总消耗热量（千卡）") },
                    placeholder = {
                        val estimated = if (isCustomExercise) {
                            "请输入消耗的热量"
                        } else {
                            selectedExercise?.let {
                                val durationInt = duration.toIntOrNull() ?: 0
                                val customPerMin = customCaloriesPerMinute.toIntOrNull()
                                val caloriesPerMin = customPerMin ?: it.caloriesPerMinute
                                "预计消耗: ${caloriesPerMin * durationInt} kcal"
                            } ?: "选择运动类型查看预计消耗"
                        }
                        Text(estimated)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val durationInt = duration.toIntOrNull() ?: 30
                            
                            // 计算总消耗：优先使用用户输入的总热量，否则使用每分钟消耗 * 时长
                            val calories = if (customCalories.isNotBlank()) {
                                customCalories.toIntOrNull() ?: 0
                            } else if (!isCustomExercise && selectedExercise != null) {
                                val customPerMin = customCaloriesPerMinute.toIntOrNull()
                                val caloriesPerMin = customPerMin ?: selectedExercise!!.caloriesPerMinute
                                caloriesPerMin * durationInt
                            } else {
                                0
                            }
                            
                            // 获取每分钟消耗（用于存储）
                            val caloriesPerMinute = if (customCaloriesPerMinute.isNotBlank()) {
                                customCaloriesPerMinute.toIntOrNull() ?: selectedExercise?.caloriesPerMinute ?: 0
                            } else {
                                selectedExercise?.caloriesPerMinute ?: 0
                            }
                            
                            // 自定义运动格式: CUSTOM:{name}:{caloriesPerMinute}
                            val notes = if (isCustomExercise && customExerciseName.isNotBlank()) {
                                "CUSTOM:${customExerciseName}:${caloriesPerMinute}"
                            } else if (customCaloriesPerMinute.isNotBlank()) {
                                // 即使是标准运动，如果用户自定义了每分钟消耗，也记录下来
                                "CUSTOM:${selectedExercise?.displayName}:${caloriesPerMinute}"
                            } else {
                                null
                            }
                            
                            selectedExercise?.let { exercise ->
                                onAddExercise(exercise, calories, notes, durationInt)
                            }
                            onDismiss()
                        },
                        enabled = selectedExercise != null && 
                                 (!isCustomExercise || customExerciseName.isNotBlank())
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}

/**
 * 运动类型项
 */
@Composable
private fun ExerciseItem(
    exercise: ExerciseType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.emoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${exercise.caloriesPerMinute} kcal/分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null
                )
            }
        }
    }
}

/**
 * 自定义运动选项
 */
@Composable
private fun CustomExerciseItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "自定义运动",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "记录其他类型的运动",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null
                )
            }
        }
    }
}
