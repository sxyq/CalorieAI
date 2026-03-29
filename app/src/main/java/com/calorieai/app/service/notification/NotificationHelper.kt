package com.calorieai.app.service.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calorieai.app.MainActivity
import com.calorieai.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        ensureChannels()
    }

    fun ensureChannels() {
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

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(listOf(mealChannel, generalChannel))
    }

    fun showMealReminderNotification(reminderType: MealReminderType) {
        val manager = NotificationManagerCompat.from(context)
        if (!canPostNotifications(manager)) {
            Log.w(TAG, "skip meal notification: permission denied, type=$reminderType")
            return
        }

        val contentIntent = buildContentIntent(
            requestCode = reminderType.notificationId,
            mealType = reminderType.name
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MEAL)
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
        if (!canPostNotifications(manager)) {
            Log.w(TAG, "skip general notification: permission denied, title=$title")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
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

    private fun canPostNotifications(manager: NotificationManagerCompat): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return permissionGranted && manager.areNotificationsEnabled()
    }

    companion object {
        const val CHANNEL_ID_MEAL = "meal_reminder_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
        private const val TAG = "NotificationHelper"
    }
}
