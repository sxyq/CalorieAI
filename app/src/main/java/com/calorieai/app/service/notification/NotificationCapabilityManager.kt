package com.calorieai.app.service.notification

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCapabilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun areNotificationsEnabledInSystem(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun canPostNotifications(): Boolean {
        return hasNotificationPermission() && areNotificationsEnabledInSystem()
    }

    fun supportsExactAlarmPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun canScheduleExactAlarms(): Boolean {
        if (!supportsExactAlarmPermission()) return true
        return runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
    }

    companion object {
        fun createAppNotificationSettingsIntent(context: Context): Intent {
            return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        fun createExactAlarmSettingsIntent(context: Context): Intent? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
            return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
