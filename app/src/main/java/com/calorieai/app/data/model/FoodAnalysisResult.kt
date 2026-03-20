package com.calorieai.app.data.model

import com.google.gson.annotations.SerializedName

data class FoodAnalysisResult(
    @SerializedName("foodName") val foodName: String = "",
    @SerializedName("estimatedWeight") val estimatedWeight: Int = 0,
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("fiber") val fiber: Float = 0f,
    @SerializedName("sugar") val sugar: Float = 0f,
    @SerializedName("sodium") val sodium: Float = 0f,
    @SerializedName("cholesterol") val cholesterol: Float = 0f,
    @SerializedName("saturatedFat") val saturatedFat: Float = 0f,
    @SerializedName("calcium") val calcium: Float = 0f,
    @SerializedName("iron") val iron: Float = 0f,
    @SerializedName("vitaminC") val vitaminC: Float = 0f,
    @SerializedName("vitaminA") val vitaminA: Float = 0f,
    @SerializedName("potassium") val potassium: Float = 0f,
    @SerializedName("description") val description: String = "",
    val promptTokens: Int = 0,
    val completionTokens: Int = 0
)

data class FoodBatchAnalysisResult(
    val items: List<FoodAnalysisResult> = emptyList(),
    val promptTokens: Int = 0,
    val completionTokens: Int = 0
)
