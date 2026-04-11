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
        // Backward-compatible entrypoint.
        syncReminders(settings = settings, source = source, force = force)
    }

    fun syncReminders(
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

        MealReminderWorker.cancelAllReminders(context)
        notificationHelper.ensureChannels()

        if (!settings.isNotificationEnabled) {
            cancelAllReminders()
            Log.i(TAG, "sync done: notifications disabled, source=$source")
            return
        }

        syncMealReminderAlarms(settings, source)
        syncWaterReminderAlarms(settings, source)
        Log.i(TAG, "sync done: meal+water reminders scheduled, source=$source")
    }

    fun onMealReminderTriggered(
        rawMealType: String?,
        rawReminderTime: String?,
        source: String
    ) {
        val reminderType = MealReminderType.fromRaw(rawMealType)
        if (reminderType == null) {
            Log.e(TAG, "meal trigger ignored: invalid type=$rawMealType")
            return
        }

        val normalizedTime = normalizeReminderTime(rawReminderTime, reminderType.defaultTime)
        val isDuplicate = shouldSuppressDuplicateTrigger("meal_${reminderType.name}")
        try {
            if (!isDuplicate) {
                notificationHelper.showMealReminderNotification(reminderType)
            }
            scheduleMealReminder(reminderType, normalizedTime, "trigger:$source")
        } catch (t: Throwable) {
            Log.e(TAG, "meal trigger failed: type=$reminderType", t)
        }
    }

    fun onWaterReminderTriggered(
        rawReminderId: String?,
        rawReminderTime: String?,
        rawIntervalMinutes: Int,
        source: String
    ) {
        val reminderId = rawReminderId?.takeIf { it.isNotBlank() } ?: WATER_REMINDER_ID_INTERVAL
        val reminderTime = normalizeReminderTime(rawReminderTime, DEFAULT_WATER_REMINDER_TIME)
        val intervalMinutes = rawIntervalMinutes.coerceIn(0, MAX_INTERVAL_MINUTES)

        val duplicateKey = "water_$reminderId"
        val isDuplicate = shouldSuppressDuplicateTrigger(duplicateKey)
        try {
            if (!isDuplicate) {
                notificationHelper.showWaterReminderNotification(
                    reminderLabel = if (reminderId.startsWith(WATER_REMINDER_ID_FIXED_PREFIX)) reminderTime else null
                )
            }

            if (reminderId.startsWith(WATER_REMINDER_ID_FIXED_PREFIX)) {
                scheduleWaterReminder(
                    reminderId = reminderId,
                    reminderTime = reminderTime,
                    intervalMinutes = 0,
                    triggerAtMillis = computeNextTriggerAtMillis(reminderTime),
                    source = "trigger:$source"
                )
            } else if (intervalMinutes > 0) {
                val settings = try {
                    runtimePrefs.getString(KEY_LAST_WATER_WINDOW_START, DEFAULT_WATER_WINDOW_START) to
                        runtimePrefs.getString(KEY_LAST_WATER_WINDOW_END, DEFAULT_WATER_WINDOW_END)
                } catch (_: Throwable) {
                    DEFAULT_WATER_WINDOW_START to DEFAULT_WATER_WINDOW_END
                }
                val nextTrigger = computeNextIntervalTriggerAtMillis(
                    intervalMinutes = intervalMinutes,
                    windowStart = settings.first ?: DEFAULT_WATER_WINDOW_START,
                    windowEnd = settings.second ?: DEFAULT_WATER_WINDOW_END
                )
                scheduleWaterReminder(
                    reminderId = WATER_REMINDER_ID_INTERVAL,
                    reminderTime = reminderTime,
                    intervalMinutes = intervalMinutes,
                    triggerAtMillis = nextTrigger,
                    source = "trigger:$source"
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "water trigger failed: id=$reminderId", t)
        }
    }

    fun cancelAllReminders() {
        cancelAllMealReminders()
        cancelAllWaterReminders()
    }

    private fun syncMealReminderAlarms(settings: UserSettings, source: String) {
        scheduleMealReminder(
            reminderType = MealReminderType.BREAKFAST,
            reminderTime = normalizeReminderTime(settings.breakfastReminderTime, MealReminderType.BREAKFAST.defaultTime),
            source = source
        )
        scheduleMealReminder(
            reminderType = MealReminderType.LUNCH,
            reminderTime = normalizeReminderTime(settings.lunchReminderTime, MealReminderType.LUNCH.defaultTime),
            source = source
        )
        scheduleMealReminder(
            reminderType = MealReminderType.DINNER,
            reminderTime = normalizeReminderTime(settings.dinnerReminderTime, MealReminderType.DINNER.defaultTime),
            source = source
        )
    }

    private fun syncWaterReminderAlarms(settings: UserSettings, source: String) {
        cancelAllWaterReminders()

        // Persist latest interval window for trigger reschedule path.
        runtimePrefs.edit()
            .putString(KEY_LAST_WATER_WINDOW_START, settings.waterReminderWindowStart)
            .putString(KEY_LAST_WATER_WINDOW_END, settings.waterReminderWindowEnd)
            .apply()

        if (!settings.showWaterFeatures || !settings.enableWaterReminder) {
            Log.i(TAG, "water reminder disabled by settings, source=$source")
            return
        }

        val fixedTimes = parseReminderTimes(settings.waterReminderTimesJson)
        fixedTimes.forEachIndexed { index, time ->
            val reminderId = "$WATER_REMINDER_ID_FIXED_PREFIX$index"
            scheduleWaterReminder(
                reminderId = reminderId,
                reminderTime = time,
                intervalMinutes = 0,
                triggerAtMillis = computeNextTriggerAtMillis(time),
                source = source
            )
        }

        val intervalMinutes = settings.waterReminderIntervalMinutes.coerceIn(0, MAX_INTERVAL_MINUTES)
        if (intervalMinutes > 0) {
            val triggerAt = computeNextIntervalTriggerAtMillis(
                intervalMinutes = intervalMinutes,
                windowStart = settings.waterReminderWindowStart,
                windowEnd = settings.waterReminderWindowEnd
            )
            scheduleWaterReminder(
                reminderId = WATER_REMINDER_ID_INTERVAL,
                reminderTime = DEFAULT_WATER_REMINDER_TIME,
                intervalMinutes = intervalMinutes,
                triggerAtMillis = triggerAt,
                source = source
            )
        }
    }

    private fun scheduleMealReminder(
        reminderType: MealReminderType,
        reminderTime: String,
        source: String
    ) {
        cancelMealReminder(reminderType)

        val triggerAtMillis = computeNextTriggerAtMillis(reminderTime)
        val pendingIntent = buildMealPendingIntent(
            reminderType = reminderType,
            reminderTime = reminderTime,
            noCreate = false
        )

        if (pendingIntent == null) {
            Log.e(TAG, "meal schedule failed: pendingIntent null, type=$reminderType")
            return
        }

        MealReminderWorker.scheduleReminder(
            context = context,
            reminderType = reminderType,
            reminderTime = reminderTime,
            triggerAtMillis = triggerAtMillis
        )

        scheduleAlarm(triggerAtMillis, pendingIntent)
        Log.i(TAG, "meal scheduled: type=$reminderType, time=$reminderTime, source=$source")
    }

    private fun scheduleWaterReminder(
        reminderId: String,
        reminderTime: String,
        intervalMinutes: Int,
        triggerAtMillis: Long,
        source: String
    ) {
        cancelWaterReminder(reminderId)

        val pendingIntent = buildWaterPendingIntent(
            reminderId = reminderId,
            reminderTime = reminderTime,
            intervalMinutes = intervalMinutes,
            noCreate = false
        ) ?: run {
            Log.e(TAG, "water schedule failed: pendingIntent null, id=$reminderId")
            return
        }

        scheduleAlarm(triggerAtMillis, pendingIntent)
        Log.i(
            TAG,
            "water scheduled: id=$reminderId, time=$reminderTime, interval=$intervalMinutes, source=$source"
        )
    }

    private fun cancelAllMealReminders() {
        MealReminderType.entries.forEach { cancelMealReminder(it) }
    }

    private fun cancelMealReminder(reminderType: MealReminderType) {
        MealReminderWorker.cancelReminder(context, reminderType)

        val pendingIntent = buildMealPendingIntent(
            reminderType = reminderType,
            reminderTime = reminderType.defaultTime,
            noCreate = true
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun cancelAllWaterReminders() {
        (0 until MAX_WATER_FIXED_REMINDER_COUNT).forEach { index ->
            cancelWaterReminder("$WATER_REMINDER_ID_FIXED_PREFIX$index")
        }
        cancelWaterReminder(WATER_REMINDER_ID_INTERVAL)
    }

    private fun cancelWaterReminder(reminderId: String) {
        val pendingIntent = buildWaterPendingIntent(
            reminderId = reminderId,
            reminderTime = DEFAULT_WATER_REMINDER_TIME,
            intervalMinutes = 0,
            noCreate = true
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildMealPendingIntent(
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

    private fun buildWaterPendingIntent(
        reminderId: String,
        reminderTime: String,
        intervalMinutes: Int,
        noCreate: Boolean
    ): PendingIntent? {
        val requestCode = waterReminderRequestCode(reminderId)
        val intent = Intent(context, MealReminderAlarmReceiver::class.java).apply {
            action = MealReminderContract.ACTION_WATER_REMINDER
            putExtra(MealReminderContract.EXTRA_WATER_REMINDER_ID, reminderId)
            putExtra(MealReminderContract.EXTRA_REMINDER_TIME, reminderTime)
            putExtra(MealReminderContract.EXTRA_WATER_INTERVAL_MINUTES, intervalMinutes)
        }
        val flags = if (noCreate) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val exactAllowed = canScheduleExactAlarms()
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
        val localTime = parseLocalTime(reminderTime, MealReminderContract.DEFAULT_REMINDER_TIME)

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

    private fun computeNextIntervalTriggerAtMillis(
        intervalMinutes: Int,
        windowStart: String,
        windowEnd: String
    ): Long {
        val safeInterval = intervalMinutes.coerceIn(1, MAX_INTERVAL_MINUTES)
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId).withSecond(0).withNano(0)

        val start = parseLocalTime(windowStart, DEFAULT_WATER_WINDOW_START)
        val end = parseLocalTime(windowEnd, DEFAULT_WATER_WINDOW_END)
        val validWindow = if (end.isAfter(start)) start to end else (LocalTime.parse(DEFAULT_WATER_WINDOW_START) to LocalTime.parse(DEFAULT_WATER_WINDOW_END))

        val startToday = now.withHour(validWindow.first.hour).withMinute(validWindow.first.minute)
        val endToday = now.withHour(validWindow.second.hour).withMinute(validWindow.second.minute)

        val next = when {
            now.isBefore(startToday) -> startToday
            now.isAfter(endToday) || now.isEqual(endToday) -> startToday.plusDays(1)
            else -> {
                val candidate = now.plusMinutes(safeInterval.toLong())
                if (candidate.isAfter(endToday)) startToday.plusDays(1) else candidate
            }
        }

        return next.toInstant().toEpochMilli()
    }

    private fun normalizeReminderTime(rawTime: String?, fallback: String): String {
        if (rawTime.isNullOrBlank()) return fallback
        return runCatching {
            val parsed = LocalTime.parse(rawTime)
            "%02d:%02d".format(parsed.hour, parsed.minute)
        }.getOrDefault(fallback)
    }

    private fun parseReminderTimes(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return Regex("\\b([01]\\d|2[0-3]):([0-5]\\d)\\b")
            .findAll(raw)
            .map { it.value }
            .distinct()
            .take(MAX_WATER_FIXED_REMINDER_COUNT)
            .toList()
    }

    private fun parseLocalTime(raw: String?, fallback: String): LocalTime {
        val normalized = normalizeReminderTime(raw, fallback)
        return runCatching { LocalTime.parse(normalized) }
            .getOrElse { LocalTime.parse(fallback) }
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

    private fun shouldSuppressDuplicateTrigger(keySuffix: String): Boolean {
        val key = "last_trigger_$keySuffix"
        val now = System.currentTimeMillis()
        val last = runtimePrefs.getLong(key, 0L)
        val duplicate = now - last < DUPLICATE_WINDOW_MILLIS
        if (!duplicate) {
            runtimePrefs.edit().putLong(key, now).apply()
        }
        return duplicate
    }

    private fun waterReminderRequestCode(reminderId: String): Int {
        return when {
            reminderId == WATER_REMINDER_ID_INTERVAL -> WATER_REQUEST_CODE_INTERVAL
            reminderId.startsWith(WATER_REMINDER_ID_FIXED_PREFIX) -> {
                val index = reminderId.removePrefix(WATER_REMINDER_ID_FIXED_PREFIX).toIntOrNull() ?: 0
                WATER_REQUEST_CODE_FIXED_BASE + index.coerceIn(0, MAX_WATER_FIXED_REMINDER_COUNT - 1)
            }
            else -> WATER_REQUEST_CODE_INTERVAL
        }
    }

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val PREFS_NAME = "meal_notification_runtime"
        private const val DUPLICATE_WINDOW_MILLIS = 120_000L

        private const val WATER_REMINDER_ID_FIXED_PREFIX = "fixed_"
        private const val WATER_REMINDER_ID_INTERVAL = "interval"
        private const val MAX_WATER_FIXED_REMINDER_COUNT = 8
        private const val MAX_INTERVAL_MINUTES = 24 * 60
        private const val DEFAULT_WATER_REMINDER_TIME = "10:00"
        private const val DEFAULT_WATER_WINDOW_START = "09:00"
        private const val DEFAULT_WATER_WINDOW_END = "21:00"

        private const val WATER_REQUEST_CODE_FIXED_BASE = 3100
        private const val WATER_REQUEST_CODE_INTERVAL = 3199

        private const val KEY_LAST_WATER_WINDOW_START = "last_water_window_start"
        private const val KEY_LAST_WATER_WINDOW_END = "last_water_window_end"
    }
}
