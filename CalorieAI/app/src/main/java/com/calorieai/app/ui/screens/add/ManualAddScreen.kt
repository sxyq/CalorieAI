package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass

/**
 * 手动录入页面 - Liquid Glass 重构版
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddScreen(
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: ManualAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手动录入") },
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 食物名称输入 - 软玻璃
                SoftInputField(
                    value = uiState.foodName,
                    onValueChange = viewModel::updateFoodName,
                    label = "食物名称",
                    placeholder = "例如：红烧肉",
                    icon = Icons.Default.Restaurant,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // 热量输入 - 软玻璃
                SoftInputField(
                    value = uiState.calories,
                    onValueChange = viewModel::updateCalories,
                    label = "热量 (千卡)",
                    placeholder = "例如：350",
                    icon = Icons.Default.LocalFireDepartment,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    suffix = "千卡"
                )

                // 营养成分标题
                Text(
                    text = "营养成分 (克)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 蛋白质、碳水、脂肪 - 软玻璃三列输入
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftNutritionInput(
                        value = uiState.protein,
                        onValueChange = viewModel::updateProtein,
                        label = "蛋白质",
                        modifier = Modifier.weight(1f)
                    )
                    SoftNutritionInput(
                        value = uiState.carbs,
                        onValueChange = viewModel::updateCarbs,
                        label = "碳水",
                        modifier = Modifier.weight(1f)
                    )
                    SoftNutritionInput(
                        value = uiState.fat,
                        onValueChange = viewModel::updateFat,
                        label = "脂肪",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 餐次选择 - 软玻璃
                Text(
                    text = "餐次",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                SoftMealTypeSelector(
                    selectedMealType = uiState.mealType,
                    onMealTypeSelected = viewModel::updateMealType
                )

                // 备注输入 - 软玻璃多行
                SoftMultilineInputField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = "备注 (可选)",
                    placeholder = "添加备注信息...",
                    icon = Icons.Default.Edit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 保存按钮 - 软玻璃
                SoftSaveButton(
                    onClick = {
                        viewModel.saveRecord()
                        onSaveComplete()
                    },
                    enabled = uiState.foodName.isNotBlank() && uiState.calories.isNotBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
}

/**
 * 软玻璃输入框
 */
@Composable
private fun SoftInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    suffix: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                blurRadius = 25f,
                borderAlpha = 0.3f
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )

                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = keyboardOptions,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )

                if (suffix != null) {
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 软玻璃营养成分输入
 */
@Composable
private fun SoftNutritionInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                blurRadius = 20f,
                borderAlpha = 0.25f
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )

            Text(
                text = "g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
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
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
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
                        .height(48.dp)
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
 * 软玻璃多行输入框
 */
@Composable
private fun SoftMultilineInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                blurRadius = 25f,
                borderAlpha = 0.3f
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 2,
                maxLines = 4,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

/**
 * 软玻璃保存按钮
 */
@Composable
private fun SoftSaveButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

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
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
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
