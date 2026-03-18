package com.calorieai.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.ui.components.WaterProgressCard
import com.calorieai.app.ui.theme.*
import com.calorieai.app.viewmodel.WaterHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WaterHistoryViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val waterRecords by viewModel.waterRecords.collectAsState()
    val todayAmount by viewModel.todayAmount.collectAsState()
    val targetAmount by viewModel.targetAmount.collectAsState()
    val weeklyAverage by viewModel.weeklyAverage.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val progress = (todayAmount.toFloat() / targetAmount).coerceIn(0f, 1f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("饮水历史") },
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
                containerColor = Color(0xFF26C6DA)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (waterRecords.isEmpty()) {
                EmptyWaterState()
            } else {
                // 今日饮水进度卡片
                WaterProgressCard(
                    currentAmount = todayAmount,
                    targetAmount = targetAmount,
                    isDark = isDark,
                    onTargetClick = { showTargetDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 统计摘要
                WaterStatsCard(
                    todayAmount = todayAmount,
                    targetAmount = targetAmount,
                    weeklyAverage = weeklyAverage,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 快捷添加按钮
                QuickAddButtons(
                    onAdd = { amount ->
                        viewModel.addWaterRecord(amount, null)
                    }
                )

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
                    items(waterRecords) { record ->
                        WaterRecordItemCard(
                            record = record,
                            isDark = isDark,
                            onDelete = { viewModel.deleteWaterRecord(record) }
                        )
                    }
                }
            }
        }
    }

    // 添加记录对话框
    if (showAddDialog) {
        AddWaterRecordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, note ->
                viewModel.addWaterRecord(amount, note)
                showAddDialog = false
            }
        )
    }

    // 修改目标对话框
    if (showTargetDialog) {
        UpdateTargetDialog(
            currentTarget = targetAmount,
            onDismiss = { showTargetDialog = false },
            onConfirm = { newTarget ->
                viewModel.updateTargetAmount(newTarget)
                showTargetDialog = false
            }
        )
    }
}

@Composable
private fun EmptyWaterState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF26C6DA).copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无饮水记录",
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
private fun WaterProgressCard(
    todayAmount: Int,
    targetAmount: Int,
    progress: Float,
    isDark: Boolean,
    onTargetClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日饮水",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onTargetClick)
                ) {
                    Text(
                        text = "目标: ${targetAmount}ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改目标",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 圆形进度指示器
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 背景圆环
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF26C6DA).copy(alpha = 0.2f),
                        strokeWidth = 12.dp,
                        trackColor = Color.Transparent
                    )
                    // 进度圆环
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF26C6DA),
                        strokeWidth = 12.dp,
                        trackColor = Color.Transparent
                    )
                    // 中心内容
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF26C6DA),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${todayAmount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26C6DA)
                        )
                        Text(
                            text = "/ ${targetAmount}ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度百分比
            val percentage = (progress * 100).toInt()
            Text(
                text = "已完成 ${percentage}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun WaterStatsCard(
    todayAmount: Int,
    targetAmount: Int,
    weeklyAverage: Float,
    isDark: Boolean
) {
    val remaining = (targetAmount - todayAmount).coerceAtLeast(0)
    
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
                WaterStatItem(
                    icon = Icons.Default.WaterDrop,
                    value = "${todayAmount}ml",
                    label = "今日饮水",
                    color = Color(0xFF26C6DA)
                )
                WaterStatItem(
                    icon = Icons.Default.Flag,
                    value = "${remaining}ml",
                    label = "还需",
                    color = MaterialTheme.colorScheme.primary
                )
                WaterStatItem(
                    icon = Icons.Default.CalendarToday,
                    value = "${String.format("%.0f", weeklyAverage)}ml",
                    label = "7日平均",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun WaterStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickAddButtons(
    onAdd: (Int) -> Unit
) {
    val quickAmounts = listOf(100, 200, 250, 500)
    
    Column {
        Text(
            text = "快捷添加",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { amount ->
                OutlinedButton(
                    onClick = { onAdd(amount) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+${amount}ml")
                }
            }
        }
    }
}

@Composable
private fun WaterRecordItemCard(
    record: WaterRecord,
    isDark: Boolean,
    onDelete: () -> Unit
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
                    .background(Color(0xFF26C6DA).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF26C6DA)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.amount} ml",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(record.recordTime)),
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

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AddWaterRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加饮水记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("饮水量 (ml)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
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
                    val amount = amountText.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount, noteText.takeIf { it.isNotBlank() })
                    }
                },
                enabled = amountText.toIntOrNull() != null
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
private fun UpdateTargetDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var targetText by remember { mutableStateOf(currentTarget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改每日目标") },
        text = {
            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it },
                label = { Text("目标饮水量 (ml)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val target = targetText.toIntOrNull()
                    if (target != null && target > 0) {
                        onConfirm(target)
                    }
                },
                enabled = targetText.toIntOrNull() != null
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
