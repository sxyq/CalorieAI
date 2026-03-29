package com.calorieai.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.domain.recipe.FavoriteUseCase
import com.calorieai.app.service.ai.FoodTextAnalysisService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val favoriteUseCase: FavoriteUseCase,
    private val foodTextAnalysisService: FoodTextAnalysisService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val record = foodRecordRepository.getRecordById(recordId)
            _uiState.value = ResultUiState(
                record = record,
                isFavoritedRecipe = favoriteUseCase.isFavoritedBySourceRecord(recordId),
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
                    maxRetries = 2,
                    requestTag = "result-regenerate-${UUID.randomUUID()}"
                )
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isRegenerating = false,
                        regenerateMessage = result.exceptionOrNull()?.message ?: "重新生成失败"
                    )
                    return@launch
                }

                val items = result.getOrNull()?.items.orEmpty()
                if (items.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isRegenerating = false,
                        regenerateMessage = "重新生成失败：AI未返回有效数据"
                    )
                    return@launch
                }
                val bestItem = selectBestMatchItem(
                    currentRecord = currentRecord,
                    items = items
                )

                val updatedRecord = currentRecord.copy(
                    foodName = bestItem.foodName.takeIf { it.isNotBlank() } ?: currentRecord.foodName,
                    totalCalories = bestItem.calories.toInt().coerceAtLeast(0),
                    protein = bestItem.protein,
                    carbs = bestItem.carbs,
                    fat = bestItem.fat,
                    fiber = bestItem.fiber,
                    sugar = bestItem.sugar,
                    sodium = bestItem.sodium,
                    cholesterol = bestItem.cholesterol,
                    saturatedFat = bestItem.saturatedFat,
                    calcium = bestItem.calcium,
                    iron = bestItem.iron,
                    vitaminC = bestItem.vitaminC,
                    vitaminA = bestItem.vitaminA,
                    potassium = bestItem.potassium
                )

                foodRecordRepository.updateRecord(updatedRecord)
                val latestRecord = foodRecordRepository.getRecordById(currentRecord.id) ?: updatedRecord
                val changed = hasNutritionChanged(currentRecord, latestRecord)
                _uiState.value = _uiState.value.copy(
                    record = latestRecord,
                    isRegenerating = false,
                    regenerateMessage = if (changed) {
                        "已重新生成并更新数据"
                    } else {
                        "已完成重新生成，数值与当前结果一致"
                    }
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
            val isFavorited = favoriteUseCase.toggleFavoriteFromRecord(record)
            if (!isFavorited) {
                _uiState.value = _uiState.value.copy(
                    isFavoritedRecipe = false,
                    favoriteMessage = "已取消收藏"
                )
            } else {
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

    private fun selectBestMatchItem(
        currentRecord: FoodRecord,
        items: List<com.calorieai.app.data.model.FoodAnalysisResult>
    ): com.calorieai.app.data.model.FoodAnalysisResult {
        if (items.size == 1) return items.first()
        val currentName = currentRecord.foodName.trim()
        val exact = items.firstOrNull { it.foodName.trim() == currentName }
        if (exact != null) return exact

        val partial = items.firstOrNull {
            val candidate = it.foodName.trim()
            candidate.contains(currentName) || currentName.contains(candidate)
        }
        if (partial != null) return partial

        return items.minByOrNull { candidate ->
            abs(candidate.calories.toInt() - currentRecord.totalCalories)
        } ?: items.first()
    }

    private fun hasNutritionChanged(
        old: FoodRecord,
        new: FoodRecord
    ): Boolean {
        return old.foodName != new.foodName ||
            old.totalCalories != new.totalCalories ||
            old.protein != new.protein ||
            old.carbs != new.carbs ||
            old.fat != new.fat ||
            old.fiber != new.fiber ||
            old.sugar != new.sugar ||
            old.sodium != new.sodium ||
            old.cholesterol != new.cholesterol ||
            old.saturatedFat != new.saturatedFat ||
            old.calcium != new.calcium ||
            old.iron != new.iron ||
            old.vitaminC != new.vitaminC ||
            old.vitaminA != new.vitaminA ||
            old.potassium != new.potassium
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
