package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val calories: Int,
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
    val servingSize: Int = 100,
    val servingUnit: String = "g",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toFoodRecord(
        mealType: MealType,
        recordTime: Long = System.currentTimeMillis()
    ): FoodRecord {
        return FoodRecord(
            foodName = foodName,
            totalCalories = calories,
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
            mealType = mealType,
            recordTime = recordTime,
            userInput = foodName
        )
    }
    
    companion object {
        fun fromFoodRecord(record: FoodRecord): FavoriteRecipe {
            return FavoriteRecipe(
                foodName = record.foodName,
                calories = record.totalCalories,
                protein = record.protein,
                carbs = record.carbs,
                fat = record.fat,
                fiber = record.fiber,
                sugar = record.sugar,
                sodium = record.sodium,
                cholesterol = record.cholesterol,
                saturatedFat = record.saturatedFat,
                calcium = record.calcium,
                iron = record.iron,
                vitaminC = record.vitaminC,
                vitaminA = record.vitaminA,
                potassium = record.potassium,
                servingSize = 100,
                servingUnit = "g"
            )
        }
    }
}
