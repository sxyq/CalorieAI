package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_recipes",
    indices = [
        Index(value = ["sourceRecordId"], unique = true)
    ]
)
data class FavoriteRecipe(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
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
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val useCount: Int = 0
)
