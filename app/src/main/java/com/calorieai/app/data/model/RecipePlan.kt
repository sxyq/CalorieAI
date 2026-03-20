package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_plans")
data class RecipePlan(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val menuText: String,
    val generatedByAI: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

