package com.calorieai.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.domain.recipe.FavoriteUseCase
import com.calorieai.app.service.ai.FoodTextAnalysisService
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.service.ai.common.AIErrorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
                var continuousAttempt = 0
                while (isActive) {
                    continuousAttempt += 1
                    val result = foodTextAnalysisService.analyzeFoodText(
                        foodDescription = currentRecord.userInput,
                        maxRetries = 0,
                        requestTag = "result-regenerate-${UUID.randomUUID()}"
                    )
                    if (result.isFailure) {
                        val errorInfo = AIErrorClassifier.classify(result.exceptionOrNull())
                        if (isTerminalRegenerateError(errorInfo)) {
                            _uiState.value = _uiState.value.copy(
                                isRegenerating = false,
                                regenerateMessage = errorInfo.userMessage
                            )
                            return@launch
                        }
                        _uiState.value = _uiState.value.copy(
                            regenerateMessage = "重新生成失败，持续重试中（第${continuousAttempt}轮）..."
                        )
                        delay(computeRetryDelayMillis(continuousAttempt))
                        continue
                    }

                    val items = result.getOrNull()?.items.orEmpty()
                    if (items.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            regenerateMessage = "AI未返回有效数据，持续重试中（第${continuousAttempt}轮）..."
                        )
                        delay(computeRetryDelayMillis(continuousAttempt))
                        continue
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
                    return@launch
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRegenerating = false,
                    regenerateMessage = "重新生成失败：${e.message ?: "未知错误"}"
                )
            }
        }
    }

    private fun isTerminalRegenerateError(errorInfo: AIErrorInfo): Boolean {
        return when (errorInfo.category) {
            AIErrorCategory.HTTP -> !errorInfo.retryEligible
            AIErrorCategory.VALIDATION -> !errorInfo.retryEligible
            else -> false
        }
    }

    private fun computeRetryDelayMillis(attempt: Int): Long {
        val exponent = (attempt - 1).coerceIn(0, 5)
        val delayMillis = 1_000L * (1L shl exponent)
        return delayMillis.coerceAtMost(15_000L)
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
