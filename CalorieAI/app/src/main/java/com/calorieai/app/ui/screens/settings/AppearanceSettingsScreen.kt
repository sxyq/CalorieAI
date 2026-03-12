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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 界面外观设置页面
 * 参考Deadliner的界面外观设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("界面外观") },
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
            // 主题模式选择
            SettingsSection(title = "主题") {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = viewModel::updateThemeMode
                )
            }

            // 主界面风格
            SettingsSection(title = "主界面") {
                SettingsSwitchItem(
                    title = "主界面风格",
                    subtitle = "调整Deadliner的布局与显示风格",
                    checked = uiState.useDeadlinerStyle,
                    onCheckedChange = viewModel::updateDeadlinerStyle
                )
            }

            // 设计
            SettingsSection(title = "设计") {
                SettingsSwitchItem(
                    title = "分割线留白设计",
                    subtitle = "开启后，界面中的分割线将会被隐藏",
                    checked = uiState.hideDividers,
                    onCheckedChange = viewModel::updateHideDividers
                )
            }

            // 字体大小
            SettingsSection(title = "字体") {
                FontSizeSelector(
                    selectedSize = uiState.fontSize,
                    onSizeSelected = viewModel::updateFontSize
                )
            }

            // 界面动画
            SettingsSection(title = "动画") {
                SettingsSwitchItem(
                    title = "界面动画",
                    subtitle = "开启界面切换和列表动画效果",
                    checked = uiState.enableAnimations,
                    onCheckedChange = viewModel::updateEnableAnimations
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 主题选择器
 */
@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 16.dp)
    ) {
        ThemeMode.values().forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (theme == selectedTheme),
                        onClick = { onThemeSelected(theme) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (theme == selectedTheme),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (theme) {
                            ThemeMode.LIGHT -> "浅色"
                            ThemeMode.DARK -> "深色"
                            ThemeMode.SYSTEM -> "跟随系统"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (theme) {
                            ThemeMode.LIGHT -> "始终使用浅色主题"
                            ThemeMode.DARK -> "始终使用深色主题"
                            ThemeMode.SYSTEM -> "根据系统设置自动切换"
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
 * 字体大小选择器
 */
@Composable
private fun FontSizeSelector(
    selectedSize: FontSize,
    onSizeSelected: (FontSize) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FontSize.values().forEach { size ->
            val isSelected = size == selectedSize
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .selectable(
                        selected = isSelected,
                        onClick = { onSizeSelected(size) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (size) {
                            FontSize.SMALL -> "小"
                            FontSize.MEDIUM -> "中"
                            FontSize.LARGE -> "大"
                        },
                        fontSize = when (size) {
                            FontSize.SMALL -> 12.sp
                            FontSize.MEDIUM -> 14.sp
                            FontSize.LARGE -> 16.sp
                        },
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

// 枚举定义
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class FontSize {
    SMALL, MEDIUM, LARGE
}
