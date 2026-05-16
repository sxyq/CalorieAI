package com.calorieai.app.service.notification

import com.calorieai.app.data.model.UserSettings
import java.time.ZoneId

internal object ReminderSyncSignature {
    fun build(settings: UserSettings): String {
        return buildString {
            append(settings.isNotificationEnabled)
            append('|')
            append(settings.breakfastReminderTime)
            append('|')
            append(settings.lunchReminderTime)
            append('|')
            append(settings.dinnerReminderTime)
            append('|')
            append(settings.showWaterFeatures)
            append('|')
            append(settings.enableWaterReminder)
            append('|')
            append(settings.waterReminderTimesJson)
            append('|')
            append(settings.waterReminderIntervalMinutes)
            append('|')
            append(settings.waterReminderWindowStart)
            append('|')
            append(settings.waterReminderWindowEnd)
            append('|')
            append(ZoneId.systemDefault().id)
        }
    }
}
