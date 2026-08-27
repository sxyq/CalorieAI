package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.getMealTypeName
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesScreen(
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onNavigateBack: () -> Unit = {},
    onNavigateToFavoritesManager: () -> Unit = {},
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
        RecipeScreenContainer(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                // Keep the final preference action above the persistent bottom navigation.
                contentPadding = PaddingValues(bottom = 72.dp + bottomContentPadding, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HomeOverviewSection(
                        favoriteCount = uiState.favorites.size,
                        planCount = uiState.recipePlans.size
                    )
                }

                item {
                    QuickFavoriteSection(
                        selectedMealType = uiState.selectedMealType,
                        favorites = uiState.quickFavorites,
                        onMealTypeChange = { mealType ->
                            viewModel.dispatch(RecipeAction.Home.ChangeSelectedMealType(mealType))
                        },
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
                    MealPlanAssistSection(
                        plans = uiState.recipePlans,
                        onManagePlans = onNavigateToMealPlanManager
                    )
                }

            }
        }
    }
}

@Composable
private fun HomeOverviewSection(
    favoriteCount: Int,
    planCount: Int
) {
    RecipePanel(
        title = "收藏资源概览",
        subtitle = "收藏 -> 食材 -> 菜单，形成可执行协同"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecipeMetricBadge("收藏", favoriteCount.toString(), Modifier.weight(1f))
            RecipeMetricBadge("菜单", planCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickFavoriteSection(
    selectedMealType: MealType,
    favorites: List<FavoriteRecipe>,
    onMealTypeChange: (MealType) -> Unit,
    onAdd: (FavoriteRecipe) -> Unit,
    onManageFavorites: () -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    RecipePanel(
        title = "快捷复用",
        subtitle = "按餐次筛选收藏，支持快速复用到今日记录",
        actionText = "管理收藏",
        onAction = onManageFavorites
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            mealTypes.forEach { type ->
                FilterChip(
                    selected = type == selectedMealType,
                    onClick = { onMealTypeChange(type) },
                    label = { Text(getMealTypeName(type)) }
                )
            }
        }

        if (favorites.isEmpty()) {
            Text(
                "暂无可复用收藏，请先在管理收藏中添加菜谱。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            favorites.take(4).forEach { recipe ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = recipeInsetCardColor()
                    ),
                    shape = CardDefaults.shape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.foodName, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${recipe.totalCalories} kcal · 复用 ${recipe.useCount} 次",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onAdd(recipe) }) { Text("加入今日") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanAssistSection(
    plans: List<RecipePlan>,
    onManagePlans: () -> Unit
) {
    val latestPlan = plans.maxByOrNull { it.updatedAt }
    val formatter = remember { DateTimeFormatter.ofPattern("MM-dd") }

    RecipePanel(
        title = "菜单辅助",
        subtitle = "菜单可回显与复用",
        actionText = "管理菜单",
        onAction = onManagePlans
    ) {
        if (latestPlan == null) {
            Text(
                "暂无菜单计划，建议先生成计划后再复用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val start = LocalDate.ofEpochDay(latestPlan.startDateEpochDay).format(formatter)
            val end = LocalDate.ofEpochDay(latestPlan.endDateEpochDay).format(formatter)
            Text(
                "当前方案：${latestPlan.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "覆盖周期：$start - $end",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
