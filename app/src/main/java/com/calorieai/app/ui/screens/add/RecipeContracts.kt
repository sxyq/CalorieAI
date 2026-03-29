package com.calorieai.app.ui.screens.add

import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import java.time.LocalDate

sealed interface RecipeUiEvent {
    data class Snackbar(val message: String) : RecipeUiEvent
}

sealed interface RecipeAction {
    sealed interface Home : RecipeAction {
        data object GenerateSuggestion : Home
        data class GeneratePlan(val days: Int) : Home
        data class AddFavoriteToToday(
            val recipe: FavoriteRecipe,
            val mealType: MealType
        ) : Home
        data object SavePersonalization : Home
    }

    sealed interface FavoriteLibrary : RecipeAction {
        data class ChangeMealType(val mealType: FavoriteFilterMealType) : FavoriteLibrary
        data class ChangeQuery(val query: String) : FavoriteLibrary
        data class ChangeSort(val sort: FavoriteSortType) : FavoriteLibrary
        data class AddToToday(val recipe: FavoriteRecipe, val mealType: MealType) : FavoriteLibrary
        data class DeleteFavorite(val recipe: FavoriteRecipe) : FavoriteLibrary
        data class UpdateRecipeDetails(
            val recipe: FavoriteRecipe,
            val ingredients: String?,
            val steps: String?,
            val tools: String?,
            val difficulty: String?,
            val durationMinutes: Int?,
            val servings: Int?
        ) : FavoriteLibrary
    }

    sealed interface Pantry : RecipeAction {
        data class AddIngredient(
            val name: String,
            val quantity: Float,
            val unit: String,
            val daysToExpire: Int?,
            val notes: String?
        ) : Pantry
        data class DeleteIngredient(val item: PantryIngredient) : Pantry
    }

    sealed interface MealPlan : RecipeAction {
        data class SaveManualPlan(
            val title: String,
            val startDate: LocalDate,
            val days: Int,
            val menuText: String
        ) : MealPlan
        data class DeletePlan(val item: RecipePlan) : MealPlan
        data class GenerateByAi(
            val days: Int,
            val startDate: LocalDate
        ) : MealPlan
    }
}

enum class FavoriteSortType {
    LAST_USED,
    USE_COUNT
}

enum class FavoriteFilterMealType {
    ALL,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

