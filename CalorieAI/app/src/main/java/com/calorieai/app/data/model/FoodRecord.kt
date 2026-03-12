package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "food_records")
@TypeConverters(Converters::class)
data class FoodRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    
    val ingredients: List<Ingredient> = emptyList(),
    
    val mealType: MealType,
    val recordTime: Long = System.currentTimeMillis(),
    val iconUrl: String? = null,
    val iconLocalPath: String? = null,
    val isStarred: Boolean = false,
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM,
    val notes: String? = null
)

data class Ingredient(
    val name: String,
    val weight: String,
    val calories: Int
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}

fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
        MealType.SNACK -> "加餐"
    }
}

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

class Converters {
    @androidx.room.TypeConverter
    fun fromIngredientsList(value: List<Ingredient>): String {
        return com.google.gson.Gson().toJson(value)
    }

    @androidx.room.TypeConverter
    fun toIngredientsList(value: String): List<Ingredient> {
        val type = object : com.google.gson.reflect.TypeToken<List<Ingredient>>() {}.type
        return com.google.gson.Gson().fromJson(value, type)
    }

    @androidx.room.TypeConverter
    fun fromMealType(value: MealType): String {
        return value.name
    }

    @androidx.room.TypeConverter
    fun toMealType(value: String): MealType {
        return MealType.valueOf(value)
    }

    @androidx.room.TypeConverter
    fun fromConfidenceLevel(value: ConfidenceLevel): String {
        return value.name
    }

    @androidx.room.TypeConverter
    fun toConfidenceLevel(value: String): ConfidenceLevel {
        return ConfidenceLevel.valueOf(value)
    }
}
