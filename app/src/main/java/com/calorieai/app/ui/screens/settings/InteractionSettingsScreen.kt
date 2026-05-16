package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.ui.components.EnumSelectorCard
import com.calorieai.app.ui.components.SettingsTopAppBar
import com.calorieai.app.ui.feedback.rememberAppHapticController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InteractionSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberAppHapticController()

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
                onItemSelected = {
                    viewModel.updateFeedbackType(it)
                    haptics.click()
                },
                itemLabel = { it.label },
                itemDescription = { it.description }
            )

            SettingsSection(title = "振动") {
                SettingsSwitchItem(
                    title = "启用振动反馈",
                    subtitle = "点击、切页和关键操作会有振动反馈",
                    checked = uiState.enableVibration,
                    onCheckedChange = {
                        viewModel.updateEnableVibration(it)
                        haptics.click()
                    }
                )
            }

            SettingsSection(title = "声音") {
                SettingsSwitchItem(
                    title = "启用声音反馈",
                    subtitle = "关键操作可播放提示音",
                    checked = uiState.enableSound,
                    onCheckedChange = {
                        viewModel.updateEnableSound(it)
                        haptics.click()
                    }
                )
            }

            EnumSelectorCard(
                title = "后台行为",
                items = BackgroundBehavior.entries,
                selectedItem = uiState.backgroundBehavior,
                onItemSelected = {
                    viewModel.updateBackgroundBehavior(it)
                    haptics.click()
                },
                itemLabel = { it.label },
                itemDescription = { it.description }
            )

            EnumSelectorCard(
                title = "启动页面",
                items = StartupPage.entries,
                selectedItem = uiState.startupPage,
                onItemSelected = {
                    viewModel.updateStartupPage(it)
                    haptics.click()
                },
                itemLabel = { it.label }
            )

            SettingsSection(title = "底栏长按跳转") {
                SettingsSwitchItem(
                    title = "长按首页进入添加",
                    subtitle = "长按底栏“首页”直接进入记录入口",
                    checked = uiState.enableLongPressHomeToAdd,
                    onCheckedChange = {
                        viewModel.updateEnableLongPressHomeToAdd(it)
                        haptics.click()
                    }
                )
                SettingsSwitchItem(
                    title = "长按概览进入统计",
                    subtitle = "长按底栏“概览”进入详细统计页",
                    checked = uiState.enableLongPressOverviewToStats,
                    onCheckedChange = {
                        viewModel.updateEnableLongPressOverviewToStats(it)
                        haptics.click()
                    }
                )
                SettingsSwitchItem(
                    title = "长按我的进入资料编辑",
                    subtitle = "长按底栏“我的”直接进入个人资料页",
                    checked = uiState.enableLongPressMyToProfileEdit,
                    onCheckedChange = {
                        viewModel.updateEnableLongPressMyToProfileEdit(it)
                        haptics.click()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

enum class FeedbackType(val label: String, val description: String) {
    NONE("无反馈", "不提供振动或声音反馈"),
    VIBRATION("仅振动", "仅提供振动反馈"),
    SOUND("仅声音", "仅提供声音反馈"),
    BOTH("振动+声音", "同时提供振动和声音反馈")
}

enum class BackgroundBehavior(val label: String, val description: String) {
    STANDARD("标准", "系统默认后台行为"),
    KEEP_ALIVE("保持运行", "尽量保持后台活跃，便于提醒及时"),
    BATTERY_SAVER("省电模式", "降低后台活动，延长续航")
}

enum class StartupPage(val label: String) {
    HOME("首页"),
    STATS("统计"),
    ADD("添加")
}
