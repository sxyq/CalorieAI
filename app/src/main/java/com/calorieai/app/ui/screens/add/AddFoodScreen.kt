package com.calorieai.app.ui.screens.add

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName
import com.calorieai.app.service.voice.VoiceInputHelper
import com.calorieai.app.service.voice.VoiceModelManager
import com.calorieai.app.ui.components.VoiceInputDialog
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    selectedDate: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: AddFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 设置选中的日期
    LaunchedEffect(selectedDate) {
        selectedDate?.let {
            viewModel.setSelectedDate(it)
        }
    }
    
    // 语音输入状态
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // 语音输入帮助类
    val voiceHelper = remember { VoiceInputHelper(VoiceModelManager(context.applicationContext)) }
    val voiceState by voiceHelper.voiceState.collectAsState()
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceInput(context, voiceHelper, viewModel, onStart = {
                isListening = true
                showVoiceDialog = true
            })
        } else {
            showPermissionDialog = true
        }
    }
    
    // 监听语音状态
    LaunchedEffect(voiceState) {
        when (val state = voiceState) {
            is com.calorieai.app.service.voice.VoiceState.Success -> {
                viewModel.onFoodDescriptionChange(
                    if (uiState.foodDescription.isBlank()) state.text 
                    else "${uiState.foodDescription} ${state.text}"
                )
                isListening = false
            }
            is com.calorieai.app.service.voice.VoiceState.Error -> {
                isListening = false
            }
            else -> {}
        }
    }
    
    Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("记录食物") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 输入方式选择 - 软玻璃按钮
                SoftInputMethodSelector(
                    onCameraClick = onNavigateToCamera,
                    onVoiceClick = {
                        when {
                            isListening -> {
                                voiceHelper.stopListening()
                                isListening = false
                                showVoiceDialog = false
                            }
                            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                                startVoiceInput(context, voiceHelper, viewModel, onStart = {
                                    isListening = true
                                    showVoiceDialog = true
                                })
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    isVoiceListening = isListening
                )
                
                // 餐次选择 - 软玻璃分段按钮
                SoftMealTypeSelector(
                    selectedMealType = uiState.selectedMealType,
                    onMealTypeSelected = viewModel::onMealTypeChange
                )
                
                // 食物描述输入 - 软玻璃输入框
                SoftTextInputField(
                    value = uiState.foodDescription,
                    onValueChange = viewModel::onFoodDescriptionChange,
                    placeholder = "描述你吃的食物，例如：番茄炒蛋，番茄150g，鸡蛋2个..."
                )
                
                // 快捷输入示例 - 软玻璃卡片
                SoftExampleCard()
                
                // 重试提示
                if (uiState.retryMessage != null) {
                    RetryMessageCard(
                        message = uiState.retryMessage!!,
                        attempt = uiState.retryAttempt,
                        maxAttempts = uiState.maxRetries + 1
                    )
                }
                
                // 错误提示
                if (uiState.errorMessage != null) {
                    ErrorMessageCard(
                        message = uiState.errorMessage!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                // 保存按钮 - 软玻璃主按钮
                SoftSaveButton(
                    onClick = { 
                        viewModel.saveFoodRecord(
                            onSuccess = onNavigateToResult,
                            onError = { error ->
                                // 错误已在UI状态中显示
                            }
                        )
                    },
                    isLoading = uiState.isLoading,
                    retryAttempt = uiState.retryAttempt,
                    maxRetries = uiState.maxRetries,
                    enabled = uiState.foodDescription.isNotBlank() && !uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    
    // 语音输入对话框
    VoiceInputDialog(
        isVisible = showVoiceDialog,
        voiceState = voiceState,
        onDismiss = {
            voiceHelper.stopListening()
            isListening = false
            showVoiceDialog = false
        },
        onStopRecording = {
            voiceHelper.stopListening()
            isListening = false
        },
        showDoneButton = true,
        onDone = {
            voiceHelper.stopListening()
            isListening = false
            showVoiceDialog = false
        }
    )
    
    // 权限说明对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要录音权限") },
            text = { Text("语音输入功能需要录音权限。请在设置中开启权限后重试。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
        }
    }
}

/**
 * 软玻璃输入方式选择器
 */
@Composable
private fun SoftInputMethodSelector(
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceListening: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 拍照按钮 - 软玻璃
        SoftGlassButton(
            onClick = onCameraClick,
            modifier = Modifier.weight(1f),
            icon = { 
                Icon(
                    Icons.Default.CameraAlt, 
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = "拍照识别",
            tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
        
        // 语音按钮 - 软玻璃，录音中状态
        val voiceTint = if (isVoiceListening) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        }
        
        SoftGlassButton(
            onClick = onVoiceClick,
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    if (isVoiceListening) Icons.Default.Mic else Icons.Default.MicNone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isVoiceListening) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        LocalContentColor.current
                    }
                )
            },
            label = if (isVoiceListening) "录音中..." else "语音输入",
            tint = voiceTint,
            contentColor = if (isVoiceListening) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                LocalContentColor.current
            }
        )
    }
}

/**
 * 软玻璃按钮组件
 */
