package com.calorieai.app.service.notification

import android.content.Context
import com.calorieai.app.data.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun syncMealReminders(settings: UserSettings) {
        if (!settings.isNotificationEnabled) {
            MealReminderWorker.cancelAllReminders(context)
            return
        }
        MealReminderWorker.scheduleMealReminders(
            context = context,
            breakfastTime = settings.breakfastReminderTime,
            lunchTime = settings.lunchReminderTime,
            dinnerTime = settings.dinnerReminderTime
        )
    }
}

