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
import com.calorieai.app.utils.inferMainMealType
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
    private var preferencesHydrated = false

    init {
        observeFavorites()
        observePantry()
        observePlans()
        observeUserSettings()
    }

    fun dispatch(action: RecipeAction.Home) {
        when (action) {
            is RecipeAction.Home.ChangeSelectedMealType -> {
                _uiState.update { current ->
                    current.copy(
                        selectedMealType = action.mealType,
                        quickFavorites = selectQuickFavorites(
                            favorites = current.favorites,
                            sourceMealTypeMap = current.sourceMealTypeMap,
                            selectedMealType = action.mealType
                        )
                    )
                }
            }
            is RecipeAction.Home.ChangeDietaryAllergens -> {
                _uiState.update {
                    it.copy(personalization = it.personalization.copy(dietaryAllergens = action.value))
                }
            }
            is RecipeAction.Home.ChangeFlavorPreferences -> {
                _uiState.update {
                    it.copy(personalization = it.personalization.copy(flavorPreferences = action.value))
                }
            }
            is RecipeAction.Home.ChangeBudgetPreference -> {
                _uiState.update {
                    it.copy(personalization = it.personalization.copy(budgetPreference = action.value))
                }
            }
            is RecipeAction.Home.ChangeMaxCookingMinutes -> {
                _uiState.update {
                    it.copy(
                        personalization = it.personalization.copy(
                            maxCookingMinutes = action.value.filter { ch -> ch.isDigit() }
                        )
                    )
                }
            }
            is RecipeAction.Home.ChangeSpecialPopulationMode -> {
                _uiState.update {
                    it.copy(personalization = it.personalization.copy(specialPopulationMode = action.value))
                }
            }
            is RecipeAction.Home.ChangeWeeklyRecordGoalDays -> {
                _uiState.update {
                    it.copy(
                        personalization = it.personalization.copy(
                            weeklyRecordGoalDays = action.value.filter { ch -> ch.isDigit() }
                        )
                    )
                }
            }
            is RecipeAction.Home.ClearAiResult -> {
                _uiState.update { it.copy(aiResult = null, aiError = null) }
            }
            is RecipeAction.Home.GenerateSuggestion -> generateRecipeSuggestion()
            is RecipeAction.Home.GeneratePlan -> generatePlan(action.days)
            is RecipeAction.Home.AddFavoriteToToday -> addFavoriteToToday(action.recipe, action.mealType)
            is RecipeAction.Home.SavePersonalization -> savePersonalizationSettings()
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteUseCase.observeFavorites().collectLatest { favorites ->
                val sourceMealTypeMap = favoriteUseCase.getSourceMealTypeMap(favorites)
                _uiState.update { current ->
                    current.copy(
                        favorites = favorites,
                        sourceMealTypeMap = sourceMealTypeMap,
                        quickFavorites = selectQuickFavorites(
                            favorites = favorites,
                            sourceMealTypeMap = sourceMealTypeMap,
                            selectedMealType = current.selectedMealType
                        )
                    )
                }
            }
        }
    }

    private fun selectQuickFavorites(
        favorites: List<FavoriteRecipe>,
        sourceMealTypeMap: Map<String, MealType>,
        selectedMealType: MealType
    ): List<FavoriteRecipe> {
        val sortedFavorites = favorites.sortedWith(
            compareByDescending<FavoriteRecipe> { it.lastUsedAt ?: 0L }
                .thenByDescending { it.useCount }
        )

        val filtered = sortedFavorites.filter { recipe ->
            val sourceMealType = sourceMealTypeMap[recipe.id]
            when (selectedMealType) {
                MealType.BREAKFAST -> sourceMealType == MealType.BREAKFAST
                MealType.LUNCH -> sourceMealType == MealType.LUNCH
                MealType.DINNER -> sourceMealType == MealType.DINNER
                MealType.SNACK -> sourceMealType in setOf(
                    MealType.SNACK,
                    MealType.BREAKFAST_SNACK,
                    MealType.LUNCH_SNACK,
                    MealType.DINNER_SNACK
                )
                MealType.BREAKFAST_SNACK -> sourceMealType == MealType.BREAKFAST_SNACK
                MealType.LUNCH_SNACK -> sourceMealType == MealType.LUNCH_SNACK
                MealType.DINNER_SNACK -> sourceMealType == MealType.DINNER_SNACK
            }
        }

        return (if (filtered.isNotEmpty()) filtered else sortedFavorites).take(6)
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
                if (!preferencesHydrated) {
                    _uiState.update {
                        it.copy(
                            personalization = RecipePersonalizationState.fromSettings(settings)
                        )
                    }
                    preferencesHydrated = true
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
            val personalization = _uiState.value.personalization
            val maxCooking = personalization.maxCookingMinutes.toIntOrNull()
            val weeklyGoal = personalization.weeklyRecordGoalDays.toIntOrNull() ?: 5
            userSettingsRepository.updateAIPersonalization(
                dietaryAllergens = personalization.dietaryAllergens,
                flavorPreferences = personalization.flavorPreferences,
                budgetPreference = personalization.budgetPreference,
                maxCookingMinutes = maxCooking,
                specialPopulationMode = personalization.specialPopulationMode,
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
                personalization = _uiState.value.personalization.toDomain()
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
                personalization = _uiState.value.personalization.toDomain(),
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
        return mealPlanUseCase.buildPantrySummary(buildRecipePantrySummaryLines(items))
    }
}

data class RecipeHomeUiState(
    val favorites: List<FavoriteRecipe> = emptyList(),
    val quickFavorites: List<FavoriteRecipe> = emptyList(),
    val sourceMealTypeMap: Map<String, MealType> = emptyMap(),
    val pantryIngredients: List<PantryIngredient> = emptyList(),
    val recipePlans: List<RecipePlan> = emptyList(),
    val selectedMealType: MealType = inferMainMealType(),
    val isAiLoading: Boolean = false,
    val aiResult: String? = null,
    val aiError: String? = null,
    val personalization: RecipePersonalizationState = RecipePersonalizationState()
)
