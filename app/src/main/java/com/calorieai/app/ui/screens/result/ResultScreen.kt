package com.calorieai.app.ui.screens.result

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.components.liquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    recordId: String,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    LaunchedEffect(uiState.favoriteMessage) {
        uiState.favoriteMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFavoriteMessage()
        }
    }
    LaunchedEffect(uiState.regenerateMessage) {
        uiState.regenerateMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRegenerateMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("记录详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                    isFavoritedRecipe = uiState.isFavoritedRecipe,
                    onSave = { updatedRecord ->
                        viewModel.updateRecord(updatedRecord)
                        onNavigateBack()
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavoriteRecipe()
                    },
                    onRegenerate = {
                        viewModel.regenerateCurrentRecord()
                    },
                    isRegenerating = uiState.isRegenerating,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ResultContent(
    record: FoodRecord,
    isFavoritedRecipe: Boolean,
    onSave: (FoodRecord) -> Unit,
    onToggleFavorite: () -> Unit,
    onRegenerate: () -> Unit = {},
    isRegenerating: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 基础营养素状态
    var calories by remember { mutableStateOf(record.totalCalories.toString()) }
    var protein by remember { mutableStateOf(record.protein.toString()) }
    var carbs by remember { mutableStateOf(record.carbs.toString()) }
    var fat by remember { mutableStateOf(record.fat.toString()) }

    // 扩展营养素状态
    var fiber by remember { mutableStateOf(record.fiber.toString()) }
    var sugar by remember { mutableStateOf(record.sugar.toString()) }
    var sodium by remember { mutableStateOf(record.sodium.toString()) }
    var cholesterol by remember { mutableStateOf(record.cholesterol.toString()) }
    var saturatedFat by remember { mutableStateOf(record.saturatedFat.toString()) }
    var calcium by remember { mutableStateOf(record.calcium.toString()) }
    var iron by remember { mutableStateOf(record.iron.toString()) }
    var vitaminC by remember { mutableStateOf(record.vitaminC.toString()) }
    var vitaminA by remember { mutableStateOf(record.vitaminA.toString()) }
    var potassium by remember { mutableStateOf(record.potassium.toString()) }

    LaunchedEffect(record) {
        calories = record.totalCalories.toString()
        protein = record.protein.toString()
        carbs = record.carbs.toString()
        fat = record.fat.toString()
        fiber = record.fiber.toString()
        sugar = record.sugar.toString()
        sodium = record.sodium.toString()
        cholesterol = record.cholesterol.toString()
        saturatedFat = record.saturatedFat.toString()
        calcium = record.calcium.toString()
        iron = record.iron.toString()
        vitaminC = record.vitaminC.toString()
        vitaminA = record.vitaminA.toString()
        potassium = record.potassium.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 食物名称卡片 - 大标题样式
        FoodNameCard(foodName = record.foodName)

        // 热量主卡片 - 突出显示
        CaloriesCard(
            calories = calories,
            onCaloriesChange = { calories = it }
        )

        // 三大营养素卡片 - 使用进度条样式
        MacronutrientsCard(
            protein = protein,
            carbs = carbs,
            fat = fat,
            onProteinChange = { protein = it },
            onCarbsChange = { carbs = it },
            onFatChange = { fat = it }
        )

        // 扩展营养素卡片 - 网格布局
        ExtendedNutrientsCard(
            fiber = fiber,
            sugar = sugar,
            sodium = sodium,
            cholesterol = cholesterol,
            saturatedFat = saturatedFat,
            calcium = calcium,
            iron = iron,
            vitaminC = vitaminC,
            vitaminA = vitaminA,
            potassium = potassium,
            onFiberChange = { fiber = it },
            onSugarChange = { sugar = it },
            onSodiumChange = { sodium = it },
            onCholesterolChange = { cholesterol = it },
            onSaturatedFatChange = { saturatedFat = it },
            onCalciumChange = { calcium = it },
            onIronChange = { iron = it },
            onVitaminCChange = { vitaminC = it },
            onVitaminAChange = { vitaminA = it },
            onPotassiumChange = { potassium = it }
        )

        // 原始输入信息
        OriginalInputCard(userInput = record.userInput)

        Spacer(modifier = Modifier.height(24.dp))

        // 保存按钮
        SaveButton(
            onClick = {
                val updatedRecord = record.copy(
                    totalCalories = calories.toIntOrNull() ?: 0,
                    protein = protein.toFloatOrNull() ?: 0f,
                    carbs = carbs.toFloatOrNull() ?: 0f,
                    fat = fat.toFloatOrNull() ?: 0f,
                    fiber = fiber.toFloatOrNull() ?: 0f,
                    sugar = sugar.toFloatOrNull() ?: 0f,
                    sodium = sodium.toFloatOrNull() ?: 0f,
                    cholesterol = cholesterol.toFloatOrNull() ?: 0f,
                    saturatedFat = saturatedFat.toFloatOrNull() ?: 0f,
                    calcium = calcium.toFloatOrNull() ?: 0f,
                    iron = iron.toFloatOrNull() ?: 0f,
                    vitaminC = vitaminC.toFloatOrNull() ?: 0f,
                    vitaminA = vitaminA.toFloatOrNull() ?: 0f,
                    potassium = potassium.toFloatOrNull() ?: 0f
                )
                onSave(updatedRecord)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FavoriteRecipeButton(
            isFavorited = isFavoritedRecipe,
            onClick = onToggleFavorite
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 重新生成数据按钮
        RegenerateButton(onClick = onRegenerate, isLoading = isRegenerating)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FavoriteRecipeButton(
    isFavorited: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isFavorited) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                }
            )
            .interactiveScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isFavorited) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFavorited) "已收藏菜谱（点击取消）" else "收藏菜谱",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 食物名称卡片
 */
@Composable
private fun FoodNameCard(foodName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = foodName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 热量主卡片
 */
@Composable
private fun CaloriesCard(
    calories: String,
    onCaloriesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "总热量",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${calories.toIntOrNull() ?: 0} 千卡",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 编辑按钮
            OutlinedTextField(
                value = calories,
                onValueChange = onCaloriesChange,
                label = { Text("修改") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
            )
        }
    }
}

/**
 * 三大营养素卡片
 */
@Composable
private fun MacronutrientsCard(
    protein: String,
    carbs: String,
    fat: String,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "三大营养素",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "蛋白质",
                    value = protein,
                    unit = "g",
                    color = Color(0xFF4CAF50),
                    onValueChange = onProteinChange,
                    modifier = Modifier.weight(1f)
                )
                MacroItem(
                    icon = Icons.Default.Grain,
                    label = "碳水",
                    value = carbs,
                    unit = "g",
                    color = Color(0xFFFF9800),
                    onValueChange = onCarbsChange,
                    modifier = Modifier.weight(1f)
                )
                MacroItem(
                    icon = Icons.Default.WaterDrop,
                    label = "脂肪",
                    value = fat,
                    unit = "g",
                    color = Color(0xFFFFC107),
                    onValueChange = onFatChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 单个营养素项
 */
@Composable
private fun MacroItem(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            ),
            suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) }
        )
    }
}

