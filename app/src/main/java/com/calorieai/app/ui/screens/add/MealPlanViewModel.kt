package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.domain.recipe.MealPlanUseCase
import com.calorieai.app.domain.recipe.PantryUseCase
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
    private var preferencesHydrated = false

    init {
        viewModelScope.launch {
            mealPlanUseCase.observePlans().collectLatest { plans ->
                _uiState.update { it.copy(plans = plans) }
            }
        }
        viewModelScope.launch {
            userSettingsRepository.getSettings().collectLatest { settings ->
                settings ?: return@collectLatest
                if (!preferencesHydrated) {
                    _uiState.update {
                        it.copy(personalization = RecipePersonalizationState.fromSettings(settings))
                    }
                    preferencesHydrated = true
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
            RecipeAction.MealPlan.ClearAiResult -> {
                _uiState.update { it.copy(aiResult = null, aiError = null) }
            }
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
                personalization = _uiState.value.personalization.toDomain(),
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

    private fun buildPantrySummary(items: List<PantryIngredient>): String {
        return mealPlanUseCase.buildPantrySummary(buildRecipePantrySummaryLines(items))
    }
}

data class MealPlanUiState(
    val plans: List<RecipePlan> = emptyList(),
    val isGenerating: Boolean = false,
    val aiResult: String? = null,
    val aiError: String? = null,
    val personalization: RecipePersonalizationState = RecipePersonalizationState()
)
