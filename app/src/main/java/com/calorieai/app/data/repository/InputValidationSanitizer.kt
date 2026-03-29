package com.calorieai.app.data.repository

import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord

private const val MAX_NAME = 64
private const val MAX_SHORT_TEXT = 128
private const val MAX_MEDIUM_TEXT = 512
private const val MAX_LONG_TEXT = 2000
private const val MAX_JSON_TEXT = 10000
private const val MAX_URL = 1024
private const val MAX_API_KEY = 2048

private fun sanitize(raw: String?, max: Int): String? {
    val normalized = raw
        ?.replace('\u0000', ' ')
        ?.trim()
        ?.take(max)
    return normalized?.takeIf { it.isNotBlank() }
}

private fun Float.safe(min: Float, max: Float): Float {
    if (isNaN() || isInfinite()) return min
    return coerceIn(min, max)
}

private fun Int.safe(min: Int, max: Int): Int {
    return coerceIn(min, max)
}

fun sanitizeFoodRecord(record: FoodRecord): FoodRecord {
    return record.copy(
        foodName = sanitize(record.foodName, MAX_NAME) ?: "未命名食物",
        userInput = sanitize(record.userInput, MAX_LONG_TEXT) ?: "",
        totalCalories = record.totalCalories.safe(0, 20000),
        protein = record.protein.safe(0f, 1000f),
        carbs = record.carbs.safe(0f, 1000f),
        fat = record.fat.safe(0f, 1000f),
        fiber = record.fiber.safe(0f, 500f),
        sugar = record.sugar.safe(0f, 500f),
        sodium = record.sodium.safe(0f, 20000f),
        cholesterol = record.cholesterol.safe(0f, 5000f),
        saturatedFat = record.saturatedFat.safe(0f, 500f),
        transFat = record.transFat.safe(0f, 500f),
        calcium = record.calcium.safe(0f, 10000f),
        iron = record.iron.safe(0f, 1000f),
        vitaminC = record.vitaminC.safe(0f, 10000f),
        vitaminA = record.vitaminA.safe(0f, 100000f),
        potassium = record.potassium.safe(0f, 10000f),
        notes = sanitize(record.notes, MAX_LONG_TEXT)
    )
}

fun sanitizeFavoriteRecipe(recipe: FavoriteRecipe): FavoriteRecipe {
    return recipe.copy(
        sourceRecordId = sanitize(recipe.sourceRecordId, MAX_MEDIUM_TEXT) ?: recipe.sourceRecordId.take(MAX_MEDIUM_TEXT),
        foodName = sanitize(recipe.foodName, MAX_NAME) ?: "未命名食物",
        userInput = sanitize(recipe.userInput, MAX_LONG_TEXT) ?: "",
        totalCalories = recipe.totalCalories.safe(0, 20000),
        protein = recipe.protein.safe(0f, 1000f),
        carbs = recipe.carbs.safe(0f, 1000f),
        fat = recipe.fat.safe(0f, 1000f),
        fiber = recipe.fiber.safe(0f, 500f),
        sugar = recipe.sugar.safe(0f, 500f),
        sodium = recipe.sodium.safe(0f, 20000f),
        cholesterol = recipe.cholesterol.safe(0f, 5000f),
        saturatedFat = recipe.saturatedFat.safe(0f, 500f),
        calcium = recipe.calcium.safe(0f, 10000f),
        iron = recipe.iron.safe(0f, 1000f),
        vitaminC = recipe.vitaminC.safe(0f, 10000f),
        vitaminA = recipe.vitaminA.safe(0f, 100000f),
        potassium = recipe.potassium.safe(0f, 10000f),
        recipeIngredientsText = sanitize(recipe.recipeIngredientsText, MAX_LONG_TEXT),
        recipeStepsText = sanitize(recipe.recipeStepsText, MAX_LONG_TEXT),
        recipeToolsText = sanitize(recipe.recipeToolsText, MAX_MEDIUM_TEXT),
        recipeDifficulty = sanitize(recipe.recipeDifficulty, MAX_SHORT_TEXT),
        recipeDurationMinutes = recipe.recipeDurationMinutes?.safe(1, 24 * 60),
        recipeServings = recipe.recipeServings?.safe(1, 100),
        recipeSourceType = sanitize(recipe.recipeSourceType, MAX_SHORT_TEXT),
        useCount = recipe.useCount.safe(0, 1_000_000)
    )
}

fun sanitizeExerciseRecord(record: ExerciseRecord): ExerciseRecord {
    return record.copy(
        durationMinutes = record.durationMinutes.safe(1, 24 * 60),
        caloriesBurned = record.caloriesBurned.safe(0, 20000),
        notes = sanitize(record.notes, MAX_LONG_TEXT)
    )
}

fun sanitizePantryIngredient(item: PantryIngredient): PantryIngredient {
    return item.copy(
        name = sanitize(item.name, MAX_NAME) ?: "未命名食材",
        quantity = item.quantity.safe(0f, 100000f),
        unit = sanitize(item.unit, MAX_SHORT_TEXT) ?: "份",
        notes = sanitize(item.notes, MAX_LONG_TEXT)
    )
}

