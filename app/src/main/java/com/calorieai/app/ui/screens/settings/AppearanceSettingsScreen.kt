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

            // AI鍔╂墜璁剧疆
            SettingsSection(title = "AI助手") {
                AIWidgetToggle(
                    checked = uiState.showAIWidget,
                    onCheckedChange = viewModel::updateShowAIWidget
                )
            }

            SettingsSection(title = "饮水功能") {
                WaterFeatureToggle(
                    checked = uiState.showWaterFeatures,
                    onCheckedChange = viewModel::updateShowWaterFeatures
                )
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

/**
 * 澹佺焊閫夋嫨鍣?
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

        // 澹佺焊绫诲瀷閫夋嫨
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

        // 鏍规嵁绫诲瀷鏄剧ず涓嶅悓閫夐」
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
        Pair("#667eea", "#764ba2"),  // 绱摑娓愬彉
        Pair("#f093fb", "#f5576c"),  // 绮夌传娓愬彉
        Pair("#4facfe", "#00f2fe"),  // 钃濋潚娓愬彉
        Pair("#43e97b", "#38f9d7"),  // 缁块潚娓愬彉
        Pair("#fa709a", "#fee140"),  // 绮夐粍娓愬彉
        Pair("#a8edea", "#fed6e3"),  // 娴呰摑绮夋笎鍙?
    )

    Column {
        Text(
            text = "选择渐变配色",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 浣跨敤Row鎹㈣鎺掑垪娓愬彉鑹?
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
                    // 濉厖鍓╀綑绌洪棿
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
        "#FF6B6B",  // 绾㈣壊
        "#4ECDC4",  // 闈掕壊
        "#45B7D1",  // 钃濊壊
        "#96CEB4",  // 缁胯壊
        "#FFEAA7",  // 榛勮壊
        "#DDA0DD",  // 绱壊
        "#98D8C8",  // 钖勮嵎缁?
        "#F7DC6F",  // 閲戣壊
    )

    Column {
        Text(
            text = "选择背景颜色",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 浣跨敤Row鎹㈣鎺掑垪棰滆壊
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
                    // 濉厖鍓╀綑绌洪棿
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
    // 鍥剧墖閫夋嫨鍣?
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
            // 鏄剧ず宸查€夋嫨鐨勫浘鐗?
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
                    // 鍒犻櫎鎸夐挳
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
            // 閫夋嫨鍥剧墖鎸夐挳
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

// 浣跨敤Compose Foundation鐨凢lowRow锛堥渶瑕佹坊鍔犱緷璧栵級
// 杩欓噷浣跨敤绠€鍖栫殑Row+Column瀹炵幇
@Suppress("UNUSED_PARAMETER")
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // 绠€鍖栦负Column鍖呭惈澶氳Row鐨勫疄鐜?
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

/**
 * AI鍔╂墜寮€鍏?
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
