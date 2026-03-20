package com.calorieai.app.service.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class MealReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val mealTypeName = inputData.getString(KEY_MEAL_TYPE) ?: return Result.failure()
        val reminderTime = inputData.getString(KEY_REMINDER_TIME) ?: return Result.failure()
        val workTag = inputData.getString(KEY_WORK_TAG) ?: return Result.failure()
        val mealType = MealType.valueOf(mealTypeName)

        notificationHelper.showMealReminderNotification(mealType)
        // 每次触发后续约下一次提醒，保证按天循环。
        scheduleReminder(applicationContext, mealType, reminderTime, workTag)

        return Result.success()
    }

    companion object {
        const val KEY_MEAL_TYPE = "meal_type"
        const val KEY_REMINDER_TIME = "reminder_time"
        const val KEY_WORK_TAG = "work_tag"

        private const val WORK_TAG_BREAKFAST = "breakfast_reminder"
        private const val WORK_TAG_LUNCH = "lunch_reminder"
        private const val WORK_TAG_DINNER = "dinner_reminder"

        fun scheduleMealReminders(context: Context, breakfastTime: String, lunchTime: String, dinnerTime: String) {
            cancelAllReminders(context)

            scheduleReminder(context, MealType.BREAKFAST, breakfastTime, WORK_TAG_BREAKFAST)
            scheduleReminder(context, MealType.LUNCH, lunchTime, WORK_TAG_LUNCH)
            scheduleReminder(context, MealType.DINNER, dinnerTime, WORK_TAG_DINNER)
        }

        fun cancelAllReminders(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORK_TAG_BREAKFAST)
            workManager.cancelAllWorkByTag(WORK_TAG_LUNCH)
            workManager.cancelAllWorkByTag(WORK_TAG_DINNER)
        }

        private fun scheduleReminder(context: Context, mealType: MealType, time: String, tag: String) {
            val (hour, minute) = parseTime(time)

            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (targetTime.before(currentTime)) {
                targetTime.add(Calendar.DAY_OF_YEAR, 1)
            }

            val delay = targetTime.timeInMillis - currentTime.timeInMillis

            val inputData = workDataOf(
                KEY_MEAL_TYPE to mealType.name,
                KEY_REMINDER_TIME to time,
                KEY_WORK_TAG to tag
            )

            val reminderWork = OneTimeWorkRequestBuilder<MealReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
        }

        private fun parseTime(time: String): Pair<Int, Int> {
            return try {
                val parts = time.split(":")
                Pair(parts[0].toInt(), parts[1].toInt())
            } catch (_: Exception) {
                Pair(8, 0)
            }
        }
    }
}
