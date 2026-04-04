package com.calorieai.app.ui.screens.onboarding

object OnboardingValidators {
    fun validateWeight(value: String, unit: String): String? {
        if (value.isBlank()) return null
        val num = value.toFloatOrNull()
        return when {
            num == null -> "请输入有效数字"
            unit == "kg" && (num < 30 || num > 200) -> "体重范围：30-200kg"
            unit == "lb" && (num < 66 || num > 440) -> "体重范围：66-440lb"
            else -> null
        }
    }

    fun validateHeight(value: String, unit: String): String? {
        if (value.isBlank()) return null
        val num = value.toFloatOrNull()
        return when {
            num == null -> "请输入有效数字"
            unit == "cm" && (num < 100 || num > 250) -> "身高范围：100-250cm"
            unit == "ft" && (num < 3.3 || num > 8.2) -> "身高范围：3.3-8.2ft"
            else -> null
        }
    }

    fun convertWeight(value: Float, from: String, to: String): Float {
        return if (from == to) value
        else if (from == "kg" && to == "lb") value * 2.20462f
        else value / 2.20462f
    }

    fun convertHeight(value: Float, from: String, to: String): Float {
        return if (from == to) value
        else if (from == "cm" && to == "ft") value / 30.48f
        else value * 30.48f
    }
}
