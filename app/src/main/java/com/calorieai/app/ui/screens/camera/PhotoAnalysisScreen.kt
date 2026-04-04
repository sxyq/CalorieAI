package com.calorieai.app.ui.screens.camera

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import com.calorieai.app.ui.components.liquidGlass
import coil.compose.rememberAsyncImagePainter
import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoAnalysisScreen(
    photoUri: Uri,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: PhotoAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(photoUri) {
        viewModel.analyzePhoto(photoUri, context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照识别") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isAnalyzing -> {
                    AnalyzingContent(retryMessage = uiState.retryMessage)
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.analyzePhoto(photoUri, context) },
                        onBack = onNavigateBack
                    )
                }
                uiState.analysisResult != null -> {
                    AnalysisResultContent(
                        photoUri = photoUri,
                        result = uiState.analysisResult!!,
                        userHint = uiState.userHint,
                        onUserHintChange = viewModel::onUserHintChange,
                        onReanalyze = { viewModel.reanalyze(context) },
                        onSave = { viewModel.saveRecord(onSaveComplete) },
                        onEdit = viewModel::enableEditMode,
                        isEditMode = uiState.isEditMode,
                        editedResult = uiState.editedResult,
                        onEditResult = viewModel::updateEditedResult,
                        selectedMealType = uiState.selectedMealType,
                        onMealTypeChange = viewModel::onMealTypeChange
                    )
                }
            }
        }
    }

}

@Composable
private fun AnalyzingContent(retryMessage: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI正在分析图片...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请稍候，正在识别食物并计算营养成分",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = retryMessage ?: "优先执行单次识别，必要时自动补救一次",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "分析失败",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("返回")
            }
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalysisResultContent(
    photoUri: Uri,
    result: FoodAnalysisResult,
    userHint: String,
    onUserHintChange: (String) -> Unit,
    onReanalyze: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    isEditMode: Boolean,
    editedResult: FoodAnalysisResult?,
    onEditResult: (FoodAnalysisResult) -> Unit,
    selectedMealType: MealType,
    onMealTypeChange: (MealType) -> Unit
) {
    val displayResult = editedResult ?: result
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 图片预览
        Box(
            modifier = Modifier.fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(12.dp),
                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "拍摄的食物照片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户提示输入
        OutlinedTextField(
            value = userHint,
            onValueChange = onUserHintChange,
            label = { Text("补充描述（可选）") },
            placeholder = { Text("例如：这是外卖的宫保鸡丁，米饭大约吃了一多半") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            trailingIcon = {
                if (userHint.isNotBlank()) {
                    IconButton(onClick = onReanalyze) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新分析")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 分析结果卡片
        Box(
            modifier = Modifier.fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(16.dp),
                    tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "识别结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isEditMode) {
                    EditableResultFields(
                        result = displayResult,
                        onResultChange = onEditResult
                    )
                } else {
                    ResultDisplayFields(result = displayResult)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        MealTypeSelectorCard(
            selectedMealType = selectedMealType,
            onMealTypeChange = onMealTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 保存按钮
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = displayResult.foodName.isNotBlank()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存记录")
        }
    }
}

@Composable
private fun MealTypeSelectorCard(
    selectedMealType: MealType,
    onMealTypeChange: (MealType) -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "餐次",
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mealTypes.forEach { type ->
                FilterChip(
                    selected = type == selectedMealType,
                    onClick = { onMealTypeChange(type) },
                    label = { Text(getMealTypeName(type)) }
                )
            }
        }
    }
}

@Composable
private fun ResultDisplayFields(result: FoodAnalysisResult) {
    Column {
        ResultRow(label = "食物名称", value = result.foodName)
        ResultRow(label = "估计重量", value = "${result.estimatedWeight}g")
        ResultRow(label = "热量", value = "${result.calories}千卡", isHighlight = true)
        ResultRow(label = "蛋白质", value = "${result.protein}g")
        ResultRow(label = "碳水化合物", value = "${result.carbs}g")
        ResultRow(label = "脂肪", value = "${result.fat}g")
        if (result.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "描述：${result.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditableResultFields(
    result: FoodAnalysisResult,
    onResultChange: (FoodAnalysisResult) -> Unit
) {
    Column {
        OutlinedTextField(
            value = result.foodName,
            onValueChange = { onResultChange(result.copy(foodName = it)) },
            label = { Text("食物名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = result.estimatedWeight.toString(),
                onValueChange = {
                    val weight = it.toIntOrNull() ?: 0
                    onResultChange(result.copy(estimatedWeight = weight))
                },
                label = { Text("重量(g)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = result.calories.toString(),
                onValueChange = {
                    val calories = it.toFloatOrNull() ?: 0f
                    onResultChange(result.copy(calories = calories))
                },
                label = { Text("热量") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = result.protein.toString(),
                onValueChange = {
                    val protein = it.toFloatOrNull() ?: 0f
                    onResultChange(result.copy(protein = protein))
                },
                label = { Text("蛋白质(g)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = result.carbs.toString(),
                onValueChange = {
                    val carbs = it.toFloatOrNull() ?: 0f
                    onResultChange(result.copy(carbs = carbs))
                },
                label = { Text("碳水(g)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = result.fat.toString(),
                onValueChange = {
                    val fat = it.toFloatOrNull() ?: 0f
                    onResultChange(result.copy(fat = fat))
                },
                label = { Text("脂肪(g)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = result.description,
            onValueChange = { onResultChange(result.copy(description = it)) },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlight) {
                MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )
    }
}
