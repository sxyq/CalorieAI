package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.calorieai.app.ui.components.liquidGlass

/**
 * 关于页面
 * 参考Deadliner的关于页面风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            )
        )
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("关于") },
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
            // 顶部应用信息卡片
            AppInfoCard()

            // 版本信息
            SettingsSection(title = "版本") {
                AboutItem(
                    title = "版本号",
                    subtitle = "v1.0.0",
                    icon = Icons.Default.Info,
                    showArrow = false
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "构建时间",
                    subtitle = "2026-03-12",
                    icon = Icons.Default.Update,
                    showArrow = false
                )
            }

            // 法律信息（暂不可点击）
            SettingsSection(title = "法律信息") {
                AboutItem(
                    title = "开源许可证",
                    subtitle = "查看第三方开源库许可",
                    icon = Icons.Default.Policy,
                    showArrow = false
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "隐私政策",
                    subtitle = "了解我们如何保护您的隐私",
                    icon = Icons.Default.Policy,
                    showArrow = false
                )
            }

            // 更多（暂不可点击）
            SettingsSection(title = "更多") {
                AboutItem(
                    title = "项目主页",
                    subtitle = "访问GitHub项目页面",
                    icon = Icons.Default.Info,
                    showArrow = false
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "反馈问题",
                    subtitle = "在GitHub上提交Issue",
                    icon = Icons.Default.Info,
                    showArrow = false
                )
            }

            // 底部版权信息
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "© 2026 CalorieAI\nAll rights reserved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
    } // End of Liquid Glass background Box
}

/**
 * 应用信息卡片
 */
@Composable
private fun AppInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 应用图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // 应用名称
                Text(
                    text = "CalorieAI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // 应用标语
                Text(
                    text = "智能热量记录助手",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 关于页面项
 */
@Composable
private fun AboutItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
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
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
