package com.calorieai.app.ui.screens.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.Ingredient
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.NutritionInfo
import com.calorieai.app.service.ai.NutritionRecognitionService
import com.calorieai.app.utils.buildRecordTimeForDateAndMeal
import com.calorieai.app.utils.inferMainMealType
import com.calorieai.app.utils.isSameLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NutritionOcrImportViewModel @Inject constructor(
    private val nutritionRecognitionService: NutritionRecognitionService,
    private val foodRecordRepository: FoodRecordRepository,
    private val favoriteRecipeRepository: FavoriteRecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionOcrImportUiState())
    val uiState: StateFlow<NutritionOcrImportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NutritionOcrImportEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NutritionOcrImportEvent> = _events.asSharedFlow()

    fun setSaveContext(dateStr: String?, mealType: MealType?) {
        val now = System.currentTimeMillis()
        val defaultMealType = mealType ?: inferMainMealType(now)

        if (dateStr.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedDate = now,
                mealType = defaultMealType,
                isHistoricalDateMode = false
            )
            return
        }

        runCatching {
            val parts = dateStr.split("-")
            require(parts.size == 3)
            Calendar.getInstance().apply {
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.onSuccess { selectedDateMillis ->
            _uiState.value = _uiState.value.copy(
                selectedDate = selectedDateMillis,
                mealType = defaultMealType,
                isHistoricalDateMode = !isSameLocalDate(selectedDateMillis, now)
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(
                selectedDate = now,
                mealType = defaultMealType,
                isHistoricalDateMode = false
            )
        }
    }

    fun recognizeNutrition(imageUri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(
            isRecognizing = true,
            imageUri = imageUri,
            nutritionInfo = null,
            foodName = "",
            weightGrams = "",
            caloriesPer100g = "",
            proteinPer100g = "",
            carbsPer100g = "",
            fatPer100g = "",
            ocrSource = "",
            rawText = "",
            errorMessage = null,
            isSaving = false
        )

        viewModelScope.launch {
            val result = nutritionRecognitionService.recognizeNutritionTable(imageUri, context)
            result.fold(
                onSuccess = { info ->
                    _uiState.value = _uiState.value.copy(
                        isRecognizing = false,
                        nutritionInfo = info,
                        caloriesPer100g = info.calories.takeIf { it > 0 }?.toString().orEmpty(),
                        proteinPer100g = info.protein.takeIf { it > 0f }?.stripTrailingZeros().orEmpty(),
                        carbsPer100g = info.carbs.takeIf { it > 0f }?.stripTrailingZeros().orEmpty(),
                        fatPer100g = info.fat.takeIf { it > 0f }?.stripTrailingZeros().orEmpty(),
                        ocrSource = info.source.toDisplaySource(),
                        rawText = info.rawText,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRecognizing = false,
                        nutritionInfo = null,
                        ocrSource = "",
                        rawText = "",
                        errorMessage = error.message ?: "\u004f\u0043\u0052\u8bc6\u522b\u5931\u8d25"
                    )
                }
            )
        }
    }

    fun updateFoodName(text: String) {
        _uiState.value = _uiState.value.copy(foodName = text)
    }

    fun updateWeightGrams(text: String) {
        _uiState.value = _uiState.value.copy(weightGrams = text.filter(Char::isDigit).take(4))
    }

    fun updateCaloriesPer100g(text: String) {
        _uiState.value = _uiState.value.copy(caloriesPer100g = sanitizeDecimalInput(text))
    }

    fun updateProteinPer100g(text: String) {
        _uiState.value = _uiState.value.copy(proteinPer100g = sanitizeDecimalInput(text))
    }

    fun updateCarbsPer100g(text: String) {
        _uiState.value = _uiState.value.copy(carbsPer100g = sanitizeDecimalInput(text))
    }

    fun updateFatPer100g(text: String) {
        _uiState.value = _uiState.value.copy(fatPer100g = sanitizeDecimalInput(text))
    }

    fun updateMealType(mealType: MealType) {
        _uiState.value = _uiState.value.copy(mealType = mealType)
    }

    fun updateSaveAsFavorite(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(saveAsFavorite = enabled)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun buildPayload(): OcrNutritionPayload? {
        val state = _uiState.value
        val foodName = state.foodName.trim()
        val weight = state.weightGrams.toIntOrNull() ?: return null
        val calories = state.caloriesPer100g.toFloatOrNull() ?: 0f
        val protein = state.proteinPer100g.toFloatOrNull() ?: 0f
        val carbs = state.carbsPer100g.toFloatOrNull() ?: 0f
        val fat = state.fatPer100g.toFloatOrNull() ?: 0f
        val hasNutrition = calories > 0f || protein > 0f || carbs > 0f || fat > 0f
        if (foodName.isBlank() || weight <= 0 || !hasNutrition) return null

        return OcrNutritionPayload(
            foodName = foodName,
            weightGrams = weight,
            caloriesPer100g = calories,
            proteinPer100g = protein,
            carbsPer100g = carbs,
            fatPer100g = fat,
            source = state.ocrSource,
            rawText = state.rawText
        )
    }

    fun saveRecord() {
        val payload = buildPayload()
        if (payload == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "\u8bf7\u5148\u786e\u8ba4\u98df\u7269\u540d\u79f0\u3001\u514b\u6570\u548c\u8425\u517b\u503c"
            )
            return
        }

        val currentState = _uiState.value
        val record = FoodRecord(
            foodName = payload.foodName,
            userInput = payload.toDescription(),
            totalCalories = payload.totalCalories(),
            protein = payload.totalProtein(),
            carbs = payload.totalCarbs(),
            fat = payload.totalFat(),
            ingredients = listOf(
                Ingredient(
                    name = payload.foodName,
                    weight = "${payload.weightGrams}g",
                    calories = payload.totalCalories()
                )
            ),
            mealType = currentState.mealType,
            recordTime = resolveRecordTime(currentState),
            notes = currentState.ocrSource.ifBlank { null }
        )

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                foodRecordRepository.addRecord(record)
                if (currentState.saveAsFavorite) {
                    favoriteRecipeRepository.upsert(
                        FavoriteRecipe(
                            sourceRecordId = record.id,
                            foodName = record.foodName,
                            userInput = record.userInput,
                            totalCalories = record.totalCalories,
                            protein = record.protein,
                            carbs = record.carbs,
                            fat = record.fat
                        )
                    )
                }
                record.id
            }.onSuccess { recordId ->
                _uiState.value = _uiState.value.copy(isSaving = false)
                _events.tryEmit(
                    NutritionOcrImportEvent.RecordSaved(
                        recordId = recordId,
                        favorited = currentState.saveAsFavorite
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "\u4fdd\u5b58\u5931\u8d25"
                )
            }
        }
    }

    private fun resolveRecordTime(state: NutritionOcrImportUiState): Long {
        return if (state.isHistoricalDateMode) {
            buildRecordTimeForDateAndMeal(state.selectedDate, state.mealType)
        } else {
            System.currentTimeMillis()
        }
    }

    private fun sanitizeDecimalInput(text: String): String {
        val builder = StringBuilder()
        var hasDecimal = false
        text.forEach { char ->
            when {
                char.isDigit() -> builder.append(char)
                char == '.' && !hasDecimal -> {
                    hasDecimal = true
                    if (builder.isEmpty()) builder.append('0')
                    builder.append(char)
                }
            }
        }
        return builder.toString().take(8)
    }
}

