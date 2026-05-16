package com.calorieai.app.ui.screens.add

import org.json.JSONObject
import kotlin.math.roundToInt

data class OcrNutritionPayload(
    val foodName: String,
    val weightGrams: Int,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val source: String = "",
    val rawText: String = ""
) {
    fun totalCalories(): Int = scaled(caloriesPer100g).roundToInt().coerceAtLeast(0)
    fun totalProtein(): Float = scaled(proteinPer100g)
    fun totalCarbs(): Float = scaled(carbsPer100g)
    fun totalFat(): Float = scaled(fatPer100g)

    fun toDescription(): String {
        return buildString {
            append("OCR识别：")
            append(foodName)
            append(" ")
            append(weightGrams)
            append("g；每100g：热量")
            append(formatNumber(caloriesPer100g))
            append("千卡，蛋白质")
            append(formatNumber(proteinPer100g))
            append("g，碳水")
            append(formatNumber(carbsPer100g))
            append("g，脂肪")
            append(formatNumber(fatPer100g))
            append("g")
        }
    }

    fun toJson(): String {
        return JSONObject()
            .put("foodName", foodName)
            .put("weightGrams", weightGrams)
            .put("caloriesPer100g", caloriesPer100g.toDouble())
            .put("proteinPer100g", proteinPer100g.toDouble())
            .put("carbsPer100g", carbsPer100g.toDouble())
            .put("fatPer100g", fatPer100g.toDouble())
            .put("source", source)
            .put("rawText", rawText)
            .toString()
    }

    private fun scaled(valuePer100g: Float): Float {
        return (valuePer100g * weightGrams.toFloat() / 100f).coerceAtLeast(0f)
    }
}

fun parseOcrNutritionPayload(json: String?): OcrNutritionPayload? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        val obj = JSONObject(json)
        OcrNutritionPayload(
            foodName = obj.optString("foodName").trim(),
            weightGrams = obj.optInt("weightGrams"),
            caloriesPer100g = obj.optDouble("caloriesPer100g").toFloat(),
            proteinPer100g = obj.optDouble("proteinPer100g").toFloat(),
            carbsPer100g = obj.optDouble("carbsPer100g").toFloat(),
            fatPer100g = obj.optDouble("fatPer100g").toFloat(),
            source = obj.optString("source").trim(),
            rawText = obj.optString("rawText")
        )
    }.getOrNull()?.takeIf { it.foodName.isNotBlank() && it.weightGrams > 0 }
}

private fun formatNumber(value: Float): String {
    val normalized = if (value < 0f) 0f else value
    val rounded = (normalized * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
