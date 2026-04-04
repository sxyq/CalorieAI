package com.calorieai.app.utils

import com.calorieai.app.data.model.MealType
import java.util.Calendar

/**
 * 按当前时间自动推断主餐类型（早餐/午餐/晚餐）。
 */
fun inferMainMealType(timestamp: Long = System.currentTimeMillis()): MealType {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..10 -> MealType.BREAKFAST
        in 11..15 -> MealType.LUNCH
        else -> MealType.DINNER
    }
}

/**
 * 基于选中日期和餐次生成用于落库的时间戳。
 */
fun buildRecordTimeForDateAndMeal(
    dateMillis: Long,
    mealType: MealType,
    sequenceOffsetSeconds: Int = 0
): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        val (hour, minute) = when (mealType) {
            MealType.BREAKFAST,
            MealType.BREAKFAST_SNACK -> 8 to 0
            MealType.LUNCH,
            MealType.LUNCH_SNACK -> 12 to 30
            MealType.DINNER,
            MealType.DINNER_SNACK -> 18 to 30
            MealType.SNACK -> 15 to 0
        }
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.SECOND, sequenceOffsetSeconds)
    }
    return calendar.timeInMillis
}

fun isSameLocalDate(firstMillis: Long, secondMillis: Long): Boolean {
    val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
    val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}
