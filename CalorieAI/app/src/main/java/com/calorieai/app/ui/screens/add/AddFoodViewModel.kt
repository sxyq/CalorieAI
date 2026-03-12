package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    fun onFoodDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(foodDescription = description)
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.value = _uiState.value.copy(selectedMealType = mealType)
    }

    fun saveFoodRecord(onSuccess: (String) -> Unit) {
        val description = _uiState.value.foodDescription
        if (description.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // 创建记录，所有数据为0，等待用户后续填写或AI分析
            val record = FoodRecord(
                foodName = extractFoodName(description),
                userInput = description,
                totalCalories = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f,
                mealType = _uiState.value.selectedMealType,
                recordTime = System.currentTimeMillis()
            )

            foodRecordRepository.addRecord(record)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess(record.id)
        }
    }

    private fun extractFoodName(description: String): String {
        // 提取食物名称，优先取第一个逗号前的内容
        val name = description.split(",").firstOrNull()?.trim()?.take(20)
            ?: description.trim().take(20)
        
        // 如果提取的名称为空，返回默认名称
        return if (name.isBlank()) "未命名食物" else name
    }
}

data class AddFoodUiState(
    val foodDescription: String = "",
    val selectedMealType: MealType = MealType.LUNCH,
    val isLoading: Boolean = false
)
