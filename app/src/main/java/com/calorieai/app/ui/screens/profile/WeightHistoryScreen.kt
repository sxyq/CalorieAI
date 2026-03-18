package com.calorieai.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.viewmodel.WeightHistoryViewModel
import com.calorieai.app.data.model.WeightRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeightHistoryViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val weightRecords by viewModel.weightRecords.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("体重历史") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (weightRecords.isEmpty()) {
                EmptyWeightState()
            } else {
                // 体重趋势图表
                WeightChartCard(weightRecords, isDark)

                Spacer(modifier = Modifier.height(16.dp))

                // 统计摘要
                WeightStatsCard(weightRecords, isDark)

                Spacer(modifier = Modifier.height(16.dp))

                // 记录列表
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weightRecords.reversed()) { record ->
                        WeightRecordItemCard(record, isDark)
                    }
                }
            }
        }
    }

    // 添加记录对话框
    if (showAddDialog) {
        AddWeightRecordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { weight, note ->
                viewModel.addWeightRecord(weight, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyWeightState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MonitorWeight,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无体重记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮添加第一条记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AddWeightRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, String?) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加体重记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("体重 (kg)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("备注 (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightText.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        onConfirm(weight, noteText.takeIf { it.isNotBlank() })
                    }
                },
                enabled = weightText.toFloatOrNull() != null
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun WeightChartCard(
    records: List<WeightRecord>,
    isDark: Boolean
) {
    if (records.isEmpty()) return

    val weights = records.map { it.weight }
    val minWeight = (weights.minOrNull() ?: 0f) - 1f
    val maxWeight = (weights.maxOrNull() ?: 100f) + 1f
    val weightRange = maxWeight - minWeight

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "体重趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Y轴标签
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(5) { i ->
                        val weight = maxWeight - (weightRange / 4) * i
                        Text(
                            text = String.format("%.1f", weight),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = if (i == 0) 0.dp else 0.dp)
                        )
                    }
                }

                // 图表区域
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val padding = 0f

                    val chartWidth = canvasWidth
                    val chartHeight = canvasHeight

                    // 绘制网格线
                    for (i in 0..4) {
                        val y = (chartHeight / 4) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(padding, y),
                            end = Offset(canvasWidth - padding, y),
                            strokeWidth = 1f
                        )
                    }

                    // 绘制数据点和曲线
                    if (records.size >= 2) {
                        val points = records.mapIndexed { index, record ->
                            val x = (chartWidth / (records.size - 1)) * index
                            val y = chartHeight - ((record.weight - minWeight) / weightRange) * chartHeight
                            Offset(x, y)
                        }

                        // 绘制填充区域
                        val path = Path().apply {
                            moveTo(points.first().x, canvasHeight)
                            points.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, canvasHeight)
                            close()
                        }

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.05f)
                                )
                            )
                        )

                        // 绘制线条
                        drawPath(
                            path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            },
                            color = primaryColor,
                            style = Stroke(width = 3f)
                        )

                        // 绘制数据点
                        points.forEach { point ->
                            drawCircle(
                                color = primaryColor,
                                radius = 6f,
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = point
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightStatsCard(
    records: List<WeightRecord>,
    isDark: Boolean
) {
    val startWeight = records.firstOrNull()?.weight ?: 0f
    val currentWeight = records.lastOrNull()?.weight ?: 0f
    val weightChange = currentWeight - startWeight
    val avgWeight = if (records.isNotEmpty()) records.map { it.weight }.average().toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "统计摘要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeightStatItem(
                    label = "起始体重",
                    value = "${startWeight}kg",
                    icon = Icons.Default.Start
                )
                WeightStatItem(
                    label = "当前体重",
                    value = "${currentWeight}kg",
                    icon = Icons.Default.MonitorWeight
                )
                WeightStatItem(
                    label = "平均体重",
                    value = "${String.format("%.1f", avgWeight)}kg",
                    icon = Icons.Default.Analytics
                )
                WeightStatItem(
                    label = "变化",
                    value = "${if (weightChange >= 0) "+" else ""}${String.format("%.1f", weightChange)}kg",
                    icon = if (weightChange <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    color = if (weightChange <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun WeightStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun WeightRecordItemCard(
    record: WeightRecord,
    isDark: Boolean
) {
    val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.weight} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(record.recordDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!record.note.isNullOrBlank()) {
                    Text(
                        text = record.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
