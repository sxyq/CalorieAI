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

    fun updateFoodName(name: String) {
        _uiState.value = _uiState.value.copy(foodName = name)
    }

    fun updateCalories(calories: String) {
        _uiState.value = _uiState.value.copy(calories = calories)
    }

    fun updateProtein(protein: String) {
        _uiState.value = _uiState.value.copy(protein = protein)
    }

    fun updateCarbs(carbs: String) {
        _uiState.value = _uiState.value.copy(carbs = carbs)
    }

    fun updateFat(fat: String) {
        _uiState.value = _uiState.value.copy(fat = fat)
    }

    fun updateMealType(mealType: MealType) {
        _uiState.value = _uiState.value.copy(mealType = mealType)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

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
    val mealType: MealType = MealType.LUNCH,
    val notes: String = ""
)
