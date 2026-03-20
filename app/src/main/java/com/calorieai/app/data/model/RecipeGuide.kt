package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_guides")
data class RecipeGuide(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val ingredientsText: String,
    val stepsText: String,
    val toolsText: String,
    val difficulty: String,
    val durationMinutes: Int,
    val servings: Int = 1,
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val sourceType: String = "MANUAL",
    val linkedFavoriteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

