package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InteractionSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SettingsTopAppBar(
                title = "交互与行为",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            EnumSelectorCard(
                title = "操作反馈",
                items = FeedbackType.entries,
                selectedItem = uiState.feedbackType,
                onItemSelected = viewModel::updateFeedbackType,
                itemLabel = { it.label },
                itemDescription = { it.description }
            )

            SettingsSection(title = "振动") {
                SettingsSwitchItem(
                    title = "启用振动反馈",
                    subtitle = "操作完成时提供振动反馈",
                    checked = uiState.enableVibration,
                    onCheckedChange = viewModel::updateEnableVibration
                )
            }

            SettingsSection(title = "声音") {
                SettingsSwitchItem(
                    title = "启用声音反馈",
                    subtitle = "操作完成时播放提示音",
                    checked = uiState.enableSound,
                    onCheckedChange = viewModel::updateEnableSound
                )
            }

            EnumSelectorCard(
                title = "后台行为",
                items = BackgroundBehavior.entries,
                selectedItem = uiState.backgroundBehavior,
                onItemSelected = viewModel::updateBackgroundBehavior,
                itemLabel = { it.label },
                itemDescription = { it.description }
            )

            EnumSelectorCard(
                title = "启动页面",
                items = StartupPage.entries,
                selectedItem = uiState.startupPage,
                onItemSelected = viewModel::updateStartupPage,
                itemLabel = { it.label }
            )

            SettingsSection(title = "快速操作") {
                SettingsSwitchItem(
                    title = "快速添加",
                    subtitle = "长按主页加号按钮直接进入AI导入添加",
                    checked = uiState.enableQuickAdd,
                    onCheckedChange = viewModel::updateEnableQuickAdd
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

enum class FeedbackType(val label: String, val description: String) {
    NONE("无反馈", "操作时不提供任何反馈"),
    VIBRATION("仅振动", "操作时仅提供振动反馈"),
    SOUND("仅声音", "操作时仅播放提示音"),
    BOTH("振动和声音", "操作时同时提供振动和声音反馈")
}

enum class BackgroundBehavior(val label: String, val description: String) {
    STANDARD("标准", "系统默认后台行为"),
    KEEP_ALIVE("保持运行", "保持应用在后台运行，及时提醒"),
    BATTERY_SAVER("省电模式", "减少后台活动，延长电池续航")
}

enum class StartupPage(val label: String) {
    HOME("首页"),
    STATS("统计"),
    ADD("添加")
}
