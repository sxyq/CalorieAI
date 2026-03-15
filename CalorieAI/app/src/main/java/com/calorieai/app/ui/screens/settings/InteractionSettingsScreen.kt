package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background

/**
 * 交互与行为设置页面
 * 参考Deadliner的交互设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InteractionSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交互与行为") },
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
            // 操作反馈
            SettingsSection(title = "操作反馈") {
                FeedbackTypeSelector(
                    selectedType = uiState.feedbackType,
                    onTypeSelected = viewModel::updateFeedbackType
                )
            }

            // 振动反馈
            SettingsSection(title = "振动") {
                SettingsSwitchItem(
                    title = "启用振动反馈",
                    subtitle = "操作完成时提供振动反馈",
                    checked = uiState.enableVibration,
                    onCheckedChange = viewModel::updateEnableVibration
                )
            }

            // 声音反馈
            SettingsSection(title = "声音") {
                SettingsSwitchItem(
                    title = "启用声音反馈",
                    subtitle = "操作完成时播放提示音",
                    checked = uiState.enableSound,
                    onCheckedChange = viewModel::updateEnableSound
                )
            }

            // 应用后台行为
            SettingsSection(title = "后台行为") {
                BackgroundBehaviorSelector(
                    selectedBehavior = uiState.backgroundBehavior,
                    onBehaviorSelected = viewModel::updateBackgroundBehavior
                )
            }

            // 启动页面
            SettingsSection(title = "启动") {
                StartupPageSelector(
                    selectedPage = uiState.startupPage,
                    onPageSelected = viewModel::updateStartupPage
                )
            }

            // 快速添加
            SettingsSection(title = "快速操作") {
                SettingsSwitchItem(
                    title = "快速添加",
                    subtitle = "长按主页加号按钮直接进入手动录入",
                    checked = uiState.enableQuickAdd,
                    onCheckedChange = viewModel::updateEnableQuickAdd
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

}

/**
 * 操作反馈类型选择器
 */
@Composable
private fun FeedbackTypeSelector(
    selectedType: FeedbackType,
    onTypeSelected: (FeedbackType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 16.dp)
    ) {
        FeedbackType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (type == selectedType),
                        onClick = { onTypeSelected(type) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (type == selectedType),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (type) {
                            FeedbackType.NONE -> "无反馈"
                            FeedbackType.VIBRATION -> "仅振动"
                            FeedbackType.SOUND -> "仅声音"
                            FeedbackType.BOTH -> "振动和声音"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (type) {
                            FeedbackType.NONE -> "操作时不提供任何反馈"
                            FeedbackType.VIBRATION -> "操作时仅提供振动反馈"
                            FeedbackType.SOUND -> "操作时仅播放提示音"
                            FeedbackType.BOTH -> "操作时同时提供振动和声音反馈"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 后台行为选择器
 */
@Composable
private fun BackgroundBehaviorSelector(
    selectedBehavior: BackgroundBehavior,
    onBehaviorSelected: (BackgroundBehavior) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 16.dp)
    ) {
        BackgroundBehavior.values().forEach { behavior ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (behavior == selectedBehavior),
                        onClick = { onBehaviorSelected(behavior) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (behavior == selectedBehavior),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (behavior) {
                            BackgroundBehavior.STANDARD -> "标准"
                            BackgroundBehavior.KEEP_ALIVE -> "保持运行"
                            BackgroundBehavior.BATTERY_SAVER -> "省电模式"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (behavior) {
                            BackgroundBehavior.STANDARD -> "系统默认后台行为"
                            BackgroundBehavior.KEEP_ALIVE -> "保持应用在后台运行，及时提醒"
                            BackgroundBehavior.BATTERY_SAVER -> "减少后台活动，延长电池续航"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 启动页面选择器
 */
@Composable
private fun StartupPageSelector(
    selectedPage: StartupPage,
    onPageSelected: (StartupPage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 16.dp)
    ) {
        StartupPage.values().forEach { page ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (page == selectedPage),
                        onClick = { onPageSelected(page) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (page == selectedPage),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = when (page) {
                        StartupPage.HOME -> "首页"
                        StartupPage.STATS -> "统计"
                        StartupPage.ADD -> "添加"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// 枚举定义
enum class FeedbackType {
    NONE, VIBRATION, SOUND, BOTH
}

enum class BackgroundBehavior {
    STANDARD, KEEP_ALIVE, BATTERY_SAVER
}

enum class StartupPage {
    HOME, STATS, ADD
}
