package com.calorieai.app.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.FoodAnalysisResult
import com.calorieai.app.service.ai.FoodImageAnalysisService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
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
            analysisResult = null
        )

        viewModelScope.launch {
            val result = foodImageAnalysisService.analyzeFoodImage(
                imageUri = photoUri,
                context = context,
                userHint = _uiState.value.userHint
            )

            result.fold(
                onSuccess = { analysisResult ->
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        analysisResult = analysisResult,
                        editedResult = analysisResult
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = error.message ?: "分析失败"
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

    fun saveRecord(onComplete: () -> Unit) {
        val result = _uiState.value.editedResult ?: return

        viewModelScope.launch {
            val record = FoodRecord(
                foodName = result.foodName,
                userInput = "拍照识别：${result.foodName} - ${result.description}",
                totalCalories = result.calories,
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                mealType = inferMealType(),
                recordTime = System.currentTimeMillis()
            )

            foodRecordRepository.addRecord(record)
            onComplete()
        }
    }

    private fun inferMealType(): MealType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> MealType.BREAKFAST
            in 11..14 -> MealType.LUNCH
            in 17..21 -> MealType.DINNER
            else -> MealType.SNACK
        }
    }
}

data class PhotoAnalysisUiState(
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val editedResult: FoodAnalysisResult? = null,
    val userHint: String = "",
    val isEditMode: Boolean = false
)