fun sanitizeWeightRecord(record: WeightRecord): WeightRecord {
    return record.copy(
        weight = record.weight.safe(1f, 500f),
        note = sanitize(record.note, MAX_LONG_TEXT)
    )
}

fun sanitizeWaterRecord(record: WaterRecord): WaterRecord {
    return record.copy(
        amount = record.amount.safe(1, 10000),
        note = sanitize(record.note, MAX_LONG_TEXT)
    )
}

fun sanitizeAIConfig(config: AIConfig): AIConfig {
    return config.copy(
        name = sanitize(config.name, MAX_NAME) ?: "未命名配置",
        icon = sanitize(config.icon, MAX_SHORT_TEXT) ?: config.icon.take(MAX_SHORT_TEXT),
        apiUrl = sanitize(config.apiUrl, MAX_URL) ?: "",
        apiKey = sanitize(config.apiKey, MAX_API_KEY) ?: "",
        modelId = sanitize(config.modelId, MAX_MEDIUM_TEXT) ?: ""
    )
}

fun sanitizeUserSettings(settings: UserSettings): UserSettings {
    return settings.copy(
        userName = sanitize(settings.userName, MAX_NAME),
        userId = sanitize(settings.userId, MAX_SHORT_TEXT),
        userGender = sanitize(settings.userGender, MAX_SHORT_TEXT),
        dietaryPreference = sanitize(settings.dietaryPreference, MAX_MEDIUM_TEXT),
        dietaryAllergens = sanitize(settings.dietaryAllergens, MAX_MEDIUM_TEXT),
        flavorPreferences = sanitize(settings.flavorPreferences, MAX_MEDIUM_TEXT),
        budgetPreference = sanitize(settings.budgetPreference, MAX_SHORT_TEXT),
        specialPopulationMode = sanitize(settings.specialPopulationMode, MAX_SHORT_TEXT) ?: "GENERAL",
        breakfastReminderTime = sanitize(settings.breakfastReminderTime, 5) ?: "08:00",
        lunchReminderTime = sanitize(settings.lunchReminderTime, 5) ?: "12:00",
        dinnerReminderTime = sanitize(settings.dinnerReminderTime, 5) ?: "18:00",
        selectedAIPresetId = sanitize(settings.selectedAIPresetId, MAX_SHORT_TEXT),
        customAIEndpoint = sanitize(settings.customAIEndpoint, MAX_URL),
        customAIModel = sanitize(settings.customAIModel, MAX_MEDIUM_TEXT),
        themeMode = sanitize(settings.themeMode, MAX_SHORT_TEXT) ?: "SYSTEM",
        fontSize = sanitize(settings.fontSize, MAX_SHORT_TEXT) ?: "MEDIUM",
        feedbackType = sanitize(settings.feedbackType, MAX_SHORT_TEXT) ?: "BOTH",
        backgroundBehavior = sanitize(settings.backgroundBehavior, MAX_SHORT_TEXT) ?: "STANDARD",
        startupPage = sanitize(settings.startupPage, MAX_SHORT_TEXT) ?: "HOME",
        lastBackupTime = sanitize(settings.lastBackupTime, MAX_SHORT_TEXT),
        wallpaperType = sanitize(settings.wallpaperType, MAX_SHORT_TEXT) ?: "SOLID",
        wallpaperColor = sanitize(settings.wallpaperColor, MAX_SHORT_TEXT),
        wallpaperGradientStart = sanitize(settings.wallpaperGradientStart, MAX_SHORT_TEXT),
        wallpaperGradientEnd = sanitize(settings.wallpaperGradientEnd, MAX_SHORT_TEXT),
        wallpaperImageUri = sanitize(settings.wallpaperImageUri, MAX_URL),
        onboardingDataJson = sanitize(settings.onboardingDataJson, MAX_JSON_TEXT),
        goalType = sanitize(settings.goalType, MAX_SHORT_TEXT),
        weightLossStrategy = sanitize(settings.weightLossStrategy, MAX_SHORT_TEXT),
        exerciseHabitsJson = sanitize(settings.exerciseHabitsJson, MAX_JSON_TEXT),
        userAvatarUri = sanitize(settings.userAvatarUri, MAX_URL),
        dailyCalorieGoal = settings.dailyCalorieGoal.safe(800, 10000),
        userAge = settings.userAge?.safe(1, 120),
        userHeight = settings.userHeight?.safe(30f, 300f),
        userWeight = settings.userWeight?.safe(1f, 500f),
        maxCookingMinutes = settings.maxCookingMinutes?.safe(1, 24 * 60),
        weeklyRecordGoalDays = settings.weeklyRecordGoalDays.safe(1, 7),
        dailyWaterGoal = settings.dailyWaterGoal.safe(500, 10000),
        onboardingCurrentStep = settings.onboardingCurrentStep.safe(1, 10),
        estimatedWeeksToGoal = settings.estimatedWeeksToGoal?.safe(1, 520),
        weeklyWeightChangeGoal = settings.weeklyWeightChangeGoal?.safe(0f, 5f),
        targetWeight = settings.targetWeight?.safe(1f, 500f),
        bmr = settings.bmr?.safe(500, 10000),
        tdee = settings.tdee?.safe(500, 15000),
        bmi = settings.bmi?.safe(5f, 100f),
        birthDate = settings.birthDate
    )
}
