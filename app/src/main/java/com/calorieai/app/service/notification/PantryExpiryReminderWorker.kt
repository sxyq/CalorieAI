package com.calorieai.app.service.notification

import android.content.Context
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

@HiltWorker
class PantryExpiryReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ingredientName = inputData.getString(KEY_INGREDIENT_NAME).orEmpty()
        if (ingredientName.isBlank()) return Result.failure()

        notificationHelper.showGeneralNotification(
            title = "食材即将过期",
            message = "$ingredientName 即将过期，建议尽快使用或调整菜单计划。"
        )
        return Result.success()
    }

    companion object {
        private const val KEY_INGREDIENT_NAME = "ingredient_name"
        private const val WORK_PREFIX = "pantry_expiry_"

        fun schedule(context: Context, ingredientId: String, ingredientName: String, expiresAt: Long) {
            val now = System.currentTimeMillis()
            val triggerAt = (expiresAt - TimeUnit.DAYS.toMillis(1)).coerceAtLeast(now + TimeUnit.MINUTES.toMillis(1))
            val delay = triggerAt - now

            val request = OneTimeWorkRequestBuilder<PantryExpiryReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(KEY_INGREDIENT_NAME to ingredientName))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_PREFIX + ingredientId,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancel(context: Context, ingredientId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + ingredientId)
        }
    }
}