@Composable
private fun SoftGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
    contentColor: Color = LocalContentColor.current
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .height(52.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = tint,
                blurRadius = 25f,
                borderAlpha = 0.3f
            )
            .interactiveScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                icon()
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * 软玻璃餐次选择器
 */
@Composable
private fun SoftMealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                blurRadius = 20f,
                borderAlpha = 0.25f
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            mealTypes.forEach { mealType ->
                val isSelected = selectedMealType == mealType
                val interactionSource = remember { MutableInteractionSource() }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .then(
                            if (isSelected) {
                                Modifier.liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    blurRadius = 15f,
                                    borderAlpha = 0.4f
                                )
                            } else {
                                Modifier
                            }
                        )
                        .interactiveScale(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onMealTypeSelected(mealType) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getMealTypeName(mealType),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * 软玻璃文本输入框
 */
@Composable
private fun SoftTextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp, max = 240.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                blurRadius = 30f,
                borderAlpha = 0.35f
            )
            .padding(16.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        
        // 使用 BasicTextField 实现更自定义的输入体验
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            maxLines = 8,
            decorationBox = { innerTextField ->
                innerTextField()
            }
        )
    }
}

/**
 * 软玻璃示例卡片
 */
@Composable
private fun SoftExampleCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                blurRadius = 25f,
                borderAlpha = 0.25f
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "输入示例",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SoftExampleItem("番茄炒蛋，番茄150g，鸡蛋2个，油10g")
                SoftExampleItem("米饭200g")
                SoftExampleItem("麦当劳巨无霸套餐")
            }
        }
    }
}

/**
 * 示例项
 */
@Composable
private fun SoftExampleItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )
}

/**
 * 软玻璃保存按钮 - 带加载动画和重试状态
 */
@Composable
private fun SoftSaveButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    retryAttempt: Int = 0,
    maxRetries: Int = 2,
    enabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundTint = if (enabled) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = backgroundTint,
                blurRadius = if (enabled) 35f else 20f,
                borderAlpha = if (enabled) 0.5f else 0.2f
            )
            .graphicsLayer {
                alpha = if (enabled) 1f else 0.6f
            }
            .interactiveScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                fadeOut(animationSpec = tween(300))
            },
            label = "loading"
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI分析动画组件
                    AIAnalysisAnimation(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Column {
                        Text(
                            text = if (retryAttempt > 0) "AI分析中 (重试${retryAttempt}/${maxRetries})..." else "AI分析中...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        // 动态提示文字
                        val tips = listOf(
                            "正在识别食物成分...",
                            "分析营养成分中...",
                            "计算热量数据...",
                            "生成营养报告...",
                            "即将完成..."
                        )
                        val tipIndex by rememberInfiniteTransition(label = "tip").animateValue(
                            initialValue = 0,
                            targetValue = tips.size - 1,
                            typeConverter = Int.VectorConverter,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "tip"
                        )
                        
                        Text(
                            text = tips[tipIndex],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                Text(
                    text = "保存记录",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 错误提示卡片
 */
@Composable
private fun ErrorMessageCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * 重试提示卡片
 */
@Composable
private fun RetryMessageCard(
    message: String,
    attempt: Int,
    maxAttempts: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 旋转的加载图标
            val infiniteTransition = rememberInfiniteTransition(label = "retry")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.rotate(rotation)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "尝试 $attempt / $maxAttempts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * AI分析动画组件
 * 包含脉冲波纹、旋转圆点和浮动效果
 */
@Composable
private fun AIAnalysisAnimation(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_analysis")
    
    // 脉冲缩放动画
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // 脉冲透明度动画
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // 旋转动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 浮动动画
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 外层脉冲波纹
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
                .background(
                    color = color.copy(alpha = pulseAlpha),
                    shape = CircleShape
                )
        )
        
        // 中层旋转圆点
        Box(
            modifier = Modifier
                .fillMaxSize(0.7f)
                .rotate(rotation)
        ) {
            // 三个旋转的小圆点
            val dotPositions = listOf(
                Pair(0f, -1f),    // 上
                Pair(0.866f, 0.5f),  // 右下
                Pair(-0.866f, 0.5f)  // 左下
            )
            
            dotPositions.forEachIndexed { index, (x, y) ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 200)
                    ),
                    label = "dot$index"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(
                            x = (x * 12).dp,
                            y = (y * 12).dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp * dotScale)
                            .background(
                                color = color.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
        
        // 内层中心图标（浮动效果）
        Box(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .offset(y = floatOffset.dp)
                .background(
                    color = color,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 开始语音输入
 */
private fun startVoiceInput(
    context: android.content.Context,
    voiceHelper: VoiceInputHelper,
    viewModel: AddFoodViewModel,
    onStart: () -> Unit
) {
    onStart()
    voiceHelper.startListening(
        context = context,
        onResult = { result ->
            // 识别成功，更新食物描述
            viewModel.onFoodDescriptionChange(result)
        },
        onError = { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
        },
        onPartialResult = { partialText ->
            // 实时显示部分识别结果
            viewModel.onFoodDescriptionChange(partialText)
        }
    )
}
