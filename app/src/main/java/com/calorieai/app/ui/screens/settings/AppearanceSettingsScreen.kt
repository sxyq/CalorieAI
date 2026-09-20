package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.SettingsTopAppBar
import com.calorieai.app.ui.navigation.UiProfile

/**
 * 鐣岄潰澶栬璁剧疆椤甸潰
 * 鍙傝€僁eadliner鐨勭晫闈㈠瑙傝缃?
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
            SettingsTopAppBar(
                title = "界面外观",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState(), enabled = true)
        ) {
            // 涓婚妯″紡閫夋嫨
            SettingsSection(title = "主题") {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = viewModel::updateThemeMode
                )
            }

            // 瀛椾綋澶у皬
            SettingsSection(title = "字体") {
                FontSizeSelector(
                    selectedSize = uiState.fontSize,
                    onSizeSelected = viewModel::updateFontSize
                )
            }

            SettingsSection(title = "显示模式") {
                SimplifiedModeToggle(
                    checked = uiState.simplifiedMode,
                    onCheckedChange = viewModel::updateSimplifiedMode
                )
            }

            if (!UiProfile(uiState.simplifiedMode).hidesWaterSettings) {
                SettingsSection(title = "饮水功能") {
                WaterFeatureToggle(
                    checked = uiState.showWaterFeatures,
                    onCheckedChange = viewModel::updateShowWaterFeatures
                )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

}

/**
 * 涓婚閫夋嫨鍣?
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
 * 瀛椾綋澶у皬閫夋嫨鍣?
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
            val label = when (size) {
                FontSize.SMALL -> "小"
                FontSize.MEDIUM -> "中"
                FontSize.LARGE -> "大"
            }
            val fontSize = when (size) {
                FontSize.SMALL -> 12.sp
                FontSize.MEDIUM -> 14.sp
                FontSize.LARGE -> 16.sp
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                        }
                    )
                    .clickable { onSizeSelected(size) }
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = fontSize,
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

// 鏋氫妇瀹氫箟
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class FontSize {
    SMALL, MEDIUM, LARGE
}

@Composable
private fun WaterFeatureToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "显示饮水相关功能",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "关闭后会在首页、概览、记录入口等位置隐藏饮水模块",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SimplifiedModeToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "简洁模式",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "只显示首页、我的、饮食、体重和核心统计",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
