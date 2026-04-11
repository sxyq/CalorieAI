package com.calorieai.app.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.SettingsTopAppBar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private sealed interface TimePickerTarget {
    data object Breakfast : TimePickerTarget
    data object Lunch : TimePickerTarget
    data object Dinner : TimePickerTarget
    data class WaterSlot(val index: Int) : TimePickerTarget
    data object WaterWindowStart : TimePickerTarget
    data object WaterWindowEnd : TimePickerTarget
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val needRuntimeNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var pickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }
    val hasNotificationPermission = remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission.value = granted
        if (granted) {
            viewModel.updateNotificationEnabled(true)
        } else {
            viewModel.updateNotificationEnabled(false)
            scope.launch {
                snackbarHostState.showSnackbar("通知权限未授予，无法开启提醒")
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            SettingsTopAppBar(
                title = "通知",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(
                title = "主要",
                mainContent = true,
                enabled = uiState.isNotificationEnabled
            ) {
                SettingsSwitchItem(
                    title = "启用通知",
                    subtitle = "接收餐次和喝水提醒",
                    checked = uiState.isNotificationEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            viewModel.updateNotificationEnabled(false)
                            return@SettingsSwitchItem
                        }

                        if (!needRuntimeNotificationPermission || hasNotificationPermission.value) {
                            viewModel.updateNotificationEnabled(true)
                        } else {
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    isMainSwitch = true
                )
            }

            if (uiState.isNotificationEnabled) {
                SettingsSection(title = "餐次提醒") {
                    TimePickerItem(
                        title = "早餐提醒",
                        subtitle = "记录早餐摄入",
                        time = uiState.breakfastReminderTime,
                        onClick = { pickerTarget = TimePickerTarget.Breakfast }
                    )
                    SettingsSectionDivider()
                    TimePickerItem(
                        title = "午餐提醒",
                        subtitle = "记录午餐摄入",
                        time = uiState.lunchReminderTime,
                        onClick = { pickerTarget = TimePickerTarget.Lunch }
                    )
                    SettingsSectionDivider()
                    TimePickerItem(
                        title = "晚餐提醒",
                        subtitle = "记录晚餐摄入",
                        time = uiState.dinnerReminderTime,
                        onClick = { pickerTarget = TimePickerTarget.Dinner }
                    )
                }

                if (uiState.showWaterFeatures) {
                    SettingsSection(title = "喝水提醒") {
                        SettingsSwitchItem(
                            title = "启用喝水提醒",
                            subtitle = "支持固定时段 + 间隔提醒",
                            checked = uiState.enableWaterReminder,
                            onCheckedChange = viewModel::updateWaterReminderEnabled
                        )

                        if (uiState.enableWaterReminder) {
                            uiState.waterReminderTimes.forEachIndexed { index, time ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TimePickerItem(
                                        title = "固定时段 ${index + 1}",
                                        subtitle = "在该时间点发出喝水提醒",
                                        time = time,
                                        onClick = { pickerTarget = TimePickerTarget.WaterSlot(index) }
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeWaterReminderTime(index) },
                                        enabled = uiState.waterReminderTimes.size > 1
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除")
                                    }
                                }
                            }

                            if (uiState.waterReminderTimes.size < 8) {
                                Button(
                                    onClick = { viewModel.addWaterReminderTime() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("新增时段")
                                }
                            }

                            OutlinedTextField(
                                value = uiState.waterReminderIntervalMinutes,
                                onValueChange = viewModel::updateWaterIntervalMinutes,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                label = { Text("间隔提醒（分钟，0 表示关闭）") },
                                singleLine = true
                            )

                            TimePickerItem(
                                title = "提醒时间窗开始",
                                subtitle = "间隔提醒开始生效时间",
                                time = uiState.waterReminderWindowStart,
                                onClick = { pickerTarget = TimePickerTarget.WaterWindowStart }
                            )
                            TimePickerItem(
                                title = "提醒时间窗结束",
                                subtitle = "间隔提醒停止时间",
                                time = uiState.waterReminderWindowEnd,
                                onClick = { pickerTarget = TimePickerTarget.WaterWindowEnd }
                            )
                        }
                    }
                }

                SettingsSection(title = "其他") {
                    SettingsSwitchItem(
                        title = "目标提醒",
                        subtitle = "接近或超过每日热量目标时提醒",
                        checked = uiState.enableGoalReminder,
                        onCheckedChange = viewModel::updateGoalReminder
                    )
                    SettingsSectionDivider()
                    SettingsSwitchItem(
                        title = "连续记录提醒",
                        subtitle = "提醒保持连续打卡习惯",
                        checked = uiState.enableStreakReminder,
                        onCheckedChange = viewModel::updateStreakReminder
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        val target = pickerTarget
        if (target != null) {
            val initialTime = when (target) {
                TimePickerTarget.Breakfast -> uiState.breakfastReminderTime
                TimePickerTarget.Lunch -> uiState.lunchReminderTime
                TimePickerTarget.Dinner -> uiState.dinnerReminderTime
                is TimePickerTarget.WaterSlot -> uiState.waterReminderTimes.getOrNull(target.index)
                    ?: LocalTime.of(10, 0)
                TimePickerTarget.WaterWindowStart -> uiState.waterReminderWindowStart
                TimePickerTarget.WaterWindowEnd -> uiState.waterReminderWindowEnd
            }

            TimePickerDialog(
                initialTime = initialTime,
                onTimeSelected = { selected ->
                    when (target) {
                        TimePickerTarget.Breakfast -> viewModel.updateBreakfastTime(selected)
                        TimePickerTarget.Lunch -> viewModel.updateLunchTime(selected)
                        TimePickerTarget.Dinner -> viewModel.updateDinnerTime(selected)
                        is TimePickerTarget.WaterSlot -> viewModel.updateWaterReminderTime(target.index, selected)
                        TimePickerTarget.WaterWindowStart -> viewModel.updateWaterWindowStart(selected)
                        TimePickerTarget.WaterWindowEnd -> viewModel.updateWaterWindowEnd(selected)
                    }
                    pickerTarget = null
                },
                onDismiss = { pickerTarget = null }
            )
        }
    }
}

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
