package com.calorieai.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.FoodTextAnalysisService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val foodTextAnalysisService: FoodTextAnalysisService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val record = foodRecordRepository.getRecordById(recordId)
            val favorite = favoriteRecipeRepository.getBySourceRecordId(recordId)
            _uiState.value = ResultUiState(
                record = record,
                isFavoritedRecipe = favorite != null,
                isLoading = false
            )
        }
    }

    fun updateRecord(updatedRecord: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.updateRecord(updatedRecord)
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            foodRecordRepository.deleteRecordById(recordId)
        }
    }

    fun regenerateCurrentRecord() {
        val currentRecord = _uiState.value.record ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegenerating = true, regenerateMessage = null)
            try {
                val result = foodTextAnalysisService.analyzeFoodText(
                    foodDescription = currentRecord.userInput,
                    maxRetries = 2
                )
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isRegenerating = false,
                        regenerateMessage = result.exceptionOrNull()?.message ?: "重新生成失败"
                    )
                    return@launch
                }

                val firstItem = result.getOrNull()?.items?.firstOrNull()
                if (firstItem == null) {
                    _uiState.value = _uiState.value.copy(
                        isRegenerating = false,
                        regenerateMessage = "重新生成失败：AI未返回有效数据"
                    )
                    return@launch
                }

                val updatedRecord = currentRecord.copy(
                    foodName = firstItem.foodName.takeIf { it.isNotBlank() } ?: currentRecord.foodName,
                    totalCalories = firstItem.calories.toInt().coerceAtLeast(0),
                    protein = firstItem.protein,
                    carbs = firstItem.carbs,
                    fat = firstItem.fat,
                    fiber = firstItem.fiber,
                    sugar = firstItem.sugar,
                    sodium = firstItem.sodium,
                    cholesterol = firstItem.cholesterol,
                    saturatedFat = firstItem.saturatedFat,
                    calcium = firstItem.calcium,
                    iron = firstItem.iron,
                    vitaminC = firstItem.vitaminC,
                    vitaminA = firstItem.vitaminA,
                    potassium = firstItem.potassium
                )

                foodRecordRepository.updateRecord(updatedRecord)
                _uiState.value = _uiState.value.copy(
                    record = updatedRecord,
                    isRegenerating = false,
                    regenerateMessage = "已重新生成并更新数据"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRegenerating = false,
                    regenerateMessage = "重新生成失败：${e.message ?: "未知错误"}"
                )
            }
        }
    }

    fun toggleFavoriteRecipe() {
        val record = _uiState.value.record ?: return
        viewModelScope.launch {
            val existing = favoriteRecipeRepository.getBySourceRecordId(record.id)
            if (existing != null) {
                favoriteRecipeRepository.delete(existing)
                _uiState.value = _uiState.value.copy(
                    isFavoritedRecipe = false,
                    favoriteMessage = "已取消收藏"
                )
            } else {
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
                _uiState.value = _uiState.value.copy(
                    isFavoritedRecipe = true,
                    favoriteMessage = "已收藏到菜谱"
                )
            }
        }
    }

    fun clearFavoriteMessage() {
        _uiState.value = _uiState.value.copy(favoriteMessage = null)
    }

    fun clearRegenerateMessage() {
        _uiState.value = _uiState.value.copy(regenerateMessage = null)
    }
}

data class ResultUiState(
    val record: FoodRecord? = null,
    val isLoading: Boolean = false,
    val isRegenerating: Boolean = false,
    val isFavoritedRecipe: Boolean = false,
    val favoriteMessage: String? = null,
    val regenerateMessage: String? = null
)
