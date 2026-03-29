package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import com.calorieai.app.domain.recipe.FavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteLibraryViewModel @Inject constructor(
    private val favoriteUseCase: FavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteLibraryUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeUiEvent>()
    val events: SharedFlow<RecipeUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            favoriteUseCase.observeFavorites().collectLatest { favorites ->
                val sourceMealTypeMap = favoriteUseCase.getSourceMealTypeMap(favorites)
                _uiState.update { old ->
                    val next = old.copy(
                        favorites = favorites,
                        sourceMealTypeMap = sourceMealTypeMap
                    )
                    next.copy(filteredFavorites = next.filterAndSort())
                }
            }
        }
    }

    fun dispatch(action: RecipeAction.FavoriteLibrary) {
        when (action) {
            is RecipeAction.FavoriteLibrary.ChangeMealType -> changeMealType(action.mealType)
            is RecipeAction.FavoriteLibrary.ChangeQuery -> changeQuery(action.query)
            is RecipeAction.FavoriteLibrary.ChangeSort -> changeSort(action.sort)
            is RecipeAction.FavoriteLibrary.AddToToday -> addToToday(action.recipe, action.mealType)
            is RecipeAction.FavoriteLibrary.DeleteFavorite -> deleteFavorite(action.recipe)
            is RecipeAction.FavoriteLibrary.UpdateRecipeDetails -> updateRecipeDetails(action)
        }
    }

    private fun changeMealType(mealType: FavoriteFilterMealType) {
        _uiState.update {
            val next = it.copy(selectedMealFilter = mealType)
            next.copy(filteredFavorites = next.filterAndSort())
        }
    }

    private fun changeQuery(query: String) {
        _uiState.update {
            val next = it.copy(searchQuery = query)
            next.copy(filteredFavorites = next.filterAndSort())
        }
    }

    private fun changeSort(sort: FavoriteSortType) {
        _uiState.update {
            val next = it.copy(sortType = sort)
            next.copy(filteredFavorites = next.filterAndSort())
        }
    }

    private fun addToToday(recipe: FavoriteRecipe, mealType: MealType) {
        viewModelScope.launch {
            runCatching {
                favoriteUseCase.addFavoriteToToday(recipe, mealType)
            }.onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已加入今日记录：${recipe.foodName}"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "加入失败"))
            }
        }
    }

    private fun deleteFavorite(recipe: FavoriteRecipe) {
        viewModelScope.launch {
            runCatching {
                favoriteUseCase.removeFavorite(recipe)
            }.onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已删除收藏：${recipe.foodName}"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "删除失败"))
            }
        }
    }

    private fun updateRecipeDetails(action: RecipeAction.FavoriteLibrary.UpdateRecipeDetails) {
        viewModelScope.launch {
            runCatching {
                favoriteUseCase.updateRecipeDetails(
                    recipe = action.recipe,
                    ingredients = action.ingredients,
                    steps = action.steps,
                    tools = action.tools,
                    difficulty = action.difficulty,
                    durationMinutes = action.durationMinutes,
                    servings = action.servings
                )
            }.onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已保存做法详情"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "保存失败"))
            }
        }
    }
}

private fun FavoriteLibraryUiState.filterAndSort(): List<FavoriteRecipe> {
    val query = searchQuery.trim()
    val filtered = favorites.filter { recipe ->
        val sourceMealType = sourceMealTypeMap[recipe.id]
        val mealMatch = when (selectedMealFilter) {
            FavoriteFilterMealType.ALL -> true
            FavoriteFilterMealType.BREAKFAST -> sourceMealType == MealType.BREAKFAST
            FavoriteFilterMealType.LUNCH -> sourceMealType == MealType.LUNCH
            FavoriteFilterMealType.DINNER -> sourceMealType == MealType.DINNER
            FavoriteFilterMealType.SNACK -> sourceMealType in setOf(
                MealType.SNACK,
                MealType.BREAKFAST_SNACK,
                MealType.LUNCH_SNACK,
                MealType.DINNER_SNACK
            )
        }
        val queryMatch = query.isBlank() ||
            recipe.foodName.contains(query, ignoreCase = true) ||
            recipe.userInput.contains(query, ignoreCase = true)
        mealMatch && queryMatch
    }

    return when (sortType) {
        FavoriteSortType.LAST_USED -> filtered.sortedWith(
            compareByDescending<FavoriteRecipe> { it.lastUsedAt ?: 0L }
                .thenByDescending { it.useCount }
        )
        FavoriteSortType.USE_COUNT -> filtered.sortedWith(
            compareByDescending<FavoriteRecipe> { it.useCount }
                .thenByDescending { it.lastUsedAt ?: 0L }
        )
    }
}

data class FavoriteLibraryUiState(
    val favorites: List<FavoriteRecipe> = emptyList(),
    val filteredFavorites: List<FavoriteRecipe> = emptyList(),
    val sourceMealTypeMap: Map<String, MealType> = emptyMap(),
    val selectedMealFilter: FavoriteFilterMealType = FavoriteFilterMealType.ALL,
    val searchQuery: String = "",
    val sortType: FavoriteSortType = FavoriteSortType.LAST_USED
)
