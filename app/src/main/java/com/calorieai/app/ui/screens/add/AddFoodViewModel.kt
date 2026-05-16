package com.calorieai.app.ui.screens.add

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.FoodTextAnalysisService
import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import com.calorieai.app.service.voice.VoiceInputHelper
import com.calorieai.app.service.voice.VoiceState
import com.calorieai.app.utils.buildRecordTimeForDateAndMeal
import com.calorieai.app.utils.inferMainMealType
import com.calorieai.app.utils.isSameLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val foodTextAnalysisService: FoodTextAnalysisService,
    private val aiTokenUsageRepository: com.calorieai.app.data.repository.AITokenUsageRepository,
    private val aiConfigRepository: com.calorieai.app.data.repository.AIConfigRepository,
    private val voiceInputHelper: VoiceInputHelper
) : ViewModel() {

    private companion object {
        const val MAX_FOOD_DESCRIPTION_LENGTH = 2000
    }

    private var lastAppliedOcrText: String? = null
    private var lastAppliedOcrPayloadJson: String? = null

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()
    val voiceState: StateFlow<VoiceState> = voiceInputHelper.voiceState

    fun onFoodDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            foodDescription = description.take(MAX_FOOD_DESCRIPTION_LENGTH)
        )
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.value = _uiState.value.copy(selectedMealType = mealType)
    }

    fun applyOcrText(ocrText: String) {
        val normalized = ocrText.trim()
        if (normalized.isBlank() || normalized == lastAppliedOcrText) return

        val currentText = _uiState.value.foodDescription.trim()
        val merged = if (currentText.isBlank()) {
            normalized
        } else {
            "$currentText\n$normalized"
        }

        _uiState.value = _uiState.value.copy(
            foodDescription = merged.take(MAX_FOOD_DESCRIPTION_LENGTH)
        )
        lastAppliedOcrText = normalized
    }

    fun applyOcrPayload(payload: OcrNutritionPayload) {
        val payloadJson = payload.toJson()
        if (payloadJson == lastAppliedOcrPayloadJson) return

        val description = payload.toDescription().take(MAX_FOOD_DESCRIPTION_LENGTH)
        _uiState.value = _uiState.value.copy(
            foodDescription = description,
            ocrPayload = payload
        )
        lastAppliedOcrPayloadJson = payloadJson
        lastAppliedOcrText = description
    }

    fun startVoiceInput(context: Context) {
        voiceInputHelper.startListening(
            context = context,
            onResult = { result ->
                val currentFoodDescription = _uiState.value.foodDescription
                val mergedText = if (currentFoodDescription.isBlank()) {
                    result
                } else {
                    "$currentFoodDescription $result"
                }
                onFoodDescriptionChange(mergedText.trim())
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun stopVoiceInput() {
        voiceInputHelper.stopListening()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, retryMessage = null)
    }

    fun clearRetryMessage() {
        _uiState.value = _uiState.value.copy(retryMessage = null)
    }

    fun setDateContext(dateStr: String?) {
        if (dateStr.isNullOrBlank()) {
            val now = System.currentTimeMillis()
            val autoMealType = inferMainMealType(now)
            _uiState.value = _uiState.value.copy(
                selectedDate = now,
                isHistoricalDateMode = false,
                autoMealType = autoMealType,
                selectedMealType = autoMealType
            )
            return
        }

        try {
            val parts = dateStr.split("-")
            if (parts.size != 3) {
                setDateContext(null)
                return
            }

            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, 12, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val selectedDateMillis = calendar.timeInMillis
            val now = System.currentTimeMillis()
            val autoMealType = inferMainMealType(now)
            val isHistoricalDateMode = !isSameLocalDate(selectedDateMillis, now)
            val currentState = _uiState.value

            _uiState.value = currentState.copy(
                selectedDate = selectedDateMillis,
                isHistoricalDateMode = isHistoricalDateMode,
                autoMealType = autoMealType,
                selectedMealType = if (isHistoricalDateMode) {
                    currentState.selectedMealType
                } else {
                    autoMealType
                }
            )
        } catch (_: Exception) {
            setDateContext(null)
        }
    }

    fun setSelectedDate(dateStr: String) {
        setDateContext(dateStr)
    }

    fun saveFoodRecord(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val description = _uiState.value.foodDescription
        if (description.isBlank()) {
            onError("请输入食物描述")
            return
        }
        if (description.length > MAX_FOOD_DESCRIPTION_LENGTH) {
            onError("输入内容过长，请控制在${MAX_FOOD_DESCRIPTION_LENGTH}字以内")
            return
        }

        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val autoMealType = inferMainMealType(now)
        val isHistoricalDateMode = currentState.isHistoricalDateMode
        val effectiveMealType = currentState.selectedMealType
        val baseRecordTime = if (isHistoricalDateMode) {
            buildRecordTimeForDateAndMeal(currentState.selectedDate, effectiveMealType)
        } else {
            now
        }
        val maxRetries = currentState.maxRetries.coerceAtLeast(0)

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null,
            retryMessage = null,
            retryAttempt = 0,
            autoMealType = autoMealType
        )

        viewModelScope.launch {
            try {
                val ocrPayload = currentState.ocrPayload
                if (ocrPayload != null) {
                    val record = FoodRecord(
                        foodName = ocrPayload.foodName,
                        userInput = description,
                        totalCalories = ocrPayload.totalCalories(),
                        protein = ocrPayload.totalProtein(),
                        carbs = ocrPayload.totalCarbs(),
                        fat = ocrPayload.totalFat(),
                        mealType = effectiveMealType,
                        recordTime = baseRecordTime
                    )
                    foodRecordRepository.addRecord(record)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(record.id)
                    return@launch
                }

                withContext(NonCancellable) {
                    val analysisResult = foodTextAnalysisService.analyzeFoodText(
                        foodDescription = description,
                        maxRetries = maxRetries,
                        onRetry = { attempt, maxAttempts ->
                            val totalRetries = (maxAttempts - 1).coerceAtLeast(0)
                            _uiState.value = _uiState.value.copy(
                                retryMessage = "结果不稳定，正在补救重试（${attempt}/${totalRetries}）...",
                                retryAttempt = attempt
                            )
                        }
                    )

                    if (analysisResult.isFailure) {
                        val error = analysisResult.exceptionOrNull()
                        val errorInfo = AIErrorClassifier.classify(error)
                        val uiMessage = when (errorInfo.category) {
                            AIErrorCategory.PARSE,
                            AIErrorCategory.VALIDATION -> "AI返回结果不稳定，请重试或调整描述后再试。"
                            else -> errorInfo.userMessage
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = uiMessage,
                            retryMessage = null
                        )
                        onError(uiMessage)
                        return@withContext
                    }

                    val batchResult = analysisResult.getOrNull()
                    val parsedItems = batchResult?.items ?: emptyList()
                    if (parsedItems.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "AI返回数据为空",
                            retryMessage = null
                        )
                        onError("AI返回数据为空")
                        return@withContext
                    }

                    val validItems = parsedItems.filter { item ->
                        item.foodName.isNotBlank() && (
                            item.calories > 0 ||
                                item.protein > 0 ||
                                item.carbs > 0 ||
                                item.fat > 0
                            )
                    }
                    if (validItems.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "无法识别食物，请尝试更详细的描述",
                            retryMessage = null
                        )
                        onError("无法识别食物")
                        return@withContext
                    }

                    val promptTokens = batchResult?.promptTokens ?: 0
                    val completionTokens = batchResult?.completionTokens ?: 0

                    if (promptTokens > 0 || completionTokens > 0) {
                        viewModelScope.launch {
                            try {
                                aiConfigRepository.getDefaultConfig().firstOrNull()?.let { config ->
                                    val cost = (promptTokens + completionTokens) * 0.000002
                                    aiTokenUsageRepository.recordTokenUsage(
                                        configId = config.id,
                                        configName = config.name,
                                        promptTokens = promptTokens,
                                        completionTokens = completionTokens,
                                        cost = cost
                                    )
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }

                    val records = validItems.mapIndexed { index, item ->
                        FoodRecord(
                            foodName = item.foodName.takeIf { it.isNotBlank() }
                                ?: extractFoodName(description, index),
                            userInput = description,
                            totalCalories = item.calories.toInt().coerceAtLeast(0),
                            protein = item.protein,
                            carbs = item.carbs,
                            fat = item.fat,
                            fiber = item.fiber,
                            sugar = item.sugar,
                            sodium = item.sodium,
                            cholesterol = item.cholesterol,
                            saturatedFat = item.saturatedFat,
                            calcium = item.calcium,
                            iron = item.iron,
                            vitaminC = item.vitaminC,
                            vitaminA = item.vitaminA,
                            potassium = item.potassium,
                            mealType = effectiveMealType,
                            recordTime = if (isHistoricalDateMode) {
                                buildRecordTimeForDateAndMeal(
                                    dateMillis = currentState.selectedDate,
                                    mealType = effectiveMealType,
                                    sequenceOffsetSeconds = index
                                )
                            } else {
                                baseRecordTime + index
                            }
                        )
                    }

                    records.forEach { record ->
                        foodRecordRepository.addRecord(record)
                    }

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(records.first().id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "发生错误: ${e.message}"
                )
                onError(e.message ?: "未知错误")
            }
        }
    }

    private fun extractFoodName(description: String, index: Int = 0): String {
        val separators = listOf("，", ",", "。", " ")
        var name = description

        for (separator in separators) {
            val parts = description.split(separator)
            val foodPart = parts.find { part ->
                part.contains(Regex("(吃|喝|个|份|片|块|碗|盘|杯|勺|包|盒|瓶|g|kg)"))
            }
            if (foodPart != null && foodPart.length < name.length) {
                name = foodPart
                break
            }
        }

        val cleanWords = listOf(
            "今天", "我", "吃", "喝", "一份", "一个", "一片", "一块", "一碗", "一点", "大约", "大概", "左右"
        )
        var cleanedName = name
        cleanWords.forEach { word ->
            cleanedName = cleanedName.replace(word, "")
        }

        val finalName = cleanedName.trim().takeIf { it.isNotBlank() } ?: name.trim()
        return finalName.take(20).takeIf { it.isNotBlank() } ?: "未命名食物${index + 1}"
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputHelper.stopListening()
    }
}

data class AddFoodUiState(
    val foodDescription: String = "",
    val selectedMealType: MealType = inferMainMealType(),
    val autoMealType: MealType = inferMainMealType(),
    val isHistoricalDateMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val retryMessage: String? = null,
    val retryAttempt: Int = 0,
    val maxRetries: Int = 1,
    val selectedDate: Long = System.currentTimeMillis(),
    val ocrPayload: OcrNutritionPayload? = null
)
