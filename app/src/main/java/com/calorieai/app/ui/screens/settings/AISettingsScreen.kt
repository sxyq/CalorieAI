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
import com.calorieai.app.service.voice.VoiceModelManager
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
    onNavigateToCallStats: () -> Unit,
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                title = { Text("AI閰嶇疆") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "杩斿洖")
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
                VoiceModelCard(
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
                    onClick = onNavigateToCallStats
                )
            }
            item {
                AddConfigButton(
                    onClick = { onNavigateToDetail(null) }
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
                        onClick = { onNavigateToDetail(config.id) },
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
                    text = "妯″瀷璋冪敤缁熻",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "鏌ョ湅 Prompt 涓庡洖澶嶇殑璋冪敤鏁版嵁",
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
        "GENERAL" to "閫氱敤鍋ュ悍",
        "DIABETES" to "鎺х硸",
        "GOUT" to "鐥涢",
        "PREGNANCY" to "瀛曟湡",
        "CHILD" to "鍎跨",
        "FITNESS" to "鍋ヨ韩"
    )
    val selectedModeLabel = modeOptions.firstOrNull { it.first == uiState.specialPopulationMode }?.second ?: "閫氱敤鍋ュ悍"

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
                text = "AI涓€у寲蹇屽彛绯荤粺",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "杩欎簺閰嶇疆浼氫綔涓篈I鎻愮ず璇嶇殑涓€閮ㄥ垎锛岀敤浜庡懆璁″垝銆佷笅涓€椁愭帹鑽愬拰鍋ュ悍鍜ㄨ銆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.dietaryAllergens,
                onValueChange = onDietaryAllergensChange,
                label = { Text("杩囨晱鍘?蹇屽彛锛堥€楀彿鍒嗛殧锛") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.flavorPreferences,
                onValueChange = onFlavorPreferencesChange,
                label = { Text("鍙ｅ懗鍋忓ソ锛堥€楀彿鍒嗛殧锛") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.budgetPreference,
                onValueChange = onBudgetPreferenceChange,
                label = { Text("棰勭畻鍋忓ソ锛堝锛氱粡娴?鍧囪　/楂樺搧璐級") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.maxCookingMinutes,
                    onValueChange = onMaxCookingMinutesChange,
                    label = { Text("鐑归オ鏃堕暱涓婇檺(鍒嗛挓)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.weeklyRecordGoalDays,
                    onValueChange = onWeeklyRecordGoalDaysChange,
                    label = { Text("姣忓懆璁板綍鐩爣(澶?") },
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
                    label = { Text("鐗瑰畾浜虹兢妯″紡") },
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
                Text("淇濆瓨涓€у寲绾︽潫")
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
                text = "娣诲姞鏂扮殑AI閰嶇疆",
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
                                text = "榛樿",
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
                            contentDescription = "鏇村"
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
                    Text("璁句负榛樿")
                }
            }
        }
    }

    // 鍒犻櫎纭瀵硅瘽妗?
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("鍒犻櫎閰嶇疆") },
            text = { Text("确定要删除 \"${config.name}\" 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("鍒犻櫎", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("鍙栨秷")
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
                    text = "API璋冪敤闄愬埗",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "鈥?榛樿API姣忓ぉ闄愬埗50娆¤皟鐢",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "鈥?瓒呰繃闄愬埗鍚庨渶绛夊緟娆℃棩閲嶇疆",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "鈥?寤鸿閰嶇疆鑷繁鐨凙PI瀵嗛挜浠ヨ幏寰楁洿楂橀搴",
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
            text = "鏆傛棤AI閰嶇疆",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "鐐瑰嚮涓婃柟鎸夐挳娣诲姞閰嶇疆",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
