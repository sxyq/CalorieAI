package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.domain.recipe.FavoriteUseCase
import com.calorieai.app.domain.recipe.MealPlanUseCase
import com.calorieai.app.domain.recipe.PantryUseCase
import com.calorieai.app.domain.recipe.RecipePersonalization
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeHomeViewModel @Inject constructor(
    private val favoriteUseCase: FavoriteUseCase,
    private val pantryUseCase: PantryUseCase,
    private val mealPlanUseCase: MealPlanUseCase,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeHomeUiState())
    val uiState: StateFlow<RecipeHomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeUiEvent>()
    val events: SharedFlow<RecipeUiEvent> = _events.asSharedFlow()

    init {
        observeFavorites()
        observePantry()
        observePlans()
        observeUserSettings()
    }

    fun dispatch(action: RecipeAction.Home) {
        when (action) {
            is RecipeAction.Home.GenerateSuggestion -> generateRecipeSuggestion()
            is RecipeAction.Home.GeneratePlan -> generatePlan(action.days)
            is RecipeAction.Home.AddFavoriteToToday -> addFavoriteToToday(action.recipe, action.mealType)
            is RecipeAction.Home.SavePersonalization -> savePersonalizationSettings()
        }
    }

    fun setSelectedMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun onDietaryAllergensChange(value: String) {
        _uiState.update { it.copy(dietaryAllergens = value) }
    }

    fun onFlavorPreferencesChange(value: String) {
        _uiState.update { it.copy(flavorPreferences = value) }
    }

    fun onBudgetPreferenceChange(value: String) {
        _uiState.update { it.copy(budgetPreference = value) }
    }

    fun onMaxCookingMinutesChange(value: String) {
        _uiState.update { it.copy(maxCookingMinutes = value.filter { ch -> ch.isDigit() }) }
    }

    fun onSpecialPopulationModeChange(value: String) {
        _uiState.update { it.copy(specialPopulationMode = value) }
    }

    fun onWeeklyRecordGoalDaysChange(value: String) {
        _uiState.update { it.copy(weeklyRecordGoalDays = value.filter { ch -> ch.isDigit() }) }
    }

    fun clearAiResult() {
        _uiState.update { it.copy(aiResult = null, aiError = null) }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteUseCase.observeFavorites().collectLatest { favorites ->
                _uiState.update {
                    it.copy(
                        favorites = favorites,
                        quickFavorites = favorites
                            .sortedWith(compareByDescending<FavoriteRecipe> { it.lastUsedAt ?: 0L }
                                .thenByDescending { it.useCount })
                            .take(6)
                    )
                }
            }
        }
    }

    private fun observePantry() {
        viewModelScope.launch {
            pantryUseCase.observePantry().collectLatest { items ->
                _uiState.update { it.copy(pantryIngredients = items) }
            }
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            mealPlanUseCase.observePlans().collectLatest { plans ->
                _uiState.update { it.copy(recipePlans = plans) }
            }
        }
    }

    private fun observeUserSettings() {
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

    private fun addFavoriteToToday(recipe: FavoriteRecipe, mealType: MealType) {
        viewModelScope.launch {
            runCatching {
                favoriteUseCase.addFavoriteToToday(recipe, mealType)
            }.onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已加入今日记录：${recipe.foodName}"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "加入失败"))
            }
        }
    }

    private fun savePersonalizationSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val maxCooking = state.maxCookingMinutes.toIntOrNull()
            val weeklyGoal = state.weeklyRecordGoalDays.toIntOrNull() ?: 5
            userSettingsRepository.updateAIPersonalization(
                dietaryAllergens = state.dietaryAllergens,
                flavorPreferences = state.flavorPreferences,
                budgetPreference = state.budgetPreference,
                maxCookingMinutes = maxCooking,
                specialPopulationMode = state.specialPopulationMode,
                weeklyRecordGoalDays = weeklyGoal
            )
            _events.emit(RecipeUiEvent.Snackbar("个性化忌口与偏好已保存"))
        }
    }

    private fun generateRecipeSuggestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiError = null) }
            val pantrySummary = buildPantrySummary(_uiState.value.pantryIngredients)
            val result = mealPlanUseCase.generateRecipeSuggestion(
                pantrySummary = pantrySummary,
                personalization = _uiState.value.toPersonalization()
            )
            result.onSuccess { text ->
                _uiState.update { it.copy(isAiLoading = false, aiResult = text, aiError = null) }
            }.onFailure {
                val errorMessage = it.message ?: "生成失败，请稍后重试"
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiError = errorMessage
                    )
                }
            }
        }
    }

    private fun generatePlan(days: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiError = null) }
            val pantrySummary = buildPantrySummary(_uiState.value.pantryIngredients)
            val result = mealPlanUseCase.generateAndSavePlan(
                pantrySummary = pantrySummary,
                personalization = _uiState.value.toPersonalization(),
                days = days,
                startDate = java.time.LocalDate.now()
            )
            result.onSuccess { text ->
                _uiState.update { it.copy(isAiLoading = false, aiResult = text, aiError = null) }
                _events.emit(RecipeUiEvent.Snackbar("AI 菜单已生成并保存"))
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiError = error.message ?: "生成失败，请稍后重试"
                    )
                }
            }
        }
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

private fun RecipeHomeUiState.toPersonalization(): RecipePersonalization {
    return RecipePersonalization(
        dietaryAllergens = dietaryAllergens,
        flavorPreferences = flavorPreferences,
        budgetPreference = budgetPreference,
        maxCookingMinutes = maxCookingMinutes,
        specialPopulationMode = specialPopulationMode,
        weeklyRecordGoalDays = weeklyRecordGoalDays
    )
}

data class RecipeHomeUiState(
    val favorites: List<FavoriteRecipe> = emptyList(),
    val quickFavorites: List<FavoriteRecipe> = emptyList(),
    val pantryIngredients: List<PantryIngredient> = emptyList(),
    val recipePlans: List<RecipePlan> = emptyList(),
    val selectedMealType: MealType = MealType.LUNCH,
    val isAiLoading: Boolean = false,
    val aiResult: String? = null,
    val aiError: String? = null,
    val dietaryAllergens: String = "",
    val flavorPreferences: String = "",
    val budgetPreference: String = "",
    val maxCookingMinutes: String = "",
    val specialPopulationMode: String = "GENERAL",
    val weeklyRecordGoalDays: String = "5"
)
