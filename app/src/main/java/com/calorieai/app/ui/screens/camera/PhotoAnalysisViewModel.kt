package com.calorieai.app.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.FoodImageAnalysisService
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.utils.inferMainMealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoAnalysisViewModel @Inject constructor(
    private val foodImageAnalysisService: FoodImageAnalysisService,
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoAnalysisUiState())
    val uiState: StateFlow<PhotoAnalysisUiState> = _uiState.asStateFlow()

    private var currentPhotoUri: Uri? = null

    fun analyzePhoto(photoUri: Uri, context: Context) {
        currentPhotoUri = photoUri
        _uiState.value = _uiState.value.copy(
            isAnalyzing = true,
            error = null,
            analysisResult = null,
            retryMessage = null,
            retryAttempt = 0
        )

        viewModelScope.launch {
            val maxRetries = _uiState.value.maxRetries.coerceAtLeast(0)
            val result = foodImageAnalysisService.analyzeFoodImage(
                imageUri = photoUri,
                context = context,
                userHint = _uiState.value.userHint,
                maxRetries = maxRetries,
                onRetry = { attempt, maxAttempts, reason ->
                    val totalRetries = (maxAttempts - 1).coerceAtLeast(0)
                    _uiState.value = _uiState.value.copy(
                        retryMessage = "结果不稳定，正在补救重试（${attempt}/${totalRetries}）：$reason",
                        retryAttempt = attempt
                    )
                }
            )

            result.fold(
                onSuccess = { analysisResult ->
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        analysisResult = analysisResult,
                        editedResult = analysisResult,
                        retryMessage = null
                    )
                },
                onFailure = { error ->
                    val errorInfo = AIErrorClassifier.classify(error)
                    val uiMessage = when (errorInfo.category) {
                        AIErrorCategory.PARSE,
                        AIErrorCategory.VALIDATION -> "AI返回结果不稳定，请重试或更换更清晰的图片。"
                        else -> errorInfo.userMessage
                    }
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = uiMessage,
                        retryMessage = null
                    )
                }
            )
        }
    }

    fun reanalyze(context: Context) {
        currentPhotoUri?.let { uri ->
            analyzePhoto(uri, context)
        }
    }

    fun onUserHintChange(hint: String) {
        _uiState.value = _uiState.value.copy(userHint = hint)
    }

    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
    }

    fun updateEditedResult(result: FoodAnalysisResult) {
        _uiState.value = _uiState.value.copy(editedResult = result)
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.value = _uiState.value.copy(selectedMealType = mealType)
    }

    fun saveRecord(onComplete: () -> Unit) {
        val state = _uiState.value
        val result = state.editedResult ?: return

        viewModelScope.launch {
            val record = FoodRecord(
                foodName = result.foodName,
                userInput = "拍照识别：${result.foodName} - ${result.description}",
                totalCalories = result.calories.toInt(),
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                fiber = result.fiber,
                sugar = result.sugar,
                sodium = result.sodium,
                cholesterol = result.cholesterol,
                saturatedFat = result.saturatedFat,
                calcium = result.calcium,
                iron = result.iron,
                vitaminC = result.vitaminC,
                vitaminA = result.vitaminA,
                potassium = result.potassium,
                mealType = state.selectedMealType,
                recordTime = System.currentTimeMillis()
            )

            foodRecordRepository.addRecord(record)
            onComplete()
        }
    }
}

data class PhotoAnalysisUiState(
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val editedResult: FoodAnalysisResult? = null,
    val userHint: String = "",
    val isEditMode: Boolean = false,
    val retryMessage: String? = null,
    val retryAttempt: Int = 0,
    val maxRetries: Int = 1,
    val selectedMealType: MealType = inferMainMealType(System.currentTimeMillis())
)
