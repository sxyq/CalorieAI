package com.calorieai.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository
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
}

data class ResultUiState(
    val record: FoodRecord? = null,
    val isLoading: Boolean = false,
    val isFavoritedRecipe: Boolean = false,
    val favoriteMessage: String? = null
)
