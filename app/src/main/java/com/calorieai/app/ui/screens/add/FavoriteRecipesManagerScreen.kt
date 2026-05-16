package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                title = { Text("管理收藏") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        RecipeScreenContainer(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RecipePanel(
                        title = "筛选与排序",
                        subtitle = "快速定位高频收藏并批量复用"
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = {
                                viewModel.dispatch(RecipeAction.FavoriteLibrary.ChangeQuery(it))
                            },
                            label = { Text("搜索名称或原始输入") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        MealFilterSection(
                            selected = uiState.selectedMealFilter,
                            onSelected = {
                                viewModel.dispatch(RecipeAction.FavoriteLibrary.ChangeMealType(it))
                            }
                        )

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
                }

                item {
                    RecipePanel(
                        title = "快捷加到今日",
                        subtitle = "设置默认餐次，一键复用收藏"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK).forEach { type ->
                                FilterChip(
                                    selected = type == addMealType,
                                    onClick = { addMealType = type },
                                    label = { Text(mealTypeLabel(type)) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RecipeMetricBadge(
                                label = "筛选后",
                                value = uiState.filteredFavorites.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            RecipeMetricBadge(
                                label = "总收藏",
                                value = uiState.favorites.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (uiState.filteredFavorites.isEmpty()) {
                    item {
                        RecipePanel(
                            title = "暂无匹配收藏",
                            subtitle = "调整筛选条件后再试"
                        ) {
                            Text(
                                "当前筛选下没有数据。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.filteredFavorites, key = { it.id }) { recipe ->
                        FavoriteRecipeItem(
                            recipe = recipe,
                            sourceMealType = uiState.sourceMealTypeMap[recipe.id],
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun FavoriteRecipeItem(
    recipe: FavoriteRecipe,
    sourceMealType: MealType?,
    onAddToday: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recipe.foodName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        recipe.userInput.trim().ifBlank { "暂无原始描述" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑做法")
                    }
                    FilledTonalIconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除收藏")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecipeInfoChip(
                        label = sourceMealType?.let(::mealTypeLabel) ?: "未分餐次",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RecipeInfoChip(
                        label = "${recipe.totalCalories} kcal",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecipeInfoChip(
                        label = "复用 ${recipe.useCount} 次",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RecipeInfoChip(
                        label = if (recipe.recipeStepsText.isNullOrBlank()) "未补做法" else "已补做法",
                        color = if (recipe.recipeStepsText.isNullOrBlank()) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        },
                        contentColor = if (recipe.recipeStepsText.isNullOrBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (recipe.recipeDurationMinutes != null) {
                    RecipeInfoChip(
                        label = "${recipe.recipeDurationMinutes} 分钟",
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (!recipe.recipeDifficulty.isNullOrBlank()) {
                    RecipeInfoChip(
                        label = recipe.recipeDifficulty,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!recipe.recipeIngredientsText.isNullOrBlank()) {
                Text(
                    "食材：${recipe.recipeIngredientsText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatFavoriteRecipeTime(recipe),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(onClick = onAddToday) {
                    Text("加入今日")
                }
            }
        }
    }
}

@Composable
private fun RecipeInfoChip(
    label: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides contentColor
                ) {
                    icon()
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                        onValueChange = { duration = it.filter(Char::isDigit) },
                        label = { Text("时长(分钟)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it.filter(Char::isDigit) },
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

private fun formatFavoriteRecipeTime(recipe: FavoriteRecipe): String {
    recipe.lastUsedAt?.let { return "最近使用 ${formatMonthDay(it)}" }
    return "收藏于 ${formatMonthDay(recipe.createdAt)}"
}

private fun formatMonthDay(timestamp: Long): String {
    return SimpleDateFormat("M-d", Locale.getDefault()).format(Date(timestamp))
}
