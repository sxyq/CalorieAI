package com.calorieai.app.service.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 历史兼容 Worker：
 * 旧版本使用 WorkManager 发送餐次提醒，这里只保留清理入口和兜底触发处理。
 */
@HiltWorker
class MealReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val rawMealType = inputData.getString(KEY_MEAL_TYPE)
        val rawReminderTime = inputData.getString(KEY_REMINDER_TIME)
        return try {
            notificationScheduler.onMealReminderTriggered(
                rawMealType = rawMealType,
                rawReminderTime = rawReminderTime,
                source = "fallback_worker"
            )
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "legacy worker failed", t)
            Result.retry()
        }
    }

    companion object {
        const val KEY_MEAL_TYPE = "meal_type"
        const val KEY_REMINDER_TIME = "reminder_time"

        private const val WORK_TAG_BREAKFAST = "breakfast_reminder"
        private const val WORK_TAG_LUNCH = "lunch_reminder"
        private const val WORK_TAG_DINNER = "dinner_reminder"
        private const val WORK_NAME_PREFIX = "meal_reminder_fallback_"
        private const val TAG = "MealReminderWorker"

        fun scheduleReminder(
            context: Context,
            reminderType: MealReminderType,
            reminderTime: String,
            triggerAtMillis: Long
        ) {
            val delayMillis = (triggerAtMillis - System.currentTimeMillis())
                .coerceAtLeast(TimeUnit.SECONDS.toMillis(10))

            val request = OneTimeWorkRequestBuilder<MealReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(tagFor(reminderType))
                .setInputData(
                    workDataOf(
                        KEY_MEAL_TYPE to reminderType.name,
                        KEY_REMINDER_TIME to reminderTime
                    )
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName(reminderType),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancelReminder(context: Context, reminderType: MealReminderType) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(uniqueWorkName(reminderType))
            workManager.cancelAllWorkByTag(tagFor(reminderType))
        }

        fun cancelAllReminders(context: Context) {
            MealReminderType.entries.forEach { reminderType ->
                cancelReminder(context, reminderType)
            }
        }

        private fun uniqueWorkName(reminderType: MealReminderType): String {
            return WORK_NAME_PREFIX + reminderType.name
        }

        private fun tagFor(reminderType: MealReminderType): String {
            return when (reminderType) {
                MealReminderType.BREAKFAST -> WORK_TAG_BREAKFAST
                MealReminderType.LUNCH -> WORK_TAG_LUNCH
                MealReminderType.DINNER -> WORK_TAG_DINNER
            }
        }
    }
}
