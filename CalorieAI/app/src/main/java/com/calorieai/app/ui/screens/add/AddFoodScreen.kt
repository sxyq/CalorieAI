package com.calorieai.app.ui.screens.add

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName
import com.calorieai.app.service.voice.VoiceInputHelper
import com.calorieai.app.ui.components.VoiceInputDialog
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: AddFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 语音输入状态
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // 语音输入帮助类
    val voiceHelper = remember { VoiceInputHelper() }
    val voiceState by voiceHelper.voiceState.collectAsState()
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，开始录音
            startVoiceInput(context, voiceHelper, onStart = {
                isListening = true
                showVoiceDialog = true
            })
        } else {
            // 权限被拒绝，显示说明对话框
            showPermissionDialog = true
        }
    }
    
    // 监听语音状态
    LaunchedEffect(voiceState) {
        when (val state = voiceState) {
            is com.calorieai.app.service.voice.VoiceState.Success -> {
                // 识别成功，更新文本
                viewModel.onFoodDescriptionChange(
                    if (uiState.foodDescription.isBlank()) state.text 
                    else "${uiState.foodDescription} ${state.text}"
                )
                // 关闭对话框
                showVoiceDialog = false
                isListening = false
            }
            is com.calorieai.app.service.voice.VoiceState.Error -> {
                // 错误发生，保持对话框显示错误信息
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 输入方式选择
            InputMethodSelector(
                onCameraClick = onNavigateToCamera,
                onVoiceClick = {
                    when {
                        isListening -> {
                            voiceHelper.stopListening()
                            isListening = false
                            showVoiceDialog = false
                        }
                        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                            startVoiceInput(context, voiceHelper, onStart = {
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
            
            // 餐次选择
            MealTypeSelector(
                selectedMealType = uiState.selectedMealType,
                onMealTypeSelected = viewModel::onMealTypeChange
            )
            
            // 食物描述输入
            OutlinedTextField(
                value = uiState.foodDescription,
                onValueChange = viewModel::onFoodDescriptionChange,
                label = { Text("描述你吃的食物") },
                placeholder = { Text("例如：番茄炒蛋，番茄150g，鸡蛋2个，油10g") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                maxLines = 10
            )
            
            // 输入示例
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "你这顿又吃了什么？😋",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 番茄炒蛋，番茄150g，鸡蛋2个，油10g\n• 米饭200g\n• 麦当劳巨无霸套餐",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 保存按钮
            Button(
                onClick = { viewModel.saveFoodRecord(onNavigateToResult) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.foodDescription.isNotBlank() && !uiState.isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("保存记录")
                }
            }
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
                        // 打开应用设置
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
    
    // 清理
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
        }
    }
}

/**
 * 开始语音输入
 */
private fun startVoiceInput(
    context: android.content.Context,
    voiceHelper: VoiceInputHelper,
    onStart: () -> Unit
) {
    onStart()
    voiceHelper.startListening(
        context = context,
        onResult = { _ ->
            // 结果通过StateFlow传递
        },
        onError = { _ ->
            // 错误通过StateFlow传递
        }
    )
}

@Composable
fun InputMethodSelector(
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceListening: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 拍照按钮
        OutlinedButton(
            onClick = onCameraClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("拍照识别")
        }
        
        // 语音按钮
        OutlinedButton(
            onClick = onVoiceClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = if (isVoiceListening) {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Icon(
                if (isVoiceListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isVoiceListening) "录音中..." else "语音输入")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        mealTypes.forEachIndexed { index, mealType ->
            SegmentedButton(
                selected = selectedMealType == mealType,
                onClick = { onMealTypeSelected(mealType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = mealTypes.size
                )
            ) {
                Text(getMealTypeName(mealType))
            }
        }
    }
}
