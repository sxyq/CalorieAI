package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.TextFoodAnalysisResult
import com.calorieai.app.service.ai.FoodTextAnalysisService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val foodTextAnalysisService: FoodTextAnalysisService
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
            try {
                // 调用AI服务分析食物
                val analysisResult = foodTextAnalysisService.analyzeFoodText(description)

                val (foodName, calories, protein, carbs, fat) = if (analysisResult.isSuccess) {
                    val result = analysisResult.getOrNull()!!
                    // 使用AI分析结果
                    Quadruple(
                        result.foodName.takeIf { it.isNotBlank() } ?: extractFoodName(description),
                        result.calories,
                        result.protein,
                        result.carbs,
                        result.fat
                    )
                } else {
                    // AI分析失败，使用默认值
                    Quadruple(
                        extractFoodName(description),
                        0,
                        0f,
                        0f,
                        0f
                    )
                }

                val record = FoodRecord(
                    foodName = foodName,
                    userInput = description,
                    totalCalories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    mealType = _uiState.value.selectedMealType,
                    recordTime = System.currentTimeMillis()
                )

                foodRecordRepository.addRecord(record)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(record.id)
            } catch (e: Exception) {
                // 发生异常时创建默认记录
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
    }

    // 辅助数据类用于返回多个值
    private data class Quadruple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    private fun extractFoodName(description: String): String {
        // 尝试从描述中提取食物名称
        // 1. 首先尝试按逗号、顿号分割，取可能包含食物的部分
        val separators = listOf("，", ",", "、", " ")
        var name = description
        
        for (separator in separators) {
            val parts = description.split(separator)
            // 查找包含常见食物关键词的部分
            val foodPart = parts.find { part ->
                part.contains(Regex("(吃|喝|了|份|个|碗|盘|杯|块|片|根|条|粒|颗|只|斤|克|g|kg)"))
            }
            if (foodPart != null && foodPart.length < name.length) {
                name = foodPart
                break
            }
        }
        
        // 2. 清理常见的非食物词汇
        val cleanWords = listOf("今天", "我", "吃", "了", "一份", "一个", "一碗", "一杯", "一盘", "一些", "一点", "大约", "大概", "大概", "左右")
        var cleanedName = name
        for (word in cleanWords) {
            cleanedName = cleanedName.replace(word, "")
        }
        
        // 3. 如果清理后为空，使用原始描述
        val finalName = cleanedName.trim().takeIf { it.isNotBlank() } ?: name.trim()
        
        // 4. 截取前20个字符，如果为空返回默认名称
        return finalName.take(20).takeIf { it.isNotBlank() } ?: "未命名食物"
    }
}

data class AddFoodUiState(
    val foodDescription: String = "",
    val selectedMealType: MealType = MealType.LUNCH,
    val isLoading: Boolean = false
)
