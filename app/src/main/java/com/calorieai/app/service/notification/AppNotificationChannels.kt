package com.calorieai.app.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object AppNotificationChannels {
    const val CHANNEL_ID_MEAL = "meal_reminder_channel"
    const val CHANNEL_ID_GENERAL = "general_channel"
    const val CHANNEL_ID_WATER = "water_reminder_channel"
    const val CHANNEL_ID_ONGOING_ACTIVITY = "ongoing_activity_channel"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val mealChannel = NotificationChannel(
            CHANNEL_ID_MEAL,
            "餐次提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "每日早餐、午餐、晚餐提醒"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            "通用通知",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "应用通用通知"
        }

        val waterChannel = NotificationChannel(
            CHANNEL_ID_WATER,
            "饮水提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "按时段或间隔提醒饮水"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 120, 200)
        }

        val ongoingActivityChannel = NotificationChannel(
            CHANNEL_ID_ONGOING_ACTIVITY,
            "持续活动",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "用于持续展示进行中的活动状态，为后续 Live Updates 适配预留"
            setShowBadge(false)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(
                listOf(mealChannel, generalChannel, waterChannel, ongoingActivityChannel)
            )
    }
}
