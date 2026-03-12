package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToInteraction: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 界面外观
            SettingGroupItem(
                icon = Icons.Default.Palette,
                title = "界面外观",
                subtitle = "主题、颜色、字体",
                onClick = onNavigateToAppearance
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 交互与行为
            SettingGroupItem(
                icon = Icons.Default.TouchApp,
                title = "交互与行为",
                subtitle = "操作反馈、后台行为",
                onClick = onNavigateToInteraction
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 通知
            SettingGroupItem(
                icon = Icons.Default.Notifications,
                title = "通知",
                subtitle = "提醒时间配置",
                onClick = onNavigateToNotification
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 备份
            SettingGroupItem(
                icon = Icons.Default.Backup,
                title = "备份",
                subtitle = "导入与导出用户数据",
                onClick = onNavigateToBackup
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI配置
            SettingGroupItem(
                icon = Icons.Default.Psychology,
                title = "AI配置",
                subtitle = "OpenAI/Claude API设置",
                onClick = onNavigateToAISettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 关于
            SettingGroupItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "版本信息、隐私政策",
                onClick = onNavigateToAbout
            )
        }
    }
}

/**
 * 设置分组项
 * 参考Deadliner风格：卡片式布局，圆角24dp，图标+标题+副标题+箭头
 */
@Composable
fun SettingGroupItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
