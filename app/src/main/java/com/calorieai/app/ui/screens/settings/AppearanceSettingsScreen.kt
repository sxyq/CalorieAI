package com.calorieai.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.SettingsTopAppBar

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
            // 主题模式选择
            SettingsSection(title = "主题") {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = viewModel::updateThemeMode
                )
            }

            // 字体大小
            SettingsSection(title = "字体") {
                FontSizeSelector(
                    selectedSize = uiState.fontSize,
                    onSizeSelected = viewModel::updateFontSize
                )
            }

            // 壁纸设置
            SettingsSection(title = "壁纸") {
                WallpaperSelector(
                    selectedType = uiState.wallpaperType,
                    wallpaperColor = uiState.wallpaperColor,
                    gradientStart = uiState.wallpaperGradientStart,
                    gradientEnd = uiState.wallpaperGradientEnd,
                    imageUri = uiState.wallpaperImageUri,
                    onTypeSelected = viewModel::updateWallpaperType,
                    onColorSelected = viewModel::updateWallpaperColor,
                    onGradientSelected = viewModel::updateWallpaperGradient,
                    onImageSelected = viewModel::updateWallpaperImage,
                    onResetWallpaper = viewModel::resetWallpaperToDefault
                )
            }

            // AI助手设置
            SettingsSection(title = "AI助手") {
                AIWidgetToggle(
                    checked = uiState.showAIWidget,
                    onCheckedChange = viewModel::updateShowAIWidget
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

// 枚举定义
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class FontSize {
    SMALL, MEDIUM, LARGE
}

/**
 * 壁纸选择器
 */
@Composable
private fun WallpaperSelector(
    selectedType: WallpaperType,
    wallpaperColor: String?,
    gradientStart: String?,
    gradientEnd: String?,
    imageUri: String?,
    onTypeSelected: (WallpaperType) -> Unit,
    onColorSelected: (String?) -> Unit,
    onGradientSelected: (String?, String?) -> Unit,
    onImageSelected: (String?) -> Unit,
    onResetWallpaper: () -> Unit
) {
    val defaultColor = AppearanceSettingsViewModel.DEFAULT_LIGHT_WALLPAPER_COLOR
    val isDefaultWallpaper = selectedType == WallpaperType.SOLID &&
        (wallpaperColor.isNullOrBlank() || wallpaperColor.equals(defaultColor, ignoreCase = true)) &&
        imageUri.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选择壁纸类型",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onResetWallpaper,
                enabled = !isDefaultWallpaper
            ) {
                Text("恢复壁纸默认")
            }
        }

        // 壁纸类型选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WallpaperTypeOption(
                type = WallpaperType.GRADIENT,
                isSelected = selectedType == WallpaperType.GRADIENT,
                onClick = { onTypeSelected(WallpaperType.GRADIENT) }
            )
            WallpaperTypeOption(
                type = WallpaperType.SOLID,
                isSelected = selectedType == WallpaperType.SOLID,
                onClick = { onTypeSelected(WallpaperType.SOLID) }
            )
            WallpaperTypeOption(
                type = WallpaperType.IMAGE,
                isSelected = selectedType == WallpaperType.IMAGE,
                onClick = { onTypeSelected(WallpaperType.IMAGE) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 根据类型显示不同选项
        when (selectedType) {
            WallpaperType.GRADIENT -> {
                GradientColorSelector(
                    startColor = gradientStart,
                    endColor = gradientEnd,
                    onColorsSelected = onGradientSelected
                )
            }
            WallpaperType.SOLID -> {
                SolidColorSelector(
                    selectedColor = wallpaperColor,
                    onColorSelected = onColorSelected
                )
            }
            WallpaperType.IMAGE -> {
                ImageWallpaperSelector(
                    imageUri = imageUri,
                    onImageSelected = onImageSelected
                )
            }
        }
    }
}

@Composable
private fun WallpaperTypeOption(
    type: WallpaperType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = when (type) {
        WallpaperType.GRADIENT -> "渐变"
        WallpaperType.SOLID -> "纯色"
        WallpaperType.IMAGE -> "图片"
    }

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun GradientColorSelector(
    startColor: String?,
    endColor: String?,
    onColorsSelected: (String?, String?) -> Unit
) {
    val presets = listOf(
        Pair("#667eea", "#764ba2"),  // 紫蓝渐变
        Pair("#f093fb", "#f5576c"),  // 粉紫渐变
        Pair("#4facfe", "#00f2fe"),  // 蓝青渐变
        Pair("#43e97b", "#38f9d7"),  // 绿青渐变
        Pair("#fa709a", "#fee140"),  // 粉黄渐变
        Pair("#a8edea", "#fed6e3"),  // 浅蓝粉渐变
    )

    Column {
        Text(
            text = "选择渐变配色",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 使用Row换行排列渐变色
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.chunked(3).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowColors.forEach { (start, end) ->
                        val isSelected = startColor == start && endColor == end
                        GradientColorPreset(
                            startColor = start,
                            endColor = end,
                            isSelected = isSelected,
                            onClick = { onColorsSelected(start, end) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 填充剩余空间
                    repeat(3 - rowColors.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientColorPreset(
    startColor: String,
    endColor: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start = Color(android.graphics.Color.parseColor(startColor))
    val end = Color(android.graphics.Color.parseColor(endColor))

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(start, end)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SolidColorSelector(
    selectedColor: String?,
    onColorSelected: (String?) -> Unit
) {
    val presets = listOf(
        "#FF6B6B",  // 红色
        "#4ECDC4",  // 青色
        "#45B7D1",  // 蓝色
        "#96CEB4",  // 绿色
        "#FFEAA7",  // 黄色
        "#DDA0DD",  // 紫色
        "#98D8C8",  // 薄荷绿
        "#F7DC6F",  // 金色
    )

    Column {
        Text(
            text = "选择背景颜色",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 使用Row换行排列颜色
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowColors.forEach { color ->
                        val isSelected = selectedColor == color
                        SolidColorPreset(
                            color = color,
                            isSelected = isSelected,
                            onClick = { onColorSelected(color) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 填充剩余空间
                    repeat(4 - rowColors.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SolidColorPreset(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorValue = Color(android.graphics.Color.parseColor(color))

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colorValue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ImageWallpaperSelector(
    imageUri: String?,
    onImageSelected: (String?) -> Unit
) {
    // 图片选择器
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it.toString()) }
    }

    Column {
        Text(
            text = "选择图片壁纸",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (imageUri != null) {
            // 显示已选择的图片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "壁纸预览",
                        modifier = Modifier.fillMaxSize()
                    )
                    // 删除按钮
                    IconButton(
                        onClick = { onImageSelected(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            // 选择图片按钮
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { imagePicker.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击选择图片",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// 使用Compose Foundation的FlowRow（需要添加依赖）
// 这里使用简化的Row+Column实现
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // 简化为Column包含多行Row的实现
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

/**
 * AI助手开关
 */
@Composable
private fun AIWidgetToggle(
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
                text = "显示AI助手",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "在首页显示AI助手悬浮按钮",
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
