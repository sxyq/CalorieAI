package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 菜谱缓存实体
 * 存储AI生成的菜谱推荐
 */
@Entity(tableName = "meal_plan_cache")
data class MealPlanCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val weekDataSummary: String,
    val mealPlanJson: String,
    val calorieTarget: Int,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int
)

/**
 * 菜谱计划
 */
data class MealPlan(
    val breakfast: MealSuggestion,
    val lunch: MealSuggestion,
    val dinner: MealSuggestion,
    val snacks: List<MealSuggestion> = emptyList(),
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val nutritionTips: List<String> = emptyList()
)

/**
 * 餐食建议
 */
data class MealSuggestion(
    val name: String,
    val description: String,
    val ingredients: List<MealIngredient>,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val cookingTime: Int = 0,
    val difficulty: String = "简单",
    val tips: String = ""
)

/**
 * 菜谱食材（区别于 FoodRecord 中的 Ingredient）
 */
data class MealIngredient(
    val name: String,
    val amount: String,
    val calories: Int = 0
)

/**
 * 菜谱推荐响应
 */
data class MealPlanResponse(
    val plan: MealPlan,
    val personalizedTips: List<String>,
    val alternatives: List<MealSuggestion> = emptyList()
)
