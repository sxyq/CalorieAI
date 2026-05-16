package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.service.voice.VoiceModelManager
import com.calorieai.app.ui.components.TokenUsageCard
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String?) -> Unit,
    onNavigateToCallStats: () -> Unit,
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val visibleConfigs = remember(uiState.configs) {
        uiState.configs.filterNot { config ->
            config.isPreset && config.protocol != AIProtocol.LONGCAT
        }
    }

    LaunchedEffect(uiState.saveMessage) {
        val message = uiState.saveMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSaveMessage()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshVoiceModelState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { RateLimitInfoCard() }
            item {
                ResponsiveVoiceModelCard(
                    isInstalled = uiState.isVoiceModelInstalled,
                    installedLabel = uiState.installedVoiceModelLabel,
                    isDownloading = uiState.isVoiceModelDownloading,
                    isRemoving = uiState.isVoiceModelRemoving,
                    progressPercent = uiState.voiceModelProgressPercent,
                    progressMessage = uiState.voiceModelProgressMessage,
                    stage = uiState.voiceModelStage,
                    onDownload = viewModel::downloadVoiceModel,
                    onDelete = viewModel::uninstallVoiceModel,
                    onRefresh = viewModel::refreshVoiceModelState
                )
            }
            item { TokenUsageCard(stats = uiState.tokenUsageStats) }
            item {
                ModelCallStatsEntryCard(
                    onClick = {
                        runCatching(onNavigateToCallStats).onFailure {
                            viewModel.showTransientMessage("打开模型调用统计失败")
                        }
                    }
                )
            }
            item {
                AddConfigButton(
                    onClick = {
                        runCatching { onNavigateToDetail(null) }.onFailure {
                            viewModel.showTransientMessage("打开 AI 配置详情失败")
                        }
                    }
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (visibleConfigs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyConfigState()
                    }
                }
            } else {
                items(
                    items = visibleConfigs,
                    key = { it.id }
                ) { config ->
                    val isDefault = config.id == uiState.defaultConfigId
                    AIConfigItem(
                        config = config,
                        isDefault = isDefault,
                        isPreset = config.isPreset,
                        onClick = {
                            runCatching { onNavigateToDetail(config.id) }.onFailure {
                                viewModel.showTransientMessage("打开 ${config.name} 失败")
                            }
                        },
                        onSetDefault = { viewModel.setDefaultConfig(config.id) },
                        onDelete = { viewModel.deleteConfig(config) }
                    )
                }
            }
        }
    }

}

