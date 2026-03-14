package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 通知设置页面
 * 参考Deadliner的通知设置风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBreakfastTimePicker by remember { mutableStateOf(false) }
    var showLunchTimePicker by remember { mutableStateOf(false) }
    var showDinnerTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 总开关
            SettingsSection(
                title = "主要",
                mainContent = true,
                enabled = uiState.isNotificationEnabled
            ) {
                SettingsSwitchItem(
                    title = "启用通知",
                    subtitle = "接收每日提醒和摄入目标通知",
                    checked = uiState.isNotificationEnabled,
                    onCheckedChange = viewModel::updateNotificationEnabled,
                    isMainSwitch = true
                )
            }

            if (uiState.isNotificationEnabled) {
                // 提醒时间设置
                SettingsSection(title = "提醒时间") {
                    TimePickerItem(
                        title = "早餐提醒",
                        subtitle = "记录早餐摄入",
                        time = uiState.breakfastReminderTime,
                        onClick = { showBreakfastTimePicker = true }
                    )
                    SettingsSectionDivider()
                    TimePickerItem(
                        title = "午餐提醒",
                        subtitle = "记录午餐摄入",
                        time = uiState.lunchReminderTime,
                        onClick = { showLunchTimePicker = true }
                    )
                    SettingsSectionDivider()
                    TimePickerItem(
                        title = "晚餐提醒",
                        subtitle = "记录晚餐摄入",
                        time = uiState.dinnerReminderTime,
                        onClick = { showDinnerTimePicker = true }
                    )
                }

                // 其他通知
                SettingsSection(title = "其他") {
                    SettingsSwitchItem(
                        title = "摄入目标提醒",
                        subtitle = "当接近或超过每日热量目标时提醒",
                        checked = uiState.enableGoalReminder,
                        onCheckedChange = viewModel::updateGoalReminder
                    )
                    SettingsSectionDivider()
                    SettingsSwitchItem(
                        title = "连续记录提醒",
                        subtitle = "提醒保持连续记录习惯",
                        checked = uiState.enableStreakReminder,
                        onCheckedChange = viewModel::updateStreakReminder
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 时间选择器弹窗
        if (showBreakfastTimePicker) {
            TimePickerDialog(
                initialTime = uiState.breakfastReminderTime,
                onTimeSelected = {
                    viewModel.updateBreakfastTime(it)
                    showBreakfastTimePicker = false
                },
                onDismiss = { showBreakfastTimePicker = false }
            )
        }

        if (showLunchTimePicker) {
            TimePickerDialog(
                initialTime = uiState.lunchReminderTime,
                onTimeSelected = {
                    viewModel.updateLunchTime(it)
                    showLunchTimePicker = false
                },
                onDismiss = { showLunchTimePicker = false }
            )
        }

        if (showDinnerTimePicker) {
            TimePickerDialog(
                initialTime = uiState.dinnerReminderTime,
                onTimeSelected = {
                    viewModel.updateDinnerTime(it)
                    showDinnerTimePicker = false
                },
                onDismiss = { showDinnerTimePicker = false }
            )
        }
    }

}

/**
 * 时间选择项
 */
@Composable
private fun TimePickerItem(
    title: String,
    subtitle: String,
    time: LocalTime,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 时间选择器弹窗
 */
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 小时选择
                NumberPicker(
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                    range = 0..23
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                // 分钟选择
                NumberPicker(
                    value = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    range = 0..59
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(selectedHour, selectedMinute))
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 数字选择器
 */
@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Text("▲", fontSize = 12.sp)
        }
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text("▼", fontSize = 12.sp)
        }
    }
}
