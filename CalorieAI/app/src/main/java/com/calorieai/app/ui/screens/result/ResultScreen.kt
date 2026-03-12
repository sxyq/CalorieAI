package com.calorieai.app.ui.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.FoodRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    recordId: String,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.record == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("记录不存在")
                }
            }
            else -> {
                ResultContent(
                    record = uiState.record!!,
                    onSave = { updatedRecord ->
                        viewModel.updateRecord(updatedRecord)
                        onNavigateBack()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ResultContent(
    record: FoodRecord,
    onSave: (FoodRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var calories by remember { mutableStateOf(record.totalCalories.toString()) }
    var protein by remember { mutableStateOf(record.protein.toString()) }
    var carbs by remember { mutableStateOf(record.carbs.toString()) }
    var fat by remember { mutableStateOf(record.fat.toString()) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 食物名称
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = record.foodName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "请手动输入热量数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 热量输入
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("热量 (千卡)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // 营养成分输入
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "营养成分 (克)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionInputField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = "蛋白质",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionInputField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = "碳水",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionInputField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = "脂肪",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // 原始输入
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "原始输入",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.userInput,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 保存按钮
        Button(
            onClick = {
                val updatedRecord = record.copy(
                    totalCalories = calories.toIntOrNull() ?: 0,
                    protein = protein.toFloatOrNull() ?: 0f,
                    carbs = carbs.toFloatOrNull() ?: 0f,
                    fat = fat.toFloatOrNull() ?: 0f
                )
                onSave(updatedRecord)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存记录")
        }
    }
}

@Composable
fun NutritionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        modifier = modifier,
        singleLine = true
    )
}
