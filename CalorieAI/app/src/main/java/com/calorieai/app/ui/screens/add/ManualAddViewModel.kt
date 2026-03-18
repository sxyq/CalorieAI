package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.Ingredient
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualAddViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    private inline fun updateState(update: (ManualAddUiState) -> ManualAddUiState) {
        _uiState.value = update(_uiState.value)
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
    fun updateNotes(notes: String) = updateState { it.copy(notes = notes) }

    fun toggleNutritionDetails() = updateState { it.copy(includeNutritionDetails = !it.includeNutritionDetails) }
    fun toggleExtendedNutrition() = updateState { it.copy(showExtendedNutrition = !it.showExtendedNutrition) }

    fun saveRecord() {
        val state = _uiState.value

        val calories = state.calories.toIntOrNull() ?: 0
        val protein = state.protein.toFloatOrNull() ?: 0f
        val carbs = state.carbs.toFloatOrNull() ?: 0f
        val fat = state.fat.toFloatOrNull() ?: 0f

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
            notes = state.notes.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            foodRecordRepository.addRecord(record)
        }
    }
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
    val notes: String = ""
)
