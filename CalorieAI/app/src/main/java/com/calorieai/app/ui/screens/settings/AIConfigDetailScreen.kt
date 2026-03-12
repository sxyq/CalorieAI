package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIConfigPresets
import com.calorieai.app.data.model.AIProtocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIConfigDetailScreen(
    configId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AIConfigDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIconSelector by remember { mutableStateOf(false) }
    var showPresetSelector by remember { mutableStateOf(false) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(configId) {
        viewModel.loadConfig(configId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "编辑AI配置" else "添加AI配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        TextButton(
                            onClick = { showPresetSelector = true }
                        ) {
                            Text("预设")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 图标选择器
            IconSelectorSection(
                selectedIcon = uiState.selectedIcon,
                onClick = { showIconSelector = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 配置名称输入
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("配置名称") },
                placeholder = { Text("例如：我的OpenAI") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 协议选择
            ProtocolSelector(
                selectedProtocol = uiState.protocol,
                onProtocolSelected = viewModel::updateProtocol
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API地址输入
            OutlinedTextField(
                value = uiState.apiUrl,
                onValueChange = viewModel::updateApiUrl,
                label = { Text("API地址") },
                placeholder = { Text("https://api.openai.com/v1/chat/completions") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API密钥输入
            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API密钥") },
                placeholder = { Text("sk-...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            imageVector = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (apiKeyVisible) "隐藏" else "显示"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 模型ID输入
            OutlinedTextField(
                value = uiState.modelId,
                onValueChange = viewModel::updateModelId,
                label = { Text("模型ID") },
                placeholder = { Text("gpt-4o 或 claude-3-5-sonnet-20241022") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 测试连接按钮
            TestConnectionButton(
                isTesting = uiState.isTesting,
                testResult = uiState.testResult,
                onTest = viewModel::testConnection,
                onClearResult = viewModel::clearTestResult
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 图像理解开关
            ImageUnderstandingCard(
                isEnabled = uiState.isImageUnderstanding,
                onToggle = viewModel::updateImageUnderstanding
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (viewModel.saveConfig()) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "保存",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 图标选择器弹窗
    if (showIconSelector) {
        IconSelectorDialog(
            selectedIcon = uiState.selectedIcon,
            onIconSelected = {
                viewModel.updateIcon(it)
                showIconSelector = false
            },
            onDismiss = { showIconSelector = false }
        )
    }

    // 预设选择器弹窗
    if (showPresetSelector) {
        PresetSelectorDialog(
            onPresetSelected = {
                viewModel.applyPreset(it)
                showPresetSelector = false
            },
            onDismiss = { showPresetSelector = false }
        )
    }
}

/**
 * 图标选择区域
 */
@Composable
fun IconSelectorSection(
    selectedIcon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedIcon,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "选择图标",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "点击更换",
                    fontSize = 13.sp,
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

/**
 * 协议选择器
 */
@Composable
fun ProtocolSelector(
    selectedProtocol: AIProtocol,
    onProtocolSelected: (AIProtocol) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "协议格式",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProtocolOption(
                    protocol = AIProtocol.OPENAI,
                    title = "OpenAI",
                    isSelected = selectedProtocol == AIProtocol.OPENAI,
                    onClick = { onProtocolSelected(AIProtocol.OPENAI) },
                    modifier = Modifier.weight(1f)
                )

                ProtocolOption(
                    protocol = AIProtocol.CLAUDE,
                    title = "Claude",
                    isSelected = selectedProtocol == AIProtocol.CLAUDE,
                    onClick = { onProtocolSelected(AIProtocol.CLAUDE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 协议选项
 */
@Composable
fun ProtocolOption(
    protocol: AIProtocol,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 测试连接按钮
 */
@Composable
fun TestConnectionButton(
    isTesting: Boolean,
    testResult: TestResult?,
    onTest: () -> Unit,
    onClearResult: () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onTest,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isTesting
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("测试中...")
            } else {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("测试连接")
            }
        }

        testResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (result) {
                        is TestResult.Success -> MaterialTheme.colorScheme.primaryContainer
                        is TestResult.Error -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                modifier = Modifier.clickable { onClearResult() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (result) {
                            is TestResult.Success -> Icons.Default.CheckCircle
                            is TestResult.Error -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (result) {
                            is TestResult.Success -> MaterialTheme.colorScheme.primary
                            is TestResult.Error -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (result) {
                            is TestResult.Success -> result.message
                            is TestResult.Error -> result.message
                        },
                        color = when (result) {
                            is TestResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                            is TestResult.Error -> MaterialTheme.colorScheme.onErrorContainer
                        },
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * 图像理解开关卡片
 */
@Composable
fun ImageUnderstandingCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "图像理解",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "允许AI识别食物图片",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * 图标选择器弹窗
 */
@Composable
fun IconSelectorDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            Column {
                // 使用两行显示图标选项
                val iconsPerRow = 4
                val iconOptions = AIConfigPresets.ICON_OPTIONS

                for (rowIndex in iconOptions.indices step iconsPerRow) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (colIndex in 0 until iconsPerRow) {
                            val iconIndex = rowIndex + colIndex
                            if (iconIndex < iconOptions.size) {
                                val icon = iconOptions[iconIndex]
                                val isSelected = icon == selectedIcon
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable { onIconSelected(icon) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                    if (rowIndex + iconsPerRow < iconOptions.size) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 预设选择器弹窗
 */
@Composable
fun PresetSelectorDialog(
    onPresetSelected: (AIConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择预设") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // OpenAI 预设
                PresetCategory(title = "OpenAI")
                PresetItem(
                    icon = "🅞",
                    name = AIConfigPresets.OPENAI_GPT5.name,
                    description = "最新旗舰模型",
                    onClick = { onPresetSelected(AIConfigPresets.OPENAI_GPT5) }
                )
                PresetItem(
                    icon = "🅞",
                    name = AIConfigPresets.OPENAI_GPT4O.name,
                    description = "多模态模型",
                    onClick = { onPresetSelected(AIConfigPresets.OPENAI_GPT4O) }
                )

                // Claude 预设
                PresetCategory(title = "Anthropic")
                PresetItem(
                    icon = "🅒",
                    name = AIConfigPresets.CLAUDE_4_6_OPUS.name,
                    description = "最强推理能力",
                    onClick = { onPresetSelected(AIConfigPresets.CLAUDE_4_6_OPUS) }
                )
                PresetItem(
                    icon = "🅒",
                    name = AIConfigPresets.CLAUDE_3_5_SONNET.name,
                    description = "均衡性能",
                    onClick = { onPresetSelected(AIConfigPresets.CLAUDE_3_5_SONNET) }
                )

                // 国产模型
                PresetCategory(title = "国产模型")
                PresetItem(
                    icon = "🅚",
                    name = AIConfigPresets.KIMI_K2_5.name,
                    description = "Moonshot",
                    onClick = { onPresetSelected(AIConfigPresets.KIMI_K2_5) }
                )
                PresetItem(
                    icon = "🅖",
                    name = AIConfigPresets.GLM_4_PLUS.name,
                    description = "智谱AI",
                    onClick = { onPresetSelected(AIConfigPresets.GLM_4_PLUS) }
                )
                PresetItem(
                    icon = "🅠",
                    name = AIConfigPresets.QWEN_2_5_MAX.name,
                    description = "阿里云",
                    onClick = { onPresetSelected(AIConfigPresets.QWEN_2_5_MAX) }
                )
                PresetItem(
                    icon = "🅓",
                    name = AIConfigPresets.DEEPSEEK_V3.name,
                    description = "深度求索",
                    onClick = { onPresetSelected(AIConfigPresets.DEEPSEEK_V3) }
                )
                PresetItem(
                    icon = "🅓",
                    name = AIConfigPresets.DEEPSEEK_R1.name,
                    description = "深度求索 - 推理模型",
                    onClick = { onPresetSelected(AIConfigPresets.DEEPSEEK_R1) }
                )

                // Google
                PresetCategory(title = "Google")
                PresetItem(
                    icon = "🅖",
                    name = AIConfigPresets.GEMINI_2_0_PRO.name,
                    description = "多模态模型",
                    onClick = { onPresetSelected(AIConfigPresets.GEMINI_2_0_PRO) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 预设分类标题
 */
@Composable
fun PresetCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

/**
 * 预设项
 */
@Composable
fun PresetItem(
    icon: String,
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
            Text(
                text = icon,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
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
