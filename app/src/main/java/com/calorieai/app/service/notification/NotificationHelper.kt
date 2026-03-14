package com.calorieai.app.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calorieai.app.MainActivity
import com.calorieai.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_MEAL_REMINDER = "meal_reminder_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
        
        const val NOTIFICATION_ID_BREAKFAST = 1001
        const val NOTIFICATION_ID_LUNCH = 1002
        const val NOTIFICATION_ID_DINNER = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 餐次提醒频道
            val mealReminderChannel = NotificationChannel(
                CHANNEL_ID_MEAL_REMINDER,
                "餐次提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒记录早餐、午餐、晚餐"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // 通用通知频道
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "通用通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "应用通用通知"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(
                listOf(mealReminderChannel, generalChannel)
            )
        }
    }

    fun showMealReminderNotification(mealType: MealType) {
        val (title, message, notificationId) = when (mealType) {
            MealType.BREAKFAST -> Triple(
                "早餐时间到了",
                "记得记录今天的早餐哦",
                NOTIFICATION_ID_BREAKFAST
            )
            MealType.LUNCH -> Triple(
                "午餐时间到了",
                "记得记录今天的午餐哦",
                NOTIFICATION_ID_LUNCH
            )
            MealType.DINNER -> Triple(
                "晚餐时间到了",
                "记得记录今天的晚餐哦",
                NOTIFICATION_ID_DINNER
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_add", true)
            putExtra("meal_type", mealType.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_MEAL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_add,
                "立即记录",
                pendingIntent
            )

        // OPPO流体云适配
        if (isOppoDevice()) {
            builder.apply {
                // 设置流体云样式
                setStyle(NotificationCompat.BigTextStyle().bigText(message))
                // 设置通知类别为提醒
                setCategory(NotificationCompat.CATEGORY_REMINDER)
                // 设置可见性
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // 权限被拒绝
                e.printStackTrace()
            }
        }
    }

    fun cancelMealReminderNotification(mealType: MealType) {
        val notificationId = when (mealType) {
            MealType.BREAKFAST -> NOTIFICATION_ID_BREAKFAST
            MealType.LUNCH -> NOTIFICATION_ID_LUNCH
            MealType.DINNER -> NOTIFICATION_ID_DINNER
        }
        
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }

    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(System.currentTimeMillis().toInt(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun isOppoDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer.contains("oppo") || 
               manufacturer.contains("realme") || 
               manufacturer.contains("oneplus")
    }
}

enum class MealType {
    BREAKFAST, LUNCH, DINNER
}
