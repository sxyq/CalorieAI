package com.calorieai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 运动消耗对话框
 * 用于添加今日运动消耗
 */
@Composable
fun ExerciseDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddExercise: (ExerciseType, Int) -> Unit
) {
    if (!isVisible) return

    var selectedExercise by remember { mutableStateOf<ExerciseType?>(null) }
    var customCalories by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }

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

                // 常见运动类型
                val commonExercises = listOf(
                    ExerciseType.RUNNING,
                    ExerciseType.WALKING,
                    ExerciseType.CYCLING,
                    ExerciseType.SWIMMING,
                    ExerciseType.YOGA,
                    ExerciseType.WEIGHT_TRAINING
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(commonExercises) { exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            isSelected = selectedExercise == exercise,
                            onClick = { selectedExercise = exercise }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                // 自定义热量输入（可选）
                OutlinedTextField(
                    value = customCalories,
                    onValueChange = { customCalories = it },
                    label = { Text("自定义热量（可选）") },
                    placeholder = { 
                        val estimated = selectedExercise?.let {
                            "预计消耗: ${it.caloriesPerMinute * duration.toIntOrNull() ?: 0} kcal"
                        } ?: "选择运动类型查看预计消耗"
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
                            selectedExercise?.let { exercise ->
                                val calories = if (customCalories.isNotBlank()) {
                                    customCalories.toIntOrNull() ?: 0
                                } else {
                                    exercise.caloriesPerMinute * (duration.toIntOrNull() ?: 30)
                                }
                                onAddExercise(exercise, calories)
                            }
                            onDismiss()
                        },
                        enabled = selectedExercise != null
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
 * 运动类型枚举
 */
enum class ExerciseType(
    val displayName: String,
    val emoji: String,
    val caloriesPerMinute: Int
) {
    RUNNING("跑步", "🏃", 10),
    WALKING("快走", "🚶", 4),
    CYCLING("骑行", "🚴", 8),
    SWIMMING("游泳", "🏊", 12),
    YOGA("瑜伽", "🧘", 3),
    WEIGHT_TRAINING("力量训练", "🏋️", 6),
    HIIT("HIIT", "🔥", 15),
    DANCING("跳舞", "💃", 7),
    HIKING("徒步", "🥾", 6),
    SKIPPING("跳绳", "🪢", 12),
    PILATES("普拉提", "🤸", 4),
    ELLIPTICAL("椭圆机", "🏃", 8),
    ROWING("划船", "🚣", 10),
    BOXING("拳击", "🥊", 11),
    SKATING("滑冰", "⛸️", 7),
    SKIING("滑雪", "⛷️", 8),
    BASKETBALL("篮球", "🏀", 8),
    FOOTBALL("足球", "⚽", 9),
    BADMINTON("羽毛球", "🏸", 6),
    TENNIS("网球", "🎾", 8),
    TABLE_TENNIS("乒乓球", "🏓", 5),
    VOLLEYBALL("排球", "🏐", 4),
    BASEBALL("棒球", "⚾", 5),
    GOLF("高尔夫", "⛳", 4),
    OTHER("其他", "🎯", 5)
}
