package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.service.ai.FoodTextAnalysisService
import com.calorieai.app.data.model.FoodAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val foodTextAnalysisService: FoodTextAnalysisService,
    private val aiTokenUsageRepository: com.calorieai.app.data.repository.AITokenUsageRepository,
    private val aiConfigRepository: com.calorieai.app.data.repository.AIConfigRepository
) : ViewModel() {
    private companion object {
        const val MAX_FOOD_DESCRIPTION_LENGTH = 2000
    }

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    fun onFoodDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            foodDescription = description.take(MAX_FOOD_DESCRIPTION_LENGTH)
        )
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.value = _uiState.value.copy(selectedMealType = mealType)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, retryMessage = null)
    }
    
    fun clearRetryMessage() {
        _uiState.value = _uiState.value.copy(retryMessage = null)
    }

    fun setSelectedDate(dateStr: String) {
        try {
            // 解析日期字符串 (格式: yyyy-MM-dd)
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1 // Calendar月份从0开始
                val day = parts[2].toInt()
                
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, 12, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                _uiState.value = _uiState.value.copy(selectedDate = calendar.timeInMillis)
            }
        } catch (e: Exception) {
            // 解析失败，使用当前时间
            _uiState.value = _uiState.value.copy(selectedDate = System.currentTimeMillis())
        }
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

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, retryMessage = null, retryAttempt = 0)
        val maxRetries = _uiState.value.maxRetries.coerceAtLeast(0)

        viewModelScope.launch {
            try {
                // 使用 NonCancellable 确保切后台时分析不中断
                withContext(NonCancellable) {
                    // 调用AI服务分析食物，带重试机制
                    val analysisResult = foodTextAnalysisService.analyzeFoodText(
                        foodDescription = description,
                        maxRetries = maxRetries,
                        onRetry = { attempt, maxAttempts ->
                            val totalRetries = (maxAttempts - 1).coerceAtLeast(0)
                            _uiState.value = _uiState.value.copy(
                                retryMessage = "AI返回数据不完整，正在重试（${attempt}/${totalRetries}）...",
                                retryAttempt = attempt
                            )
                        }
                    )

                    if (analysisResult.isFailure) {
                        val error = analysisResult.exceptionOrNull()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error?.message ?: "AI分析失败，请检查网络连接或AI配置",
                            retryMessage = null
                        )
                        onError(error?.message ?: "AI分析失败")
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

                    // 过滤无效条目，确保多食材拆分后每条都可入库
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

                    // 记录Token使用情况（在单独的协程中，避免阻塞主流程）
                    if (promptTokens > 0 || completionTokens > 0) {
                        viewModelScope.launch {
                            try {
                                aiConfigRepository.getDefaultConfig().firstOrNull()?.let { config ->
                                    // 估算成本（简化计算，实际应根据模型价格）
                                    val cost = (promptTokens + completionTokens) * 0.000002 // 假设每1000 tokens $0.002
                                    aiTokenUsageRepository.recordTokenUsage(
                                        configId = config.id,
                                        configName = config.name,
                                        promptTokens = promptTokens,
                                        completionTokens = completionTokens,
                                        cost = cost
                                    )
                                }
                            } catch (e: Exception) {
                                // 记录token失败不影响主流程
                            }
                        }
                    }

                    // 多食材逐条保存，首页将展示分开的记录
                    val records = validItems.mapIndexed { index, item ->
                        FoodRecord(
                            foodName = item.foodName.takeIf { it.isNotBlank() } ?: extractFoodName(description, index),
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
                            mealType = _uiState.value.selectedMealType,
                            recordTime = _uiState.value.selectedDate + index
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
        return finalName.take(20).takeIf { it.isNotBlank() } ?: "未命名食物${index + 1}"
    }
}

data class AddFoodUiState(
    val foodDescription: String = "",
    val selectedMealType: MealType = MealType.LUNCH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val retryMessage: String? = null,  // 重试提示信息
    val retryAttempt: Int = 0,  // 当前重试次数
    val maxRetries: Int = 2,  // 最大重试次数
    val selectedDate: Long = System.currentTimeMillis()  // 选中的日期
)
