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
    @Volatile
    private var lastSyncSignature: String? = null

    fun syncMealReminders(settings: UserSettings) {
        val signature = buildString {
            append(settings.isNotificationEnabled)
            append("|")
            append(settings.breakfastReminderTime)
            append("|")
            append(settings.lunchReminderTime)
            append("|")
            append(settings.dinnerReminderTime)
        }
        if (signature == lastSyncSignature) return
        lastSyncSignature = signature

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