/**
 * 扩展营养素卡片
 */
@Composable
private fun ExtendedNutrientsCard(
    fiber: String, sugar: String, sodium: String, cholesterol: String, saturatedFat: String,
    calcium: String, iron: String, vitaminC: String, vitaminA: String, potassium: String,
    onFiberChange: (String) -> Unit, onSugarChange: (String) -> Unit,
    onSodiumChange: (String) -> Unit, onCholesterolChange: (String) -> Unit,
    onSaturatedFatChange: (String) -> Unit, onCalciumChange: (String) -> Unit,
    onIronChange: (String) -> Unit, onVitaminCChange: (String) -> Unit,
    onVitaminAChange: (String) -> Unit, onPotassiumChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "详细营养成分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 使用网格布局展示所有营养素
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 膳食纤维和糖
                NutrientRow {
                    NutrientInput(
                        label = "膳食纤维",
                        value = fiber,
                        unit = "g",
                        emoji = "🌾",
                        onValueChange = onFiberChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "糖",
                        value = sugar,
                        unit = "g",
                        emoji = "🍯",
                        onValueChange = onSugarChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "饱和脂肪",
                        value = saturatedFat,
                        unit = "g",
                        emoji = "🧈",
                        onValueChange = onSaturatedFatChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 钠和胆固醇
                NutrientRow {
                    NutrientInput(
                        label = "钠",
                        value = sodium,
                        unit = "mg",
                        emoji = "🧂",
                        onValueChange = onSodiumChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "胆固醇",
                        value = cholesterol,
                        unit = "mg",
                        emoji = "🥚",
                        onValueChange = onCholesterolChange,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 矿物质
                NutrientRow {
                    NutrientInput(
                        label = "钙",
                        value = calcium,
                        unit = "mg",
                        emoji = "🥛",
                        onValueChange = onCalciumChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "铁",
                        value = iron,
                        unit = "mg",
                        emoji = "🥩",
                        onValueChange = onIronChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "钾",
                        value = potassium,
                        unit = "mg",
                        emoji = "🍌",
                        onValueChange = onPotassiumChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 维生素
                NutrientRow {
                    NutrientInput(
                        label = "维生素A",
                        value = vitaminA,
                        unit = "μg",
                        emoji = "🥕",
                        onValueChange = onVitaminAChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInput(
                        label = "维生素C",
                        value = vitaminC,
                        unit = "mg",
                        emoji = "🍊",
                        onValueChange = onVitaminCChange,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 营养素行
 */
@Composable
private fun NutrientRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/**
 * 营养素输入项
 */
@Composable
private fun NutrientInput(
    label: String,
    value: String,
    unit: String,
    emoji: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center
            ),
            suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) }
        )
    }
}

/**
 * 原始输入卡片
 */
@Composable
private fun OriginalInputCard(userInput: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "原始输入",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = userInput,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 保存按钮
 */
@Composable
private fun SaveButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .liquidGlass(
                shape = RoundedCornerShape(28.dp),
                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                blurRadius = 30f
            )
            .interactiveScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "保存修改",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 重新生成数据按钮
 */
@Composable
private fun RegenerateButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .interactiveScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重新生成中...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重新生成数据",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
