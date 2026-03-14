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

    // 基础营养素
    fun updateProtein(protein: String) {
        _uiState.value = _uiState.value.copy(protein = protein)
    }

    fun updateCarbs(carbs: String) {
        _uiState.value = _uiState.value.copy(carbs = carbs)
    }

    fun updateFat(fat: String) {
        _uiState.value = _uiState.value.copy(fat = fat)
    }

    // 扩展营养素
    fun updateFiber(fiber: String) {
        _uiState.value = _uiState.value.copy(fiber = fiber)
    }

    fun updateSugar(sugar: String) {
        _uiState.value = _uiState.value.copy(sugar = sugar)
    }

    fun updateSodium(sodium: String) {
        _uiState.value = _uiState.value.copy(sodium = sodium)
    }

    fun updateCholesterol(cholesterol: String) {
        _uiState.value = _uiState.value.copy(cholesterol = cholesterol)
    }

    fun updateSaturatedFat(saturatedFat: String) {
        _uiState.value = _uiState.value.copy(saturatedFat = saturatedFat)
    }

    fun updateCalcium(calcium: String) {
        _uiState.value = _uiState.value.copy(calcium = calcium)
    }

    fun updateIron(iron: String) {
        _uiState.value = _uiState.value.copy(iron = iron)
    }

    fun updateVitaminC(vitaminC: String) {
        _uiState.value = _uiState.value.copy(vitaminC = vitaminC)
    }

    // 切换是否添加营养素详情
    fun toggleNutritionDetails() {
        _uiState.value = _uiState.value.copy(
            includeNutritionDetails = !_uiState.value.includeNutritionDetails
        )
    }

    // 切换是否显示扩展营养素
    fun toggleExtendedNutrition() {
        _uiState.value = _uiState.value.copy(
            showExtendedNutrition = !_uiState.value.showExtendedNutrition
        )
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
            // 扩展营养素（仅在开启详情时保存）
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
    // 基础营养素
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    // 扩展营养素
    val fiber: String = "",
    val sugar: String = "",
    val sodium: String = "",
    val cholesterol: String = "",
    val saturatedFat: String = "",
    val calcium: String = "",
    val iron: String = "",
    val vitaminC: String = "",
    // 控制选项
    val includeNutritionDetails: Boolean = true,  // 是否添加营养素详情
    val showExtendedNutrition: Boolean = false,   // 是否显示扩展营养素
    val mealType: MealType = MealType.LUNCH,
    val notes: String = ""
)
