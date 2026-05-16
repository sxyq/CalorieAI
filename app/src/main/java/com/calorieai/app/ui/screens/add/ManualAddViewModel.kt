package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.Ingredient
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.utils.buildRecordTimeForDateAndMeal
import com.calorieai.app.utils.inferMainMealType
import com.calorieai.app.utils.isSameLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ManualAddViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository
) : ViewModel() {

    private companion object {
        const val DEFAULT_QUICK_ADD_GRAMS = 100
        const val MAX_QUICK_ADD_GRAMS = 5000
    }

    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ManualAddEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ManualAddEvent> = _events.asSharedFlow()

    private inline fun updateState(update: (ManualAddUiState) -> ManualAddUiState) {
        _uiState.value = update(_uiState.value)
    }

    init {
        viewModelScope.launch {
            favoriteRecipeRepository.getAllFavorites().collect { favorites ->
                val sourceIds = favorites
                    .map { it.sourceRecordId }
                    .filter { it.isNotBlank() }
                    .distinct()

                val sourceRecordMap = foodRecordRepository
                    .getRecordsByIds(sourceIds)
                    .associateBy { it.id }

                val recipeMealTypeMap = buildMap<String, MealType> {
                    favorites.forEach { favorite ->
                        sourceRecordMap[favorite.sourceRecordId]?.mealType?.let { mealType ->
                            put(favorite.id, mealType)
                        }
                    }
                }

                val favoriteDefaultGramsMap = buildMap<String, Int> {
                    favorites.forEach { favorite ->
                        val sourceRecord = sourceRecordMap[favorite.sourceRecordId]
                        val grams = sourceRecord?.let(::extractRecordGrams) ?: DEFAULT_QUICK_ADD_GRAMS
                        put(favorite.id, grams)
                    }
                }

                updateState {
                    val next = it.copy(
                        favoriteRecipes = favorites,
                        favoriteRecipeMealTypeMap = recipeMealTypeMap,
                        favoriteRecipeDefaultGramsMap = favoriteDefaultGramsMap
                    )
                    if (next.favoriteQuickAddGramsUserEdited) {
                        next
                    } else {
                        next.copy(favoriteQuickAddGrams = resolveMealDefaultGrams(next).toString())
                    }
                }
            }
        }
    }

    fun updateFoodName(name: String) = updateState { it.copy(foodName = name) }
    fun updateCalories(calories: String) = updateState { it.copy(calories = calories) }
    fun updateProtein(protein: String) = updateState { it.copy(protein = protein) }
    fun updateCarbs(carbs: String) = updateState { it.copy(carbs = carbs) }
    fun updateFat(fat: String) = updateState { it.copy(fat = fat) }
    fun updateFiber(fiber: String) = updateState { it.copy(fiber = fiber) }
    fun updateSugar(sugar: String) = updateState { it.copy(sugar = sugar) }
    fun updateSodium(sodium: String) = updateState { it.copy(sodium = sodium) }
    fun updateCholesterol(cholesterol: String) = updateState { it.copy(cholesterol = cholesterol) }
    fun updateSaturatedFat(saturatedFat: String) = updateState { it.copy(saturatedFat = saturatedFat) }
    fun updateCalcium(calcium: String) = updateState { it.copy(calcium = calcium) }
    fun updateIron(iron: String) = updateState { it.copy(iron = iron) }
    fun updateVitaminC(vitaminC: String) = updateState { it.copy(vitaminC = vitaminC) }
    fun updateMealType(mealType: MealType) = updateState { it.copy(mealType = mealType) }

    fun updateFavoriteMealType(mealType: MealType) = updateState {
        val next = it.copy(favoriteMealType = mealType)
        if (next.favoriteQuickAddGramsUserEdited) {
            next
        } else {
            next.copy(favoriteQuickAddGrams = resolveMealDefaultGrams(next).toString())
        }
    }

    fun toggleFavoriteQuickAddGramInput() =
        updateState { it.copy(showFavoriteQuickAddGramInput = !it.showFavoriteQuickAddGramInput) }

    fun updateFavoriteQuickAddGrams(input: String) {
        val digitsOnly = input.filter(Char::isDigit).take(4)
        val normalized = digitsOnly.toIntOrNull()
            ?.coerceIn(1, MAX_QUICK_ADD_GRAMS)
            ?.toString()
            ?: digitsOnly
        updateState {
            it.copy(
                favoriteQuickAddGrams = normalized,
                favoriteQuickAddGramsUserEdited = true
            )
        }
    }

    fun updateNotes(notes: String) = updateState { it.copy(notes = notes) }
    fun toggleNutritionDetails() = updateState { it.copy(includeNutritionDetails = !it.includeNutritionDetails) }
    fun toggleExtendedNutrition() = updateState { it.copy(showExtendedNutrition = !it.showExtendedNutrition) }

    fun resetFavoriteQuickAddGramsToDefault() = updateState {
        it.copy(
            favoriteQuickAddGrams = resolveMealDefaultGrams(it).toString(),
            favoriteQuickAddGramsUserEdited = false
        )
    }

    fun setDateContext(dateStr: String?) {
        if (dateStr.isNullOrBlank()) {
            val now = System.currentTimeMillis()
            val autoMealType = inferMainMealType(now)
            updateState {
                it.copy(
                    selectedDate = now,
                    isHistoricalDateMode = false,
                    mealType = autoMealType,
                    favoriteMealType = autoMealType
                )
            }
            return
        }

        try {
            val parts = dateStr.split("-")
            if (parts.size != 3) {
                setDateContext(null)
                return
            }

            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val selectedDateMillis = Calendar.getInstance().apply {
                set(year, month, day, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val now = System.currentTimeMillis()
            val isHistoricalDateMode = !isSameLocalDate(selectedDateMillis, now)

            updateState {
                it.copy(
                    selectedDate = selectedDateMillis,
                    isHistoricalDateMode = isHistoricalDateMode
                )
            }
        } catch (_: Exception) {
            setDateContext(null)
        }
    }

    fun saveRecord() {
        val state = _uiState.value
        val calories = state.calories.toIntOrNull() ?: 0
        val protein = state.protein.toFloatOrNull() ?: 0f
        val carbs = state.carbs.toFloatOrNull() ?: 0f
        val fat = state.fat.toFloatOrNull() ?: 0f
        val recordTime = resolveRecordTime(state, state.mealType)

        val record = FoodRecord(
            foodName = state.foodName,
            userInput = "手动录入: ${state.foodName}",
            totalCalories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = if (state.includeNutritionDetails) state.fiber.toFloatOrNull() ?: 0f else 0f,
            sugar = if (state.includeNutritionDetails) state.sugar.toFloatOrNull() ?: 0f else 0f,
            sodium = if (state.includeNutritionDetails) state.sodium.toFloatOrNull() ?: 0f else 0f,
            cholesterol = if (state.includeNutritionDetails) state.cholesterol.toFloatOrNull() ?: 0f else 0f,
            saturatedFat = if (state.includeNutritionDetails) state.saturatedFat.toFloatOrNull() ?: 0f else 0f,
            calcium = if (state.includeNutritionDetails) state.calcium.toFloatOrNull() ?: 0f else 0f,
            iron = if (state.includeNutritionDetails) state.iron.toFloatOrNull() ?: 0f else 0f,
            vitaminC = if (state.includeNutritionDetails) state.vitaminC.toFloatOrNull() ?: 0f else 0f,
            mealType = state.mealType,
            ingredients = listOf(
                Ingredient(
                    name = state.foodName,
                    weight = "1份",
                    calories = calories
                )
            ),
            recordTime = recordTime,
            notes = state.notes.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            runCatching {
                foodRecordRepository.addRecord(record)
            }.onSuccess {
                _events.emit(ManualAddEvent.ManualSaveSuccess(state.foodName))
            }.onFailure { error ->
                _events.emit(ManualAddEvent.ManualSaveFailed(error.message ?: "保存失败"))
            }
        }
    }

    fun addFavoriteRecipeToToday(recipe: FavoriteRecipe) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val mealType = state.mealType
                val recipeDefaultGrams = state.favoriteRecipeDefaultGramsMap[recipe.id]
                    ?: DEFAULT_QUICK_ADD_GRAMS
                val grams = if (state.favoriteQuickAddGramsUserEdited) {
                    state.favoriteQuickAddGrams.toIntOrNull()
                        ?.coerceIn(1, MAX_QUICK_ADD_GRAMS)
                        ?: recipeDefaultGrams
                } else {
                    recipeDefaultGrams
                }
                val scaleFactor = grams / 100f
                val record = FoodRecord(
                    foodName = recipe.foodName,
                    userInput = recipe.userInput,
                    totalCalories = (recipe.totalCalories * scaleFactor).roundToInt().coerceAtLeast(0),
                    protein = recipe.protein * scaleFactor,
                    carbs = recipe.carbs * scaleFactor,
                    fat = recipe.fat * scaleFactor,
                    fiber = recipe.fiber * scaleFactor,
                    sugar = recipe.sugar * scaleFactor,
                    sodium = recipe.sodium * scaleFactor,
                    cholesterol = recipe.cholesterol * scaleFactor,
                    saturatedFat = recipe.saturatedFat * scaleFactor,
                    calcium = recipe.calcium * scaleFactor,
                    iron = recipe.iron * scaleFactor,
                    vitaminC = recipe.vitaminC * scaleFactor,
                    vitaminA = recipe.vitaminA * scaleFactor,
                    potassium = recipe.potassium * scaleFactor,
                    ingredients = listOf(
                        Ingredient(
                            name = recipe.foodName,
                            weight = "${grams}g",
                            calories = (recipe.totalCalories * scaleFactor).roundToInt().coerceAtLeast(0)
                        )
                    ),
                    mealType = mealType,
                    recordTime = resolveRecordTime(state, mealType)
                )
                foodRecordRepository.addRecord(record)
                favoriteRecipeRepository.upsert(
                    recipe.copy(
                        lastUsedAt = recipe.lastUsedAt,
                        useCount = recipe.useCount + 1
                    )
                )
                _events.emit(ManualAddEvent.FavoriteQuickAddSuccess(recipe.foodName))
            } catch (e: Exception) {
                _events.emit(ManualAddEvent.FavoriteQuickAddFailed(e.message ?: "添加失败"))
            }
        }
    }

    private fun resolveRecordTime(state: ManualAddUiState, mealType: MealType): Long {
        return if (state.isHistoricalDateMode) {
            buildRecordTimeForDateAndMeal(state.selectedDate, mealType)
        } else {
            System.currentTimeMillis()
        }
    }

    private fun resolveMealDefaultGrams(state: ManualAddUiState): Int {
        val favorite = state.favoriteRecipes
            .asSequence()
            .filter { state.favoriteRecipeMealTypeMap[it.id] == state.favoriteMealType }
            .sortedByDescending { it.lastUsedAt ?: 0L }
            .firstOrNull()

        return favorite?.let { state.favoriteRecipeDefaultGramsMap[it.id] }
            ?.coerceIn(1, MAX_QUICK_ADD_GRAMS)
            ?: DEFAULT_QUICK_ADD_GRAMS
    }

    private fun extractRecordGrams(record: FoodRecord): Int {
        val fromIngredients = record.ingredients
            .asSequence()
            .mapNotNull { parseGramsFromText(it.weight) }
            .firstOrNull()
        val fromUserInput = parseGramsFromText(record.userInput)
        return (fromIngredients ?: fromUserInput ?: DEFAULT_QUICK_ADD_GRAMS)
            .coerceIn(1, MAX_QUICK_ADD_GRAMS)
    }

    private fun parseGramsFromText(text: String?): Int? {
        if (text.isNullOrBlank()) return null
        val normalized = text.lowercase()

        val kgMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(kg|千克)").find(normalized)
        if (kgMatch != null) {
            val value = kgMatch.groupValues.getOrNull(1)?.toFloatOrNull() ?: return null
            return (value * 1000f).roundToInt().coerceAtLeast(1)
        }

        val gramMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(g|克|gram|grams)").find(normalized)
        if (gramMatch != null) {
            val value = gramMatch.groupValues.getOrNull(1)?.toFloatOrNull() ?: return null
            return value.roundToInt().coerceAtLeast(1)
        }

        return null
    }
}

