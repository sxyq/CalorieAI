package com.calorieai.app.domain.recipe

import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteUseCase @Inject constructor(
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val foodRecordRepository: FoodRecordRepository
) {
    fun observeFavorites(): Flow<List<FavoriteRecipe>> = favoriteRecipeRepository.getAllFavorites()

    suspend fun getSourceMealTypeMap(favorites: List<FavoriteRecipe>): Map<String, MealType> {
        if (favorites.isEmpty()) return emptyMap()
        return buildMap {
            favorites.forEach { favorite ->
                val sourceMealType = foodRecordRepository
                    .getRecordById(favorite.sourceRecordId)
                    ?.mealType
                if (sourceMealType != null) {
                    put(favorite.id, sourceMealType)
                }
            }
        }
    }

    suspend fun addFavoriteToToday(recipe: FavoriteRecipe, mealType: MealType) {
        val now = System.currentTimeMillis()
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
    }

    suspend fun toggleFavoriteFromRecord(record: FoodRecord): Boolean {
        val existing = favoriteRecipeRepository.getBySourceRecordId(record.id)
        if (existing != null) {
            favoriteRecipeRepository.delete(existing)
            return false
        }

        favoriteRecipeRepository.upsert(
            FavoriteRecipe(
                sourceRecordId = record.id,
                foodName = record.foodName,
                userInput = record.userInput,
                totalCalories = record.totalCalories,
                protein = record.protein,
                carbs = record.carbs,
                fat = record.fat,
                fiber = record.fiber,
                sugar = record.sugar,
                sodium = record.sodium,
                cholesterol = record.cholesterol,
                saturatedFat = record.saturatedFat,
                calcium = record.calcium,
                iron = record.iron,
                vitaminC = record.vitaminC,
                vitaminA = record.vitaminA,
                potassium = record.potassium
            )
        )
        return true
    }

    suspend fun removeFavorite(recipe: FavoriteRecipe) {
        favoriteRecipeRepository.delete(recipe)
    }

    suspend fun updateRecipeDetails(
        recipe: FavoriteRecipe,
        ingredients: String?,
        steps: String?,
        tools: String?,
        difficulty: String?,
        durationMinutes: Int?,
        servings: Int?
    ) {
        favoriteRecipeRepository.upsert(
            recipe.copy(
                recipeIngredientsText = ingredients?.trim()?.ifBlank { null },
                recipeStepsText = steps?.trim()?.ifBlank { null },
                recipeToolsText = tools?.trim()?.ifBlank { null },
                recipeDifficulty = difficulty?.trim()?.ifBlank { null },
                recipeDurationMinutes = durationMinutes?.coerceAtLeast(1),
                recipeServings = servings?.coerceAtLeast(1),
                recipeSourceType = recipe.recipeSourceType ?: "MERGED",
                recipeUpdatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun isFavoritedBySourceRecord(recordId: String): Boolean {
        return favoriteRecipeRepository.getBySourceRecordId(recordId) != null
    }

    suspend fun getAllFavoritesOnce(): List<FavoriteRecipe> {
        return favoriteRecipeRepository.getAllFavorites().first()
    }
}

