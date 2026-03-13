package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

// 导入 ExerciseType 用于 Converters 类
import com.calorieai.app.data.model.ExerciseType

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
    BREAKFAST,      // 早餐
    BREAKFAST_SNACK, // 早加餐
    LUNCH,          // 午餐
    LUNCH_SNACK,    // 午加餐
    DINNER,         // 晚餐
    DINNER_SNACK,   // 晚加餐
    SNACK           // 其他加餐
}

fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.BREAKFAST_SNACK -> "早加餐"
        MealType.LUNCH -> "午餐"
        MealType.LUNCH_SNACK -> "午加餐"
        MealType.DINNER -> "晚餐"
        MealType.DINNER_SNACK -> "晚加餐"
        MealType.SNACK -> "加餐"
    }
}

/**
 * 将加餐类型合并为统一的"加餐"显示
 */
fun getSimplifiedMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.BREAKFAST_SNACK,
        MealType.LUNCH_SNACK,
        MealType.DINNER_SNACK,
        MealType.SNACK -> "加餐"
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
    }
}

/**
 * 获取简化后的餐次顺序（用于排序）
 */
fun getSimplifiedMealTypeOrder(mealType: MealType): Int {
    return when (mealType) {
        MealType.BREAKFAST -> 1
        MealType.LUNCH -> 2
        MealType.DINNER -> 3
        MealType.BREAKFAST_SNACK,
        MealType.LUNCH_SNACK,
        MealType.DINNER_SNACK,
        MealType.SNACK -> 4
    }
}

fun getMealTypeOrder(mealType: MealType): Int {
    return when (mealType) {
        MealType.BREAKFAST -> 1
        MealType.BREAKFAST_SNACK -> 2
        MealType.LUNCH -> 3
        MealType.LUNCH_SNACK -> 4
        MealType.DINNER -> 5
        MealType.DINNER_SNACK -> 6
        MealType.SNACK -> 7
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

    @androidx.room.TypeConverter
    fun fromExerciseType(value: ExerciseType): String {
        return value.name
    }

    @androidx.room.TypeConverter
    fun toExerciseType(value: String): ExerciseType {
        return ExerciseType.valueOf(value)
    }
}
