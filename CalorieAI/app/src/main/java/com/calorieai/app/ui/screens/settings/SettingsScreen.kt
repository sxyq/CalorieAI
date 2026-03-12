package com.calorieai.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExportSuccess by remember { mutableStateOf(false) }
    var showImportSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // 导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it) { success, count ->
                if (success) {
                    showExportSuccess = true
                } else {
                    showError = "导出失败"
                }
            }
        }
    }

    // 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importData(it) { success, count ->
                if (success) {
                    showImportSuccess = true
                } else {
                    showError = "导入失败"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            // 数据备份部分
            SettingsSection(title = "数据管理") {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "导出数据",
                    subtitle = "将记录导出为JSON文件",
                    onClick = {
                        exportLauncher.launch("calorieai_backup_${System.currentTimeMillis()}.json")
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "导入数据",
                    subtitle = "从JSON文件导入记录",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                )
            }

            // 关于部分
            SettingsSection(title = "关于") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本",
                    subtitle = "v1.0.0",
                    onClick = {}
                )
            }
        }
    }

    // 成功提示
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("导出成功") },
            text = { Text("数据已成功导出") },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text("确定")
                }
            }
        )
    }

    if (showImportSuccess) {
        AlertDialog(
            onDismissRequest = { showImportSuccess = false },
            title = { Text("导入成功") },
            text = { Text("数据已成功导入") },
            confirmButton = {
                TextButton(onClick = { showImportSuccess = false }) {
                    Text("确定")
                }
            }
        )
    }

    // 错误提示
    showError?.let { error ->
        AlertDialog(
            onDismissRequest = { showError = null },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showError = null }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable { onClick() }
    )
}
