package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipeGuide
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.data.repository.RecipeGuideRepository
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.service.ai.AIChatService
import com.calorieai.app.service.notification.PantryExpiryReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FavoriteRecipesViewModel @Inject constructor(
    private val favoriteRecipeRepository: FavoriteRecipeRepository,
    private val foodRecordRepository: FoodRecordRepository,
    private val pantryIngredientRepository: PantryIngredientRepository,
    private val recipeGuideRepository: RecipeGuideRepository,
    private val recipePlanRepository: RecipePlanRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val aiChatService: AIChatService,
    private val pantryExpiryReminderScheduler: PantryExpiryReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteRecipesUiState())
    val uiState: StateFlow<FavoriteRecipesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRecipeRepository.getAllFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
        viewModelScope.launch {
            pantryIngredientRepository.getAll().collect { items ->
                _uiState.update { it.copy(pantryIngredients = items) }
            }
        }
        viewModelScope.launch {
            recipeGuideRepository.getAll().collect { guides ->
                _uiState.update { it.copy(recipeGuides = guides) }
            }
        }
        viewModelScope.launch {
            recipePlanRepository.getAll().collect { plans ->
                _uiState.update { it.copy(recipePlans = plans) }
            }
        }
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.update { state ->
                        state.copy(
                            dietaryAllergens = it.dietaryAllergens.orEmpty(),
                            flavorPreferences = it.flavorPreferences.orEmpty(),
                            budgetPreference = it.budgetPreference.orEmpty(),
                            maxCookingMinutes = it.maxCookingMinutes?.toString().orEmpty(),
                            specialPopulationMode = it.specialPopulationMode,
                            weeklyRecordGoalDays = it.weeklyRecordGoalDays.toString()
                        )
                    }
                }
            }
        }
    }

    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun addFavoriteToToday(recipe: FavoriteRecipe, onDone: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val mealType = _uiState.value.selectedMealType
            val record = FoodRecord(
                foodName = recipe.foodName,
                userInput = recipe.userInput,
                totalCalories = recipe.totalCalories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat,
                fiber = recipe.fiber,
                sugar = recipe.sugar,
                sodium = recipe.sodium,
                cholesterol = recipe.cholesterol,
                saturatedFat = recipe.saturatedFat,
                calcium = recipe.calcium,
                iron = recipe.iron,
                vitaminC = recipe.vitaminC,
                vitaminA = recipe.vitaminA,
                potassium = recipe.potassium,
                mealType = mealType,
                recordTime = now
            )
            foodRecordRepository.addRecord(record)
            favoriteRecipeRepository.upsert(
                recipe.copy(
                    lastUsedAt = now,
                    useCount = recipe.useCount + 1
                )
            )
            onDone()
        }
    }

    fun removeFavorite(recipe: FavoriteRecipe) {
        viewModelScope.launch {
            favoriteRecipeRepository.delete(recipe)
        }
    }

    fun createGuideFromFavorite(recipe: FavoriteRecipe) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            recipeGuideRepository.upsert(
                RecipeGuide(
                    name = recipe.foodName,
                    ingredientsText = "主食材：${recipe.foodName}（按实际准备）",
                    stepsText = "1. 准备食材并清洗\n2. 按个人习惯烹饪\n3. 出锅后按份量分餐",
                    toolsText = "锅 / 刀 / 砧板",
                    difficulty = "中等",
                    durationMinutes = 30,
                    servings = 1,
                    calories = recipe.totalCalories,
                    protein = recipe.protein,
                    carbs = recipe.carbs,
                    fat = recipe.fat,
                    sourceType = "FAVORITE",
                    linkedFavoriteId = recipe.id,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun addPantryIngredient(
        name: String,
        quantity: Float,
        unit: String,
        daysToExpire: Int?,
        notes: String?
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expiresAt = daysToExpire
                ?.takeIf { it > 0 }
                ?.let { now + it * 24L * 60L * 60L * 1000L }

            val item = PantryIngredient(
                name = name.trim(),
                quantity = quantity,
                unit = unit.trim().ifBlank { "份" },
                expiresAt = expiresAt,
                notes = notes?.trim().takeUnless { it.isNullOrBlank() },
                createdAt = now,
                updatedAt = now
            )
            pantryIngredientRepository.upsert(item)
            pantryExpiryReminderScheduler.scheduleFor(item)
        }
    }

    fun removePantryIngredient(item: PantryIngredient) {
        viewModelScope.launch {
            pantryIngredientRepository.delete(item)
            pantryExpiryReminderScheduler.cancelFor(item.id)
        }
    }

    fun addRecipeGuide(
        name: String,
        ingredientsText: String,
        stepsText: String,
        toolsText: String,
        difficulty: String,
        durationMinutes: Int,
        servings: Int,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float
    ) {
        if (name.isBlank() || ingredientsText.isBlank() || stepsText.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            recipeGuideRepository.upsert(
                RecipeGuide(
                    name = name.trim(),
                    ingredientsText = ingredientsText.trim(),
                    stepsText = stepsText.trim(),
                    toolsText = toolsText.trim().ifBlank { "锅 / 刀 / 砧板" },
                    difficulty = difficulty.ifBlank { "中等" },
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    servings = servings.coerceAtLeast(1),
                    calories = calories.coerceAtLeast(0),
                    protein = protein.coerceAtLeast(0f),
                    carbs = carbs.coerceAtLeast(0f),
                    fat = fat.coerceAtLeast(0f),
                    sourceType = "MANUAL",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun removeRecipeGuide(item: RecipeGuide) {
        viewModelScope.launch {
            recipeGuideRepository.delete(item)
        }
    }

    fun saveRecipePlan(
        title: String,
        startDate: LocalDate,
        days: Int,
        menuText: String,
        generatedByAI: Boolean
    ) {
        if (title.isBlank() || menuText.isBlank()) return
        viewModelScope.launch {
            val safeDays = days.coerceAtLeast(1)
            val endDate = startDate.plusDays((safeDays - 1).toLong())
            val now = System.currentTimeMillis()
            recipePlanRepository.upsert(
                RecipePlan(
                    title = title.trim(),
                    startDateEpochDay = startDate.toEpochDay(),
                    endDateEpochDay = endDate.toEpochDay(),
                    menuText = menuText.trim(),
                    generatedByAI = generatedByAI,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun removeRecipePlan(item: RecipePlan) {
        viewModelScope.launch {
            recipePlanRepository.delete(item)
        }
    }

    fun clearAiResult() {
        _uiState.update { it.copy(aiResult = null, aiError = null) }
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

    fun savePersonalizationSettings() {
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

            _uiState.update { it.copy(saveMessage = "个性化忌口与偏好已保存") }
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }

    fun generateRecipeSuggestionByPantry() {
        viewModelScope.launch {
            val pantry = pantryIngredientRepository.getAllOnce()
            _uiState.update { it.copy(isAiLoading = true, aiError = null) }

            try {
                val response = aiChatService.recommendRecipesWithPantry(
                    buildRecipeRequestContext(items = pantry)
                )
                _uiState.update { it.copy(isAiLoading = false, aiResult = response, aiError = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiError = e.message ?: "生成失败，请稍后重试"
                    )
                }
            }
        }
    }

    fun generatePlanByPantry(days: Int = 3, startDate: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val pantry = pantryIngredientRepository.getAllOnce()
            _uiState.update { it.copy(isAiLoading = true, aiError = null) }

            try {
                val safeDays = days.coerceIn(1, 14)
                val response = aiChatService.generatePantryMealPlan(
                    pantrySummary = buildRecipeRequestContext(items = pantry),
                    days = safeDays
                )
                saveRecipePlan(
                    title = "AI生成${safeDays}天菜单",
                    startDate = startDate,
                    days = safeDays,
                    menuText = response,
                    generatedByAI = true
                )
                _uiState.update { it.copy(isAiLoading = false, aiResult = response, aiError = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiError = e.message ?: "生成失败，请稍后重试"
                    )
                }
            }
        }
    }

    private fun buildRecipeRequestContext(items: List<PantryIngredient>): String {
        val state = _uiState.value
        val pantrySummary = buildPantrySummary(items)
        val modeLabel = when (state.specialPopulationMode) {
            "DIABETES" -> "控糖"
            "GOUT" -> "痛风"
            "PREGNANCY" -> "孕期"
            "CHILD" -> "儿童"
            "FITNESS" -> "健身"
            else -> "通用健康"
        }
        return """
【本地食材信息】
$pantrySummary

【已保存的饮食偏好】
- 过敏原/忌口：${state.dietaryAllergens.ifBlank { "未填写" }}
- 口味偏好：${state.flavorPreferences.ifBlank { "未填写" }}
- 预算偏好：${state.budgetPreference.ifBlank { "未填写" }}
- 烹饪时长上限：${state.maxCookingMinutes.ifBlank { "未填写" }} 分钟
- 特定人群模式：$modeLabel
- 每周记录目标：${state.weeklyRecordGoalDays.ifBlank { "5" }} 天
        """.trimIndent()
    }

    private fun buildPantrySummary(items: List<PantryIngredient>): String {
        if (items.isEmpty()) {
            return "暂无本地食材库存。请直接基于近期健康数据与饮食偏好推荐菜谱，并附可选采购清单。"
        }
        val now = System.currentTimeMillis()
        return items.joinToString(separator = "\n") { item ->
            val expireInfo = item.expiresAt?.let {
                val days = ((it - now) / (24f * 60f * 60f * 1000f)).toInt()
                val text = if (days >= 0) "${days}天后过期" else "已过期"
                "（$text）"
            } ?: ""
            "- ${item.name} ${item.quantity}${item.unit}$expireInfo"
        }
    }
}

data class FavoriteRecipesUiState(
    val favorites: List<FavoriteRecipe> = emptyList(),
    val selectedMealType: MealType = MealType.LUNCH,
    val pantryIngredients: List<PantryIngredient> = emptyList(),
    val recipeGuides: List<RecipeGuide> = emptyList(),
    val recipePlans: List<RecipePlan> = emptyList(),
    val isAiLoading: Boolean = false,
    val aiResult: String? = null,
    val aiError: String? = null,
    val dietaryAllergens: String = "",
    val flavorPreferences: String = "",
    val budgetPreference: String = "",
    val maxCookingMinutes: String = "",
    val specialPopulationMode: String = "GENERAL",
    val weeklyRecordGoalDays: String = "5",
    val saveMessage: String? = null
)
