package com.calorieai.app.utils

object MetabolicConstants {
    val ACTIVITY_MULTIPLIERS = mapOf(
        "SEDENTARY" to 1.2f,
        "LIGHT" to 1.375f,
        "MODERATE" to 1.55f,
        "ACTIVE" to 1.725f,
        "VERY_ACTIVE" to 1.9f
    )

    fun getMultiplier(activityLevel: String): Float {
        return ACTIVITY_MULTIPLIERS[activityLevel] ?: 1.2f
    }

    fun calculateBMR(gender: String, weight: Float?, height: Float?, age: Int?): Int {
        if (weight == null || height == null || age == null) return 0

        val bmr = if (gender == "MALE") {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
        return bmr.toInt().coerceAtLeast(1000)
    }

    fun calculateTDEE(bmr: Int, activityLevel: String): Int {
        val multiplier = getMultiplier(activityLevel)
        return (bmr * multiplier).toInt().coerceAtLeast(1200)
    }

    fun calculateBMRFromTDEE(tdee: Int, activityLevel: String): Int {
        val multiplier = getMultiplier(activityLevel)
        return (tdee / multiplier).toInt()
    }
}
