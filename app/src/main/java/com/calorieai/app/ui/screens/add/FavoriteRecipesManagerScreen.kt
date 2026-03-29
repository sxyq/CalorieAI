package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: FavoriteLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var detailTarget by remember { mutableStateOf<FavoriteRecipe?>(null) }
    var addMealType by rememberSaveable { mutableStateOf(MealType.LUNCH) }
    var sortExpanded by remember { mutableStateOf(false) }

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
                title = { Text("收藏库管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = {
                        viewModel.dispatch(RecipeAction.FavoriteLibrary.ChangeQuery(it))
                    },
                    label = { Text("搜索收藏（名称/原始输入）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                MealFilterSection(
                    selected = uiState.selectedMealFilter,
                    onSelected = {
                        viewModel.dispatch(RecipeAction.FavoriteLibrary.ChangeMealType(it))
                    }
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = !sortExpanded }
                ) {
                    OutlinedTextField(
                        value = when (uiState.sortType) {
                            FavoriteSortType.LAST_USED -> "最近使用"
                            FavoriteSortType.USE_COUNT -> "使用次数"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("排序方式") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("最近使用") },
                            onClick = {
                                viewModel.dispatch(
                                    RecipeAction.FavoriteLibrary.ChangeSort(FavoriteSortType.LAST_USED)
                                )
                                sortExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("使用次数") },
                            onClick = {
                                viewModel.dispatch(
                                    RecipeAction.FavoriteLibrary.ChangeSort(FavoriteSortType.USE_COUNT)
                                )
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("加入今日时餐次：", style = MaterialTheme.typography.bodyMedium)
                    listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK).forEach { type ->
                        FilterChip(
                            selected = type == addMealType,
                            onClick = { addMealType = type },
                            label = { Text(mealTypeLabel(type)) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = "筛选后 ${uiState.filteredFavorites.size} 条 / 总计 ${uiState.favorites.size} 条",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.filteredFavorites.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Text(
                            "当前筛选条件下暂无收藏。",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(uiState.filteredFavorites, key = { it.id }) { recipe ->
                    FavoriteRecipeItem(
                        recipe = recipe,
                        onAddToday = {
                            viewModel.dispatch(
                                RecipeAction.FavoriteLibrary.AddToToday(
                                    recipe = recipe,
                                    mealType = addMealType
                                )
                            )
                        },
                        onEdit = { detailTarget = recipe },
                        onDelete = {
                            viewModel.dispatch(RecipeAction.FavoriteLibrary.DeleteFavorite(recipe))
                        }
                    )
                }
            }
        }
    }

    detailTarget?.let { target ->
        RecipeDetailDialog(
            recipe = target,
            onDismiss = { detailTarget = null },
            onSave = { ingredients, steps, tools, difficulty, duration, servings ->
                viewModel.dispatch(
                    RecipeAction.FavoriteLibrary.UpdateRecipeDetails(
                        recipe = target,
                        ingredients = ingredients,
                        steps = steps,
                        tools = tools,
                        difficulty = difficulty,
                        durationMinutes = duration,
                        servings = servings
                    )
                )
                detailTarget = null
            }
        )
    }
}

@Composable
private fun MealFilterSection(
    selected: FavoriteFilterMealType,
    onSelected: (FavoriteFilterMealType) -> Unit
) {
    val options = listOf(
        FavoriteFilterMealType.ALL to "全部",
        FavoriteFilterMealType.BREAKFAST to "早餐",
        FavoriteFilterMealType.LUNCH to "午餐",
        FavoriteFilterMealType.DINNER to "晚餐",
        FavoriteFilterMealType.SNACK to "加餐"
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (type, label) ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelected(type) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteRecipeItem(
    recipe: FavoriteRecipe,
    onAddToday: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(recipe.foodName, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑做法")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除收藏")
                }
            }
            Text(
                "${recipe.totalCalories} kcal · 使用 ${recipe.useCount} 次",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!recipe.recipeStepsText.isNullOrBlank()) {
                Text(
                    "做法已维护",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAddToday) { Text("加入今日") }
            }
        }
    }
}

@Composable
private fun RecipeDetailDialog(
    recipe: FavoriteRecipe,
    onDismiss: () -> Unit,
    onSave: (
        ingredients: String?,
        steps: String?,
        tools: String?,
        difficulty: String?,
        durationMinutes: Int?,
        servings: Int?
    ) -> Unit
) {
    var ingredients by rememberSaveable(recipe.id) { mutableStateOf(recipe.recipeIngredientsText.orEmpty()) }
    var steps by rememberSaveable(recipe.id) { mutableStateOf(recipe.recipeStepsText.orEmpty()) }
    var tools by rememberSaveable(recipe.id) { mutableStateOf(recipe.recipeToolsText.orEmpty()) }
    var difficulty by rememberSaveable(recipe.id) { mutableStateOf(recipe.recipeDifficulty.orEmpty()) }
    var duration by rememberSaveable(recipe.id) {
        mutableStateOf(recipe.recipeDurationMinutes?.toString().orEmpty())
    }
    var servings by rememberSaveable(recipe.id) {
        mutableStateOf(recipe.recipeServings?.toString().orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑做法详情") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("食材与克数") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("步骤") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tools,
                    onValueChange = { tools = it },
                    label = { Text("厨具") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("难度") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it.filter { ch -> ch.isDigit() } },
                        label = { Text("时长(分钟)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it.filter { ch -> ch.isDigit() } },
                        label = { Text("份量") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ingredients.ifBlank { null },
                        steps.ifBlank { null },
                        tools.ifBlank { null },
                        difficulty.ifBlank { null },
                        duration.toIntOrNull(),
                        servings.toIntOrNull()
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
