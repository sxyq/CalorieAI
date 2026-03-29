package com.calorieai.app.service.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.calorieai.app.data.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val runtimePrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Volatile
    private var lastSyncSignature: String? = null

    fun syncMealReminders(
        settings: UserSettings,
        source: String = "unknown",
        force: Boolean = false
    ) {
        val signature = buildSyncSignature(settings)
        if (!force && signature == lastSyncSignature) {
            Log.d(TAG, "sync skipped: same signature, source=$source")
            return
        }
        lastSyncSignature = signature

        // 清理历史 WorkManager 任务，避免旧逻辑重复触发。
        MealReminderWorker.cancelAllReminders(context)
        notificationHelper.ensureChannels()

        if (!settings.isNotificationEnabled) {
            cancelAllReminders()
            Log.i(TAG, "sync done: notifications disabled, source=$source")
            return
        }

        scheduleReminder(
            reminderType = MealReminderType.BREAKFAST,
            reminderTime = normalizeReminderTime(settings.breakfastReminderTime, MealReminderType.BREAKFAST),
            source = source
        )
        scheduleReminder(
            reminderType = MealReminderType.LUNCH,
            reminderTime = normalizeReminderTime(settings.lunchReminderTime, MealReminderType.LUNCH),
            source = source
        )
        scheduleReminder(
            reminderType = MealReminderType.DINNER,
            reminderTime = normalizeReminderTime(settings.dinnerReminderTime, MealReminderType.DINNER),
            source = source
        )
        Log.i(TAG, "sync done: 3 reminders scheduled, source=$source")
    }

    fun onMealReminderTriggered(
        rawMealType: String?,
        rawReminderTime: String?,
        source: String
    ) {
        val reminderType = MealReminderType.fromRaw(rawMealType)
        if (reminderType == null) {
            Log.e(TAG, "trigger ignored: invalid meal type, rawMealType=$rawMealType")
            return
        }

        val normalizedTime = normalizeReminderTime(rawReminderTime, reminderType)
        val isDuplicate = shouldSuppressDuplicateTrigger(reminderType)
        try {
            if (isDuplicate) {
                Log.i(TAG, "trigger deduplicated: type=$reminderType, source=$source")
            } else {
                notificationHelper.showMealReminderNotification(reminderType)
            }
            scheduleReminder(reminderType, normalizedTime, "trigger:$source")
            Log.i(TAG, "trigger handled: type=$reminderType, time=$normalizedTime, source=$source")
        } catch (t: Throwable) {
            Log.e(TAG, "trigger failed: type=$reminderType, time=$normalizedTime, source=$source", t)
        }
    }

    fun cancelAllReminders() {
        MealReminderType.entries.forEach { cancelReminder(it) }
        Log.i(TAG, "cancelAllReminders: all meal alarms cancelled")
    }

    private fun scheduleReminder(
        reminderType: MealReminderType,
        reminderTime: String,
        source: String
    ) {
        cancelReminder(reminderType)

        val triggerAtMillis = computeNextTriggerAtMillis(reminderTime)
        val pendingIntent = buildPendingIntent(
            reminderType = reminderType,
            reminderTime = reminderTime,
            noCreate = false
        )

        if (pendingIntent == null) {
            Log.e(TAG, "schedule failed: pendingIntent null, type=$reminderType")
            return
        }

        MealReminderWorker.scheduleReminder(
            context = context,
            reminderType = reminderType,
            reminderTime = reminderTime,
            triggerAtMillis = triggerAtMillis
        )

        val exactAllowed = canScheduleExactAlarms()
        try {
            if (exactAllowed) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.i(
                TAG,
                "schedule success: type=$reminderType, time=$reminderTime, triggerAt=$triggerAtMillis, exact=$exactAllowed, source=$source"
            )
        } catch (t: Throwable) {
            Log.e(TAG, "schedule failed: type=$reminderType, time=$reminderTime, source=$source", t)
        }
    }

    private fun cancelReminder(reminderType: MealReminderType) {
        MealReminderWorker.cancelReminder(context, reminderType)

        val pendingIntent = buildPendingIntent(
            reminderType = reminderType,
            reminderTime = reminderType.defaultTime,
            noCreate = true
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildPendingIntent(
        reminderType: MealReminderType,
        reminderTime: String,
        noCreate: Boolean
    ): PendingIntent? {
        val intent = Intent(context, MealReminderAlarmReceiver::class.java).apply {
            action = MealReminderContract.ACTION_MEAL_REMINDER
            putExtra(MealReminderContract.EXTRA_MEAL_TYPE, reminderType.name)
            putExtra(MealReminderContract.EXTRA_REMINDER_TIME, reminderTime)
        }
        val flags = if (noCreate) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(
            context,
            reminderType.requestCode,
            intent,
            flags
        )
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
        }
    }

    private fun computeNextTriggerAtMillis(reminderTime: String): Long {
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)
        val localTime = runCatching { LocalTime.parse(reminderTime) }
            .getOrElse { LocalTime.parse(MealReminderContract.DEFAULT_REMINDER_TIME) }

        var target = now
            .withHour(localTime.hour)
            .withMinute(localTime.minute)
            .withSecond(0)
            .withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return target.toInstant().toEpochMilli()
    }

    private fun normalizeReminderTime(rawTime: String?, reminderType: MealReminderType): String {
        if (rawTime.isNullOrBlank()) return reminderType.defaultTime
        return runCatching {
            val parsed = LocalTime.parse(rawTime)
            "%02d:%02d".format(parsed.hour, parsed.minute)
        }.getOrDefault(reminderType.defaultTime)
    }

    private fun buildSyncSignature(settings: UserSettings): String {
        return buildString {
            append(settings.isNotificationEnabled)
            append('|')
            append(settings.breakfastReminderTime)
            append('|')
            append(settings.lunchReminderTime)
            append('|')
            append(settings.dinnerReminderTime)
            append('|')
            append(ZoneId.systemDefault().id)
        }
    }

    private fun shouldSuppressDuplicateTrigger(reminderType: MealReminderType): Boolean {
        val key = "last_trigger_${reminderType.name}"
        val now = System.currentTimeMillis()
        val last = runtimePrefs.getLong(key, 0L)
        val duplicate = now - last < DUPLICATE_WINDOW_MILLIS
        if (!duplicate) {
            runtimePrefs.edit().putLong(key, now).apply()
        }
        return duplicate
    }

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val PREFS_NAME = "meal_notification_runtime"
        private const val DUPLICATE_WINDOW_MILLIS = 120_000L
    }
}
