package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteRecipesViewModel @Inject constructor(
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteRecipesUiState())
    val uiState: StateFlow<FavoriteRecipesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRecipeRepository.getAllFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun addFavoriteToToday(recipe: FavoriteRecipe, onDone: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val mealType = _uiState.value.selectedMealType
            val record = FoodRecord(
                foodName = recipe.foodName,
                userInput = recipe.userInput,
                totalCalories = recipe.totalCalories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat,
                fiber = recipe.fiber,
                sugar = recipe.sugar,
                sodium = recipe.sodium,
                cholesterol = recipe.cholesterol,
                saturatedFat = recipe.saturatedFat,
                calcium = recipe.calcium,
                iron = recipe.iron,
                vitaminC = recipe.vitaminC,
                vitaminA = recipe.vitaminA,
                potassium = recipe.potassium,
                mealType = mealType,
                recordTime = now
            )
            foodRecordRepository.addRecord(record)
            favoriteRecipeRepository.upsert(
                recipe.copy(
                    lastUsedAt = now,
                    useCount = recipe.useCount + 1
                )
            )
            onDone()
        }
    }

    fun removeFavorite(recipe: FavoriteRecipe) {
        viewModelScope.launch {
            favoriteRecipeRepository.delete(recipe)
        }
    }
}

data class FavoriteRecipesUiState(
    val favorites: List<FavoriteRecipe> = emptyList(),
    val selectedMealType: MealType = MealType.LUNCH
)
