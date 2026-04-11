package com.calorieai.app.ui.screens.add

import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.domain.recipe.RecipePersonalization
import kotlin.math.floor

data class RecipePersonalizationState(
    val dietaryAllergens: String = "",
    val flavorPreferences: String = "",
    val budgetPreference: String = "",
    val maxCookingMinutes: String = "",
    val specialPopulationMode: String = "GENERAL",
    val weeklyRecordGoalDays: String = "5"
) {
    fun toDomain(): RecipePersonalization {
        return RecipePersonalization(
            dietaryAllergens = dietaryAllergens,
            flavorPreferences = flavorPreferences,
            budgetPreference = budgetPreference,
            maxCookingMinutes = maxCookingMinutes,
            specialPopulationMode = specialPopulationMode,
            weeklyRecordGoalDays = weeklyRecordGoalDays
        )
    }

    companion object {
        fun fromSettings(settings: UserSettings): RecipePersonalizationState {
            return RecipePersonalizationState(
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

fun buildRecipePantrySummaryLines(
    items: List<PantryIngredient>,
    nowMillis: Long = System.currentTimeMillis()
): List<String> {
    return items.map { item ->
        val expireInfo = item.expiresAt?.let { expiresAt ->
            val days = floor((expiresAt - nowMillis) / (24f * 60f * 60f * 1000f)).toInt()
            val text = if (days >= 0) "${days}天后过期" else "已过期"
            "（$text）"
        } ?: ""
        "- ${item.name} ${item.quantity}${item.unit}$expireInfo"
    }
}
