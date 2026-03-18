package com.calorieai.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.FavoriteRecipeRepository
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
            _uiState.value = ResultUiState(
                record = record,
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
    
    suspend fun isFavorite(foodName: String): Boolean {
        return favoriteRecipeRepository.isFavorite(foodName)
    }
    
    suspend fun addFavorite(record: FoodRecord) {
        favoriteRecipeRepository.addFavoriteFromFoodRecord(record)
    }
    
    suspend fun removeFavorite(foodName: String) {
        val favorites = favoriteRecipeRepository.getAllFavorites()
        favorites.collect { list ->
            list.find { it.foodName == foodName }?.let {
                favoriteRecipeRepository.deleteFavorite(it)
            }
            return@collect
        }
    }
}

data class ResultUiState(
    val record: FoodRecord? = null,
    val isLoading: Boolean = false
)
