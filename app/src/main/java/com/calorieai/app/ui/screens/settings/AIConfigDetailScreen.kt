package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIConfigPresets
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.APICallRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIConfigDetailScreen(
    configId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AIConfigDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var apiKeyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(configId) {
        viewModel.loadConfig(configId)
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            uiState.isPreset -> "预设 AI 配置"
                            uiState.isEditing -> "编辑 AI 配置"
                            else -> "添加 AI 配置"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.isEditing && !uiState.isPreset) {
                PresetQuickPickSection(onPresetSelected = viewModel::applyPreset)
            }

            if (uiState.isPreset) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "这是预设配置，只能查看，不能直接修改。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("图标", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AIConfigPresets.ICON_OPTIONS.forEach { icon ->
                            FilterChip(
                                selected = uiState.selectedIcon == icon,
                                onClick = { if (!uiState.isPreset) viewModel.updateIcon(icon) },
                                enabled = !uiState.isPreset,
                                label = { Text(icon) }
                            )
                        }
                        if (uiState.selectedIcon !in AIConfigPresets.ICON_OPTIONS) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("当前: ${uiState.selectedIcon}") }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { if (!uiState.isPreset) viewModel.updateName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("配置名称") },
                placeholder = { Text("例如：我的 LongCat") },
                singleLine = true,
                enabled = !uiState.isPreset,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("协议格式", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AIProtocol.entries.forEach { protocol ->
                            FilterChip(
                                selected = uiState.protocol == protocol,
                                onClick = { if (!uiState.isPreset) viewModel.updateProtocol(protocol) },
                                enabled = !uiState.isPreset,
                                label = { Text(protocol.displayLabel()) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.apiUrl,
                onValueChange = { if (!uiState.isPreset) viewModel.updateApiUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API 地址") },
                placeholder = { Text("https://api.example.com/v1/chat/completions") },
                singleLine = true,
                enabled = !uiState.isPreset,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = { if (!uiState.isPreset) viewModel.updateApiKey(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API 密钥") },
                placeholder = { Text("sk-...") },
                singleLine = true,
                enabled = !uiState.isPreset,
                visualTransformation = if (apiKeyVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    if (!uiState.isPreset) {
                        TextButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Text(if (apiKeyVisible) "隐藏" else "显示")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.modelId,
                onValueChange = { if (!uiState.isPreset) viewModel.updateModelId(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("模型 ID") },
                placeholder = { Text("LongCat-Flash-Omni-2603") },
                singleLine = true,
                enabled = !uiState.isPreset,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("图像理解", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "允许该配置处理图片输入。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isImageUnderstanding,
                        onCheckedChange = { if (!uiState.isPreset) viewModel.updateImageUnderstanding(it) },
                        enabled = !uiState.isPreset
                    )
                }
            }

            if (!uiState.isPreset) {
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isTesting
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试中...")
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试连接")
                    }
                }
            }

            uiState.testResult?.let { result ->
                val isSuccess = result is TestResult.Success
                val title = if (isSuccess) "连接成功" else "连接失败"
                val message = when (result) {
                    is TestResult.Success -> result.message
                    is TestResult.Error -> result.message
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isSuccess) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.SemiBold)
                            Text(message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (uiState.isEditing || uiState.isPreset) {
                APICallSummarySection(
                    recentCallRecords = uiState.recentCallRecords,
                    contentPadding = PaddingValues(0.dp)
                )
            }

            if (!uiState.isPreset) {
                Button(
                    onClick = {
                        if (viewModel.saveConfig()) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun PresetQuickPickSection(
    onPresetSelected: (AIConfig) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("快速套用预设", style = MaterialTheme.typography.titleMedium)
            AIConfigPresets.ALL_PRESETS.forEach { preset ->
                OutlinedButton(
                    onClick = { onPresetSelected(preset) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(preset.name)
                }
            }
        }
    }
}

@Composable
private fun APICallSummarySection(
    recentCallRecords: List<APICallRecord>,
    contentPadding: PaddingValues
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("最近调用记录", style = MaterialTheme.typography.titleMedium)
            if (recentCallRecords.isEmpty()) {
                Text(
                    text = "暂无调用记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recentCallRecords.take(10).forEach { record ->
                    key(record.id) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatRecordTime(record.timestamp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = record.modelId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "输入 ${record.promptTokens} / 输出 ${record.completionTokens} / ${record.duration}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatRecordTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

private fun AIProtocol.displayLabel(): String = when (this) {
    AIProtocol.OPENAI -> "OpenAI"
    AIProtocol.CLAUDE -> "Claude"
    AIProtocol.KIMI -> "Kimi"
    AIProtocol.GLM -> "GLM"
    AIProtocol.QWEN -> "Qwen"
    AIProtocol.DEEPSEEK -> "DeepSeek"
    AIProtocol.GEMINI -> "Gemini"
    AIProtocol.LONGCAT -> "LongCat"
}
