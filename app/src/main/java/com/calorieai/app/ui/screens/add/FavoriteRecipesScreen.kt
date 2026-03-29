package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFavoritesManager: () -> Unit = {},
    onNavigateToPantryManager: () -> Unit = {},
    onNavigateToMealPlanManager: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: RecipeHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RecipeUiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱中心") },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OverviewCard(
                    favoriteCount = uiState.favorites.size,
                    pantryCount = uiState.pantryIngredients.size,
                    planCount = uiState.recipePlans.size
                )
            }

            item {
                QuickFavoriteCard(
                    selectedMealType = uiState.selectedMealType,
                    favorites = uiState.quickFavorites,
                    onMealTypeChange = viewModel::setSelectedMealType,
                    onAdd = { recipe ->
                        viewModel.dispatch(
                            RecipeAction.Home.AddFavoriteToToday(
                                recipe = recipe,
                                mealType = uiState.selectedMealType
                            )
                        )
                    },
                    onManageFavorites = onNavigateToFavoritesManager
                )
            }

            item {
                AICard(
                    isLoading = uiState.isAiLoading,
                    result = uiState.aiResult,
                    error = uiState.aiError,
                    onGenerateSuggestion = {
                        viewModel.dispatch(RecipeAction.Home.GenerateSuggestion)
                    },
                    onGeneratePlan = {
                        viewModel.dispatch(RecipeAction.Home.GeneratePlan(days = 3))
                    },
                    onClear = viewModel::clearAiResult
                )
            }

            item {
                PersonalizationCard(
                    uiState = uiState,
                    onDietaryAllergensChange = viewModel::onDietaryAllergensChange,
                    onFlavorPreferencesChange = viewModel::onFlavorPreferencesChange,
                    onBudgetPreferenceChange = viewModel::onBudgetPreferenceChange,
                    onMaxCookingMinutesChange = viewModel::onMaxCookingMinutesChange,
                    onSpecialPopulationModeChange = viewModel::onSpecialPopulationModeChange,
                    onWeeklyRecordGoalDaysChange = viewModel::onWeeklyRecordGoalDaysChange,
                    onSave = { viewModel.dispatch(RecipeAction.Home.SavePersonalization) }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onNavigateToPantryManager,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("食材管理")
                    }
                    Button(
                        onClick = onNavigateToMealPlanManager,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("菜单计划")
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    favoriteCount: Int,
    pantryCount: Int,
    planCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("收藏复用闭环", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "收藏 -> 一键加入今日 -> 按库存与偏好联动 AI 菜单",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("收藏", favoriteCount, Modifier.weight(1f))
                StatPill("食材", pantryCount, Modifier.weight(1f))
                StatPill("菜单", planCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickFavoriteCard(
    selectedMealType: MealType,
    favorites: List<FavoriteRecipe>,
    onMealTypeChange: (MealType) -> Unit,
    onAdd: (FavoriteRecipe) -> Unit,
    onManageFavorites: () -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("快捷复用收藏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onManageFavorites) { Text("进入收藏库") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealTypes.forEach { type ->
                    FilterChip(
                        selected = type == selectedMealType,
                        onClick = { onMealTypeChange(type) },
                        label = { Text(mealTypeLabel(type)) }
                    )
                }
            }
            if (favorites.isEmpty()) {
                Text("暂无收藏菜谱，先在结果页或收藏库添加。", style = MaterialTheme.typography.bodySmall)
            } else {
                favorites.forEach { recipe ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recipe.foodName, fontWeight = FontWeight.Medium)
                                Text(
                                    "${recipe.totalCalories} kcal · 使用 ${recipe.useCount} 次",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { onAdd(recipe) }) {
                                Text("加入今日")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AICard(
    isLoading: Boolean,
    result: String?,
    error: String?,
    onGenerateSuggestion: () -> Unit,
    onGeneratePlan: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI 菜谱协同", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGenerateSuggestion,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) { Text("生成推荐") }
                Button(
                    onClick = onGeneratePlan,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) { Text("生成3天菜单") }
            }
            if (isLoading) {
                CircularProgressIndicator()
            }
            if (!error.isNullOrBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (!result.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        MarkdownText(
                            text = result,
                            config = MarkdownConfig.ChatReadable
                        )
                    }
                }
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("清空")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalizationCard(
    uiState: RecipeHomeUiState,
    onDietaryAllergensChange: (String) -> Unit,
    onFlavorPreferencesChange: (String) -> Unit,
    onBudgetPreferenceChange: (String) -> Unit,
    onMaxCookingMinutesChange: (String) -> Unit,
    onSpecialPopulationModeChange: (String) -> Unit,
    onWeeklyRecordGoalDaysChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val modeOptions = listOf(
        "GENERAL" to "通用健康",
        "DIABETES" to "控糖",
        "GOUT" to "痛风",
        "PREGNANCY" to "孕期",
        "CHILD" to "儿童",
        "FITNESS" to "健身"
    )
    var modeExpanded by remember { mutableStateOf(false) }
    val selectedLabel = modeOptions.firstOrNull { it.first == uiState.specialPopulationMode }?.second ?: "通用健康"

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f))) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("个性化偏好", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = uiState.dietaryAllergens,
                onValueChange = onDietaryAllergensChange,
                label = { Text("过敏原/忌口") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.flavorPreferences,
                onValueChange = onFlavorPreferencesChange,
                label = { Text("口味偏好") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.budgetPreference,
                onValueChange = onBudgetPreferenceChange,
                label = { Text("预算偏好") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.maxCookingMinutes,
                    onValueChange = onMaxCookingMinutesChange,
                    label = { Text("烹饪时长(分钟)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.weeklyRecordGoalDays,
                    onValueChange = onWeeklyRecordGoalDaysChange,
                    label = { Text("每周记录目标") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = !modeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("特定人群模式") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
            Button(onClick = onSave, modifier = Modifier.align(Alignment.End)) {
                Text("保存")
            }
        }
    }
}

private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> "早餐"
    MealType.LUNCH -> "午餐"
    MealType.DINNER -> "晚餐"
    MealType.SNACK -> "加餐"
    MealType.BREAKFAST_SNACK -> "早加餐"
    MealType.LUNCH_SNACK -> "午加餐"
    MealType.DINNER_SNACK -> "晚加餐"
}
