package com.calorieai.app.service.notification

enum class MealReminderType(
    val requestCode: Int,
    val notificationId: Int,
    val defaultTime: String,
    val title: String,
    val message: String
) {
    BREAKFAST(
        requestCode = 2101,
        notificationId = 1001,
        defaultTime = "08:00",
        title = "早餐时间到了",
        message = "记得记录今天的早餐哦"
    ),
    LUNCH(
        requestCode = 2102,
        notificationId = 1002,
        defaultTime = "12:00",
        title = "午餐时间到了",
        message = "记得记录今天的午餐哦"
    ),
    DINNER(
        requestCode = 2103,
        notificationId = 1003,
        defaultTime = "18:00",
        title = "晚餐时间到了",
        message = "记得记录今天的晚餐哦"
    );

    companion object {
        fun fromRaw(value: String?): MealReminderType? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}

object MealReminderContract {
    const val ACTION_MEAL_REMINDER = "com.calorieai.app.action.MEAL_REMINDER"
    const val EXTRA_MEAL_TYPE = "extra_meal_type"
    const val EXTRA_REMINDER_TIME = "extra_reminder_time"
    const val DEFAULT_REMINDER_TIME = "08:00"
}