sealed interface ManualAddEvent {
    data class ManualSaveSuccess(val foodName: String) : ManualAddEvent
    data class ManualSaveFailed(val message: String) : ManualAddEvent
    data class FavoriteQuickAddSuccess(val foodName: String) : ManualAddEvent
    data class FavoriteQuickAddFailed(val message: String) : ManualAddEvent
}

data class ManualAddUiState(
    val foodName: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val fiber: String = "",
    val sugar: String = "",
    val sodium: String = "",
    val cholesterol: String = "",
    val saturatedFat: String = "",
    val calcium: String = "",
    val iron: String = "",
    val vitaminC: String = "",
    val includeNutritionDetails: Boolean = true,
    val showExtendedNutrition: Boolean = false,
    val mealType: MealType = MealType.LUNCH,
    val favoriteMealType: MealType = MealType.LUNCH,
    val showFavoriteQuickAddGramInput: Boolean = false,
    val favoriteQuickAddGrams: String = "100",
    val favoriteQuickAddGramsUserEdited: Boolean = false,
    val notes: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val isHistoricalDateMode: Boolean = false,
    val favoriteRecipes: List<FavoriteRecipe> = emptyList(),
    val favoriteRecipeMealTypeMap: Map<String, MealType> = emptyMap(),
    val favoriteRecipeDefaultGramsMap: Map<String, Int> = emptyMap()
)
