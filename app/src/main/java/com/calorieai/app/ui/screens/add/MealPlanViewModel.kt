package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.domain.recipe.MealPlanUseCase
import com.calorieai.app.domain.recipe.PantryUseCase
import com.calorieai.app.domain.recipe.RecipePersonalization
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanUseCase: MealPlanUseCase,
    private val pantryUseCase: PantryUseCase,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeUiEvent>()
    val events: SharedFlow<RecipeUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            mealPlanUseCase.observePlans().collectLatest { plans ->
                _uiState.update { it.copy(plans = plans) }
            }
        }
        viewModelScope.launch {
            userSettingsRepository.getSettings().collectLatest { settings ->
                settings ?: return@collectLatest
                _uiState.update {
                    it.copy(
                        dietaryAllergens = settings.dietaryAllergens.orEmpty(),
                        flavorPreferences = settings.flavorPreferences.orEmpty(),
                        budgetPreference = settings.budgetPreference.orEmpty(),
                        maxCookingMinutes = settings.maxCookingMinutes?.toString().orEmpty(),
                        specialPopulationMode = settings.specialPopulationMode,
                        weeklyRecordGoalDays = settings.weeklyRecordGoalDays.toString()
                    )
                }
            }
        }
    }

    fun dispatch(action: RecipeAction.MealPlan) {
        when (action) {
            is RecipeAction.MealPlan.SaveManualPlan -> saveManualPlan(
                title = action.title,
                startDate = action.startDate,
                days = action.days,
                menuText = action.menuText
            )
            is RecipeAction.MealPlan.DeletePlan -> deletePlan(action.item)
            is RecipeAction.MealPlan.GenerateByAi -> generateByAi(action.days, action.startDate)
        }
    }

    private fun saveManualPlan(
        title: String,
        startDate: LocalDate,
        days: Int,
        menuText: String
    ) {
        viewModelScope.launch {
            mealPlanUseCase.savePlan(
                title = title,
                startDate = startDate,
                days = days,
                menuText = menuText,
                generatedByAI = false
            ).onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("菜单已保存"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "保存失败"))
            }
        }
    }

    private fun deletePlan(item: RecipePlan) {
        viewModelScope.launch {
            runCatching {
                mealPlanUseCase.removePlan(item)
            }.onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已删除菜单：${item.title}"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "删除失败"))
            }
        }
    }

    private fun generateByAi(days: Int, startDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, aiResult = null, aiError = null) }
            val pantryItems = pantryUseCase.getPantryOnce()
            val pantrySummary = buildPantrySummary(pantryItems)
            mealPlanUseCase.generateAndSavePlan(
                pantrySummary = pantrySummary,
                personalization = _uiState.value.toPersonalization(),
                days = days,
                startDate = startDate
            ).onSuccess { result ->
                _uiState.update { it.copy(isGenerating = false, aiResult = result, aiError = null) }
                _events.emit(RecipeUiEvent.Snackbar("AI 菜单生成成功并已保存"))
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isGenerating = false,
                        aiError = it.message ?: "AI 生成失败"
                    )
                }
            }
        }
    }

    fun clearAiResult() {
        _uiState.update { it.copy(aiResult = null, aiError = null) }
    }

    private fun buildPantrySummary(items: List<PantryIngredient>): String {
        val now = System.currentTimeMillis()
        return mealPlanUseCase.buildPantrySummary(
            items.map { item ->
                val expireInfo = item.expiresAt?.let {
                    val days = ((it - now) / (24f * 60f * 60f * 1000f)).toInt()
                    val text = if (days >= 0) "${days}天后过期" else "已过期"
                    "（$text）"
                } ?: ""
                "- ${item.name} ${item.quantity}${item.unit}$expireInfo"
            }
        )
    }
}

private fun MealPlanUiState.toPersonalization(): RecipePersonalization {
    return RecipePersonalization(
        dietaryAllergens = dietaryAllergens,
        flavorPreferences = flavorPreferences,
        budgetPreference = budgetPreference,
        maxCookingMinutes = maxCookingMinutes,
        specialPopulationMode = specialPopulationMode,
        weeklyRecordGoalDays = weeklyRecordGoalDays
    )
}

data class MealPlanUiState(
    val plans: List<RecipePlan> = emptyList(),
    val isGenerating: Boolean = false,
    val aiResult: String? = null,
    val aiError: String? = null,
    val dietaryAllergens: String = "",
    val flavorPreferences: String = "",
    val budgetPreference: String = "",
    val maxCookingMinutes: String = "",
    val specialPopulationMode: String = "GENERAL",
    val weeklyRecordGoalDays: String = "5"
)
