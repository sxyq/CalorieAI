package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName

/**
 * 手动录入页面
 * 用户手动输入食物名称、热量和营养成分
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
                .verticalScroll(rememberScrollState())
        ) {
            // 食物名称输入
            OutlinedTextField(
                value = uiState.foodName,
                onValueChange = viewModel::updateFoodName,
                label = { Text("食物名称") },
                placeholder = { Text("例如：红烧肉") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 热量输入
            OutlinedTextField(
                value = uiState.calories,
                onValueChange = viewModel::updateCalories,
                label = { Text("热量 (千卡)") },
                placeholder = { Text("例如：350") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 营养成分标题
            Text(
                text = "营养成分 (克)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 蛋白质、碳水、脂肪
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NutritionInput(
                    value = uiState.protein,
                    onValueChange = viewModel::updateProtein,
                    label = "蛋白质",
                    modifier = Modifier.weight(1f)
                )
                NutritionInput(
                    value = uiState.carbs,
                    onValueChange = viewModel::updateCarbs,
                    label = "碳水",
                    modifier = Modifier.weight(1f)
                )
                NutritionInput(
                    value = uiState.fat,
                    onValueChange = viewModel::updateFat,
                    label = "脂肪",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 餐次选择
            Text(
                text = "餐次",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ManualMealTypeSelector(
                selectedMealType = uiState.mealType,
                onMealTypeSelected = viewModel::updateMealType
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 备注输入
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("备注 (可选)") },
                placeholder = { Text("添加备注信息...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveRecord()
                    onSaveComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = uiState.foodName.isNotBlank() && uiState.calories.isNotBlank()
            ) {
                Text(
                    text = "保存记录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 营养成分输入框
 */
@Composable
private fun NutritionInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        )
    )
}

/**
 * 餐次选择器（手动录入页面用）
 */
@Composable
private fun ManualMealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealType.values().forEach { mealType ->
            val isSelected = mealType == selectedMealType
            FilterChip(
                selected = isSelected,
                onClick = { onMealTypeSelected(mealType) },
                label = { Text(getMealTypeName(mealType)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