data class NutritionOcrImportUiState(
    val isRecognizing: Boolean = false,
    val isSaving: Boolean = false,
    val imageUri: Uri? = null,
    val nutritionInfo: NutritionInfo? = null,
    val foodName: String = "",
    val weightGrams: String = "",
    val caloriesPer100g: String = "",
    val proteinPer100g: String = "",
    val carbsPer100g: String = "",
    val fatPer100g: String = "",
    val ocrSource: String = "",
    val rawText: String = "",
    val mealType: MealType = inferMainMealType(),
    val selectedDate: Long = System.currentTimeMillis(),
    val isHistoricalDateMode: Boolean = false,
    val saveAsFavorite: Boolean = false,
    val errorMessage: String? = null
)

sealed interface NutritionOcrImportEvent {
    data class RecordSaved(
        val recordId: String,
        val favorited: Boolean
    ) : NutritionOcrImportEvent
}

private fun Float.stripTrailingZeros(): String {
    val normalized = if (this < 0f) 0f else this
    val asLong = normalized.toLong()
    return if (normalized == asLong.toFloat()) {
        asLong.toString()
    } else {
        normalized.toString()
    }
}

private fun String.toDisplaySource(): String {
    return when {
        contains("paddleocr_pretrained", ignoreCase = true) && contains("ai_vision", ignoreCase = true) ->
            "\u0050\u0061\u0064\u0064\u006c\u0065\u004f\u0043\u0052\u0020\u002b\u0020\u0041\u0049\u6821\u9a8c"
        contains("local_service", ignoreCase = true) && contains("ai_vision", ignoreCase = true) ->
            "\u672c\u5730\u004f\u0043\u0052\u0020\u002b\u0020\u0041\u0049\u6821\u9a8c"
        contains("mlkit", ignoreCase = true) && contains("ai_vision", ignoreCase = true) ->
            "\u004d\u004c\u0020\u004b\u0069\u0074\u0020\u002b\u0020\u0041\u0049\u6821\u9a8c"
        equals("local_service", ignoreCase = true) ->
            "\u672c\u5730\u004f\u0043\u0052\u670d\u52a1"
        equals("mlkit", ignoreCase = true) ->
            "\u004d\u004c\u0020\u004b\u0069\u0074\uff08\u8bbe\u5907\u7aef\uff09"
        equals("paddleocr_pretrained", ignoreCase = true) ->
            "\u0050\u0061\u0064\u0064\u006c\u0065\u004f\u0043\u0052\u0020\u9884\u8bad\u7ec3"
        else -> ifBlank { "OCR" }
    }
}
