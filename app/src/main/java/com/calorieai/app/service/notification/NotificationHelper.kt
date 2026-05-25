package com.calorieai.app.service.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calorieai.app.MainActivity
import com.calorieai.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationCapabilityManager: NotificationCapabilityManager
) {

    init {
        ensureChannels()
    }

    fun ensureChannels() {
        AppNotificationChannels.ensure(context)
    }

    fun showMealReminderNotification(reminderType: MealReminderType) {
        val manager = NotificationManagerCompat.from(context)
        if (!notificationCapabilityManager.canPostNotifications()) {
            Log.w(TAG, "skip meal notification: permission denied, type=$reminderType")
            return
        }

        val contentIntent = buildContentIntent(
            requestCode = reminderType.notificationId,
            mealType = reminderType.name
        )

        val notification = NotificationCompat.Builder(context, AppNotificationChannels.CHANNEL_ID_MEAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminderType.title)
            .setContentText(reminderType.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(reminderType.message)
            )
            .addAction(
                R.drawable.ic_add,
                "立即记录",
                contentIntent
            )
            .build()

        try {
            manager.notify(reminderType.notificationId, notification)
            Log.i(TAG, "meal notification sent: type=$reminderType")
        } catch (t: Throwable) {
            Log.e(TAG, "meal notification failed: type=$reminderType", t)
        }
    }

    fun showGeneralNotification(title: String, message: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!notificationCapabilityManager.canPostNotifications()) {
            Log.w(TAG, "skip general notification: permission denied, title=$title")
            return
        }

        val notification = NotificationCompat.Builder(context, AppNotificationChannels.CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(requestCode = 0, mealType = null))
            .build()

        try {
            manager.notify(System.currentTimeMillis().toInt(), notification)
            Log.i(TAG, "general notification sent: title=$title")
        } catch (t: Throwable) {
            Log.e(TAG, "general notification failed: title=$title", t)
        }
    }

    fun showWaterReminderNotification(reminderLabel: String? = null) {
        val manager = NotificationManagerCompat.from(context)
        if (!notificationCapabilityManager.canPostNotifications()) {
            Log.w(TAG, "skip water notification: permission denied")
            return
        }

        val contentText = if (reminderLabel.isNullOrBlank()) {
            "该补充水分了，保持今天饮水目标进度！"
        } else {
            "[$reminderLabel] 该补充水分了，保持今天饮水目标进度！"
        }

        val notification = NotificationCompat.Builder(context, AppNotificationChannels.CHANNEL_ID_WATER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("饮水提醒")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(requestCode = 4001, mealType = null))
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .build()

        try {
            manager.notify(4001, notification)
            Log.i(TAG, "water notification sent")
        } catch (t: Throwable) {
            Log.e(TAG, "water notification failed", t)
        }
    }

    fun cancelMealReminderNotification(reminderType: MealReminderType) {
        NotificationManagerCompat.from(context).cancel(reminderType.notificationId)
    }

    private fun buildContentIntent(requestCode: Int, mealType: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_add", true)
            if (!mealType.isNullOrBlank()) {
                putExtra("meal_type", mealType)
            }
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "NotificationHelper"
    }
}
