package com.calorieai.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 备份设置页面
 * 参考Deadliner的备份设置风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 监听导出导入结果
    LaunchedEffect(uiState.exportResult) {
        uiState.exportResult?.let { result ->
            when (result) {
                is BackupResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is BackupResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
            viewModel.clearExportResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备份") },
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
            // 说明卡片
            InfoCard(
                title = "数据备份",
                description = "导出您的所有记录数据到本地文件，或从备份文件恢复数据"
            )

            // 导入/导出按钮区域
            SettingsSection {
                // 导出数据
                SettingsButtonItem(
                    title = "导出数据",
                    subtitle = "将所有记录导出为文件",
                    icon = Icons.Default.Save,
                    onClick = { viewModel.exportData(context) }
                )
                SettingsSectionDivider()
                // 导入数据
                SettingsButtonItem(
                    title = "导入数据",
                    subtitle = "从备份文件恢复数据",
                    icon = Icons.Default.UploadFile,
                    onClick = { viewModel.importData(context) }
                )
            }

            // 自动备份设置
            SettingsSection(title = "自动备份") {
                SettingsSwitchItem(
                    title = "启用自动备份",
                    subtitle = "每周自动备份数据到本地",
                    checked = uiState.enableAutoBackup,
                    onCheckedChange = viewModel::updateAutoBackup
                )
                if (uiState.enableAutoBackup) {
                    SettingsSectionDivider()
                    Text(
                        text = "上次备份: ${uiState.lastBackupTime ?: "从未备份"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            // 云同步设置
            SettingsSection(title = "云同步") {
                SettingsSwitchItem(
                    title = "启用云同步",
                    subtitle = "自动同步数据到云端",
                    checked = uiState.enableCloudSync,
                    onCheckedChange = viewModel::updateCloudSync
                )
                if (uiState.enableCloudSync) {
                    SettingsSectionDivider()
                    SettingsButtonItem(
                        title = "立即同步",
                        subtitle = "手动触发一次云同步",
                        icon = Icons.Default.CloudUpload,
                        onClick = { viewModel.syncToCloud() }
                    )
                    SettingsSectionDivider()
                    SettingsButtonItem(
                        title = "从云端恢复",
                        subtitle = "从云端下载最新数据",
                        icon = Icons.Default.CloudDownload,
                        onClick = { viewModel.restoreFromCloud() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 信息卡片
 */
@Composable
private fun InfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 设置按钮项
 */
@Composable
private fun SettingsButtonItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
    }
}

// 备份结果密封类
sealed class BackupResult {
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}
