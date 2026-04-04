package com.calorieai.app.service.backup

import com.calorieai.app.data.model.AIChatHistory
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings

internal fun FoodRecord.toBackup() = FoodRecordBackup(
    id = id,
    foodName = foodName,
    userInput = userInput,
    totalCalories = totalCalories,
    protein = protein,
    fat = fat,
    carbs = carbs,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    cholesterol = cholesterol,
    saturatedFat = saturatedFat,
    calcium = calcium,
    iron = iron,
    vitaminC = vitaminC,
    vitaminA = vitaminA,
    potassium = potassium,
    ingredients = ingredients.map {
        IngredientBackup(
            name = it.name,
            weight = it.weight,
            calories = it.calories
        )
    },
    mealType = mealType.name,
    recordTime = recordTime,
    iconUrl = iconUrl,
    iconLocalPath = iconLocalPath,
    isStarred = isStarred,
    confidence = confidence.name,
    notes = notes
)

internal fun com.calorieai.app.data.model.ExerciseRecord.toBackup() = ExerciseRecordBackup(
    id = id,
    exerciseType = exerciseType.name,
    durationMinutes = durationMinutes,
    caloriesBurned = caloriesBurned,
    notes = notes,
    recordTime = recordTime
)

internal fun com.calorieai.app.data.model.WeightRecord.toBackup() = WeightRecordBackup(
    id = id,
    weight = weight,
    recordDate = recordDate,
    note = note
)

internal fun com.calorieai.app.data.model.WaterRecord.toBackup() = WaterRecordBackup(
    id = id,
    amount = amount,
    recordTime = recordTime,
    recordDate = recordDate,
    note = note
)

internal fun FavoriteRecipe.toBackup() = FavoriteRecipeBackup(
    id = id,
    sourceRecordId = sourceRecordId,
    foodName = foodName,
    userInput = userInput,
    totalCalories = totalCalories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    cholesterol = cholesterol,
    saturatedFat = saturatedFat,
    calcium = calcium,
    iron = iron,
    vitaminC = vitaminC,
    vitaminA = vitaminA,
    potassium = potassium,
    recipeIngredientsText = recipeIngredientsText,
    recipeStepsText = recipeStepsText,
    recipeToolsText = recipeToolsText,
    recipeDifficulty = recipeDifficulty,
    recipeDurationMinutes = recipeDurationMinutes,
    recipeServings = recipeServings,
    recipeSourceType = recipeSourceType,
    recipeUpdatedAt = recipeUpdatedAt,
    createdAt = createdAt,
    lastUsedAt = lastUsedAt,
    useCount = useCount
)

internal fun PantryIngredient.toBackup() = PantryIngredientBackup(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    expiresAt = expiresAt,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun RecipePlan.toBackup() = RecipePlanBackup(
    id = id,
    title = title,
    startDateEpochDay = startDateEpochDay,
    endDateEpochDay = endDateEpochDay,
    menuText = menuText,
    generatedByAI = generatedByAI,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AIChatHistory.toBackup() = AIChatHistoryBackup(
    id = id,
    sessionId = sessionId,
    title = title,
    messages = messages,
    createdAt = createdAt,
    updatedAt = updatedAt,
    messageCount = messageCount,
    isPinned = isPinned
)

internal fun APICallRecord.toBackup() = APICallLogBackup(
    id = id,
    timestamp = timestamp,
    configId = configId,
    configName = configName,
    modelId = modelId,
    inputText = inputText,
    outputText = outputText,
    promptTokens = promptTokens,
    completionTokens = completionTokens,
    totalTokens = totalTokens,
    cost = cost,
    duration = duration,
    isSuccess = isSuccess,
    errorMessage = errorMessage
)

internal fun UserSettings.toBackup() = UserSettingsBackup(
    dailyCalorieGoal = dailyCalorieGoal,
    userName = userName,
    userGender = userGender,
    userAge = userAge,
    userHeight = userHeight,
    userWeight = userWeight,
    activityLevel = activityLevel,
    dietaryPreference = dietaryPreference,
    isNotificationEnabled = isNotificationEnabled,
    isDarkMode = isDarkMode,
    themeMode = themeMode,
    useDeadlinerStyle = useDeadlinerStyle,
    hideDividers = hideDividers,
    fontSize = fontSize,
    enableAnimations = enableAnimations,
    feedbackType = feedbackType,
    enableVibration = enableVibration,
    enableSound = enableSound,
    backgroundBehavior = backgroundBehavior,
    startupPage = startupPage,
    enableQuickAdd = enableQuickAdd,
    enableLongPressHomeToAdd = enableLongPressHomeToAdd,
    enableLongPressOverviewToStats = enableLongPressOverviewToStats,
    enableLongPressMyToProfileEdit = enableLongPressMyToProfileEdit,
    enableGoalReminder = enableGoalReminder,
    enableStreakReminder = enableStreakReminder,
    enableAutoBackup = enableAutoBackup,
    enableCloudSync = enableCloudSync,
    showAIWidget = showAIWidget,
    wallpaperType = wallpaperType,
    wallpaperColor = wallpaperColor,
    wallpaperGradientStart = wallpaperGradientStart,
    wallpaperGradientEnd = wallpaperGradientEnd,
    wallpaperImageUri = wallpaperImageUri
)

internal fun com.calorieai.app.data.model.AIConfig.toBackup() = AIConfigBackup(
    id = id,
    name = name,
    icon = icon,
    iconType = iconType.name,
    protocol = protocol.name,
    apiUrl = apiUrl,
    apiKey = null,
    hasApiKey = apiKey.isNotBlank(),
    modelId = modelId,
    isImageUnderstanding = isImageUnderstanding,
    isDefault = isDefault,
    isPreset = isPreset
)
