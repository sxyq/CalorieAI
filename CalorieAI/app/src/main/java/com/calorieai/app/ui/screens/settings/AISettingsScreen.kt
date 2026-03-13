package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.ui.components.TokenUsageCard
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String?) -> Unit,
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                title = { Text("AI配置") },
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 调用限制提示卡片
            RateLimitInfoCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Token使用统计
            TokenUsageCard(stats = uiState.tokenUsageStats)

            Spacer(modifier = Modifier.height(16.dp))

            // 添加新配置按钮
            AddConfigButton(
                onClick = { onNavigateToDetail(null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 配置列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.configs.isEmpty()) {
                EmptyConfigState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.configs,
                        key = { it.id }
                    ) { config ->
                        AIConfigItem(
                            config = config,
                            isDefault = config.id == uiState.defaultConfigId,
                            onClick = { onNavigateToDetail(config.id) },
                            onSetDefault = { viewModel.setDefaultConfig(config.id) },
                            onDelete = { viewModel.deleteConfig(config) }
                        )
                    }
                }
            }
        }
    }
    } // End of Liquid Glass background Box
}

/**
 * 添加新配置按钮
 */
@Composable
fun AddConfigButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加新的AI配置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * AI配置项
 */
@Composable
fun AIConfigItem(
    config: AIConfig,
    isDefault: Boolean,
    onClick: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = config.icon,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 名称和协议
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = config.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "默认",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (config.protocol) {
                            AIProtocol.OPENAI -> "OpenAI"
                            AIProtocol.CLAUDE -> "Claude"
                            AIProtocol.KIMI -> "Kimi"
                            AIProtocol.GLM -> "GLM"
                            AIProtocol.QWEN -> "Qwen"
                            AIProtocol.DEEPSEEK -> "DeepSeek"
                            AIProtocol.GEMINI -> "Gemini"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 更多操作
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }
            }

            // 模型ID
            if (config.modelId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = config.modelId,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 设为默认按钮（如果不是默认）
            if (!isDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSetDefault,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("设为默认")
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除配置") },
            text = { Text("确定要删除\"${config.name}\"吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 调用限制信息卡片
 */
@Composable
fun RateLimitInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "API调用限制",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• 默认API每天限制50次调用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "• 超过限制后需等待次日重置",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "• 建议配置自己的API密钥以获得更高额度",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * 空配置状态
 */
@Composable
fun EmptyConfigState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无AI配置",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击上方按钮添加配置",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
