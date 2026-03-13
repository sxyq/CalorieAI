package com.calorieai.app.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 创建备份文件选择器
    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.createBackup(it) }
    }

    // 选择备份文件
    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadBackupInfo(it) }
    }

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
                title = { Text("备份与恢复") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI配置备份选项
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(16.dp),
                        tint = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "包含AI配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "备份时包含AI模型配置信息",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.includeAIConfigs,
                        onCheckedChange = { viewModel.setIncludeAIConfigs(it) }
                    )
                }
            }

            // 创建备份卡片
            BackupActionCard(
                title = "创建备份",
                description = "将所有数据导出为JSON文件，包括饮食记录、运动记录和设置" +
                    if (uiState.includeAIConfigs) "（包含AI配置）" else "（不包含AI配置）",
                icon = Icons.Default.Backup,
                onClick = {
                    val timestamp = java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    createBackupLauncher.launch("calorieai_backup_$timestamp.json")
                }
            )

            // 恢复备份卡片
            BackupActionCard(
                title = "恢复备份",
                description = "从备份文件恢复数据",
                icon = Icons.Default.Restore,
                onClick = {
                    openBackupLauncher.launch(arrayOf("application/json"))
                }
            )

            // 备份信息对话框
            if (uiState.showRestoreDialog && uiState.backupInfo != null) {
                RestoreConfirmDialog(
                    backupInfo = uiState.backupInfo!!,
                    onConfirm = { viewModel.confirmRestore() },
                    onDismiss = { viewModel.dismissRestoreDialog() }
                )
            }

            // 结果显示
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.resultMessage != null -> {
                    Box(
                        modifier = Modifier.liquidGlass(
                            shape = RoundedCornerShape(16.dp),
                            tint = if (uiState.isSuccess) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (uiState.isSuccess) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.resultMessage!!,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 说明文字
            Box(
                modifier = Modifier.liquidGlass(
                    shape = RoundedCornerShape(16.dp),
                    tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "备份说明",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 备份文件包含您的所有饮食记录、运动记录和个人设置",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• 建议定期备份，以防数据丢失",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• 恢复备份会覆盖当前所有数据，请谨慎操作",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• 备份文件可以跨设备使用",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    } // End of Liquid Glass background Box
}

@Composable
private fun BackupActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    backupInfo: com.calorieai.app.service.backup.BackupData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认恢复备份") },
        text = {
            Column {
                Text("备份日期: ${backupInfo.backupDate}")
                Text("饮食记录: ${backupInfo.foodRecords.size} 条")
                Text("运动记录: ${backupInfo.exerciseRecords.size} 条")
                if (backupInfo.includeAIConfigs && backupInfo.aiConfigs.isNotEmpty()) {
                    Text("AI配置: ${backupInfo.aiConfigs.size} 个")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "警告：恢复备份将覆盖当前所有数据，是否继续？",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("恢复")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
