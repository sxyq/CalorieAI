package com.calorieai.app.service.backup

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 7,
    val backupDate: String,
    val appVersion: String = "3.6.0",
    val foodRecords: List<FoodRecordBackup>,
    val exerciseRecords: List<ExerciseRecordBackup>,
    val userSettings: UserSettingsBackup?,
    val aiConfigs: List<AIConfigBackup> = emptyList(),
    val weightRecords: List<WeightRecordBackup> = emptyList(),
    val waterRecords: List<WaterRecordBackup> = emptyList(),
    val favoriteRecipes: List<FavoriteRecipeBackup> = emptyList(),
    val pantryIngredients: List<PantryIngredientBackup> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("recipeGuides")
    val legacyRecipeGuides: List<RecipeGuideBackup> = emptyList(),
    val recipePlans: List<RecipePlanBackup> = emptyList(),
    val aiChatHistory: List<AIChatHistoryBackup> = emptyList(),
    val apiCallLogs: List<APICallLogBackup> = emptyList(),
    val includeAIConfigs: Boolean = true
)

@Serializable
data class AIConfigBackup(
    val id: String,
    val name: String,
    val icon: String,
    val iconType: String,
    val protocol: String,
    val apiUrl: String,
    val apiKey: String? = null,
    val hasApiKey: Boolean = false,
    val modelId: String,
    val isImageUnderstanding: Boolean,
    val isDefault: Boolean,
    val isPreset: Boolean = false
)

@Serializable
data class FoodRecordBackup(
    val id: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val cholesterol: Float = 0f,
    val saturatedFat: Float = 0f,
    val calcium: Float = 0f,
    val iron: Float = 0f,
    val vitaminC: Float = 0f,
    val vitaminA: Float = 0f,
    val potassium: Float = 0f,
    val ingredients: List<IngredientBackup> = emptyList(),
    val mealType: String,
    val recordTime: Long,
    val iconUrl: String? = null,
    val iconLocalPath: String? = null,
    val isStarred: Boolean = false,
    val confidence: String = "MEDIUM",
    val notes: String? = null
)

@Serializable
data class IngredientBackup(
    val name: String,
    val weight: String,
    val calories: Int
)

@Serializable
data class ExerciseRecordBackup(
    val id: String,
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String? = null,
    val recordTime: Long
)

@Serializable
data class WeightRecordBackup(
    val id: Long,
    val weight: Float,
    val recordDate: Long,
    val note: String? = null
)

@Serializable
data class WaterRecordBackup(
    val id: Long,
    val amount: Int,
    val recordTime: Long,
    val recordDate: Long,
    val note: String? = null
)

@Serializable
data class FavoriteRecipeBackup(
    val id: String,
    val sourceRecordId: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val cholesterol: Float = 0f,
    val saturatedFat: Float = 0f,
    val calcium: Float = 0f,
    val iron: Float = 0f,
    val vitaminC: Float = 0f,
    val vitaminA: Float = 0f,
    val potassium: Float = 0f,
    val recipeIngredientsText: String? = null,
    val recipeStepsText: String? = null,
    val recipeToolsText: String? = null,
    val recipeDifficulty: String? = null,
    val recipeDurationMinutes: Int? = null,
    val recipeServings: Int? = null,
    val recipeSourceType: String? = null,
    val recipeUpdatedAt: Long? = null,
    val createdAt: Long,
    val lastUsedAt: Long? = null,
    val useCount: Int = 0
)

@Serializable
data class PantryIngredientBackup(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val expiresAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RecipeGuideBackup(
    val id: String,
    val name: String,
    val ingredientsText: String,
    val stepsText: String,
    val toolsText: String,
    val difficulty: String,
    val durationMinutes: Int,
    val servings: Int,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sourceType: String,
    val linkedFavoriteId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RecipePlanBackup(
    val id: String,
    val title: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val menuText: String,
    val generatedByAI: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class AIChatHistoryBackup(
    val id: Long,
    val sessionId: String,
    val title: String,
    val messages: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int = 0,
    val isPinned: Boolean = false
)

@Serializable
data class APICallLogBackup(
    val id: String,
    val timestamp: Long,
    val configId: String,
    val configName: String,
    val modelId: String,
    val inputText: String,
    val outputText: String,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val cost: Double = 0.0,
    val duration: Long = 0,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

@Serializable
data class UserSettingsBackup(
    val dailyCalorieGoal: Int,
    val userName: String? = null,
    val userGender: String? = null,
    val userAge: Int? = null,
    val userHeight: Float? = null,
    val userWeight: Float? = null,
    val activityLevel: String = "MODERATE",
    val dietaryPreference: String? = null,
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean? = null,
    val themeMode: String = "SYSTEM",
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: String = "MEDIUM",
    val enableAnimations: Boolean = true,
    val feedbackType: String = "NONE",
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val backgroundBehavior: String = "STANDARD",
    val startupPage: String = "HOME",
    val enableQuickAdd: Boolean = true,
    val enableLongPressHomeToAdd: Boolean = true,
    val enableLongPressOverviewToStats: Boolean = true,
    val enableLongPressMyToProfileEdit: Boolean = true,
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = true,
    val enableAutoBackup: Boolean = false,
    val enableCloudSync: Boolean = false,
    val showAIWidget: Boolean = true,
    val wallpaperType: String = "GRADIENT",
    val wallpaperColor: String? = null,
    val wallpaperGradientStart: String? = null,
    val wallpaperGradientEnd: String? = null,
    val wallpaperImageUri: String? = null
)