@Composable
private fun VoiceModelCard(
    isInstalled: Boolean,
    installedLabel: String?,
    isDownloading: Boolean,
    isRemoving: Boolean,
    progressPercent: Int,
    progressMessage: String?,
    stage: VoiceModelManager.OperationStage,
    onDownload: (VoiceModelManager.VoiceModelPackage) -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val isBusy = isDownloading || isRemoving

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "离线语音模型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (isInstalled) {
                    "状态：已安装（可离线语音输入）\n${installedLabel ?: ""}"
                } else {
                    "状态：未安装，请先下载模型后再使用离线语音输入"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (stage != VoiceModelManager.OperationStage.IDLE || progressPercent > 0 || !progressMessage.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { (progressPercent.coerceIn(0, 100) / 100f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = progressMessage ?: "处理中 ${progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showPicker = true },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("下载中...${progressPercent}%")
                    } else {
                        Text(if (isInstalled) "重新下载" else "下载模型")
                    }
                }

                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isBusy && isInstalled,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("删除中...")
                    } else {
                        Text("删除模型")
                    }
                }

                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("刷新状态")
                }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("选择语音模型包") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceModelOptionItem(
                        title = VoiceModelManager.VoiceModelPackage.LARGE_CN.displayName,
                        subtitle = "${VoiceModelManager.VoiceModelPackage.LARGE_CN.sizeHint}，识别更准",
                        onClick = {
                            showPicker = false
                            onDownload(VoiceModelManager.VoiceModelPackage.LARGE_CN)
                        }
                    )
                    VoiceModelOptionItem(
                        title = VoiceModelManager.VoiceModelPackage.SMALL_CN.displayName,
                        subtitle = "${VoiceModelManager.VoiceModelPackage.SMALL_CN.sizeHint}，下载更快",
                        onClick = {
                            showPicker = false
                            onDownload(VoiceModelManager.VoiceModelPackage.SMALL_CN)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResponsiveVoiceModelCard(
    isInstalled: Boolean,
    installedLabel: String?,
    isDownloading: Boolean,
    isRemoving: Boolean,
    progressPercent: Int,
    progressMessage: String?,
    stage: VoiceModelManager.OperationStage,
    onDownload: (VoiceModelManager.VoiceModelPackage) -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val isBusy = isDownloading || isRemoving

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "离线语音模型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (isInstalled) {
                    "状态：已安装（可离线语音输入）\n${installedLabel ?: ""}"
                } else {
                    "状态：未安装，请先下载模型后再使用离线语音输入"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (stage != VoiceModelManager.OperationStage.IDLE || progressPercent > 0 || !progressMessage.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { progressPercent.coerceIn(0, 100) / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = progressMessage ?: "处理中 ${progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { showPicker = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("下载中 ${progressPercent}%")
                } else {
                    Text(if (isInstalled) "重新下载语音模型" else "下载语音模型")
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isBusy && isInstalled,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("删除中")
                    } else {
                        Text("删除模型")
                    }
                }

                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("刷新状态")
                }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("选择语音模型包") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceModelOptionItem(
                        title = VoiceModelManager.VoiceModelPackage.LARGE_CN.displayName,
                        subtitle = "${VoiceModelManager.VoiceModelPackage.LARGE_CN.sizeHint}，识别更准",
                        onClick = {
                            showPicker = false
                            onDownload(VoiceModelManager.VoiceModelPackage.LARGE_CN)
                        }
                    )
                    VoiceModelOptionItem(
                        title = VoiceModelManager.VoiceModelPackage.SMALL_CN.displayName,
                        subtitle = "${VoiceModelManager.VoiceModelPackage.SMALL_CN.sizeHint}，下载更快",
                        onClick = {
                            showPicker = false
                            onDownload(VoiceModelManager.VoiceModelPackage.SMALL_CN)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun VoiceModelOptionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModelCallStatsEntryCard(
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
                tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
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
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "模型调用统计",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "查看 Prompt 与回复的调用数据",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIPersonalizationCard(
    uiState: AISettingsUiState,
    onDietaryAllergensChange: (String) -> Unit,
    onFlavorPreferencesChange: (String) -> Unit,
    onBudgetPreferenceChange: (String) -> Unit,
    onMaxCookingMinutesChange: (String) -> Unit,
    onSpecialPopulationModeChange: (String) -> Unit,
    onWeeklyRecordGoalDaysChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var modeExpanded by remember { mutableStateOf(false) }
    val modeOptions = listOf(
        "GENERAL" to "通用健康",
        "DIABETES" to "控糖",
        "GOUT" to "痛风",
        "PREGNANCY" to "孕期",
        "CHILD" to "儿童",
        "FITNESS" to "健身"
    )
    val selectedModeLabel = modeOptions.firstOrNull { it.first == uiState.specialPopulationMode }?.second ?: "通用健康"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "AI个性化偏好设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "这些配置会作为 AI 分析与建议的上下文，配置越清晰，推荐越贴近你的目标。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.dietaryAllergens,
                onValueChange = onDietaryAllergensChange,
                label = { Text("过敏/忌口（逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.flavorPreferences,
                onValueChange = onFlavorPreferencesChange,
                label = { Text("口味偏好（逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.budgetPreference,
                onValueChange = onBudgetPreferenceChange,
                label = { Text("预算偏好（如：经济/均衡/高品质）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.maxCookingMinutes,
                    onValueChange = onMaxCookingMinutesChange,
                    label = { Text("烹饪时长上限(分钟)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.weeklyRecordGoalDays,
                    onValueChange = onWeeklyRecordGoalDaysChange,
                    label = { Text("每周记录目标(天)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = !modeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedModeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("特定人群模式") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    modeOptions.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onSpecialPopulationModeChange(mode)
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("保存个性化偏好")
            }
        }
    }
}

/**
 * 娣诲姞鏂伴厤缃寜閽?
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
 * AI閰嶇疆椤?
 */
@Composable
fun AIConfigItem(
    config: AIConfig,
    isDefault: Boolean,
    isPreset: Boolean,
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
                tint = if (isPreset) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                }
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
                // 鍥炬爣
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

                // 鍚嶇О鍜屽崗璁?
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
                            AIProtocol.CLAUDE -> "Anthropic"
                            AIProtocol.KIMI -> "Kimi"
                            AIProtocol.GLM -> "GLM"
                            AIProtocol.QWEN -> "Qwen"
                            AIProtocol.DEEPSEEK -> "DeepSeek"
                            AIProtocol.GEMINI -> "Gemini"
                            AIProtocol.LONGCAT -> "LongCat"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 鏇村鎿嶄綔锛堜粎闈為璁鹃厤缃樉绀猴級
                if (!isPreset) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                    }
                }
            }

            // 妯″瀷ID
            if (config.modelId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = config.modelId,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 璁句负榛樿鎸夐挳锛堝鏋滀笉鏄粯璁わ級
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

    // 鍒犻櫎纭瀵硅瘽妗?
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除配置") },
            text = { Text("确定要删除 \"${config.name}\" 吗？") },
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
 * 璋冪敤闄愬埗淇℃伅鍗＄墖
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
                text = "• 默认 API 每天限制 50 次调用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "• 超过限制后需等待次日重置",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "• 建议配置自己的 API 密钥以获得更高额度",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * 绌洪厤缃姸鎬?
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
