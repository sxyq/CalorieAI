package com.calorieai.app.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MealReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action.orEmpty()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    EntryPointAccessor::class.java
                )
                val scheduler = entryPoint.notificationScheduler()

                when (action) {
                    MealReminderContract.ACTION_MEAL_REMINDER -> {
                        val rawMealType = intent?.getStringExtra(MealReminderContract.EXTRA_MEAL_TYPE)
                        val rawReminderTime = intent?.getStringExtra(MealReminderContract.EXTRA_REMINDER_TIME)
                        scheduler.onMealReminderTriggered(
                            rawMealType = rawMealType,
                            rawReminderTime = rawReminderTime,
                            source = action
                        )
                    }

                    MealReminderContract.ACTION_WATER_REMINDER -> {
                        val reminderId = intent?.getStringExtra(MealReminderContract.EXTRA_WATER_REMINDER_ID)
                        val reminderTime = intent?.getStringExtra(MealReminderContract.EXTRA_REMINDER_TIME)
                        val intervalMinutes = intent?.getIntExtra(
                            MealReminderContract.EXTRA_WATER_INTERVAL_MINUTES,
                            0
                        ) ?: 0
                        scheduler.onWaterReminderTriggered(
                            rawReminderId = reminderId,
                            rawReminderTime = reminderTime,
                            rawIntervalMinutes = intervalMinutes,
                            source = action
                        )
                    }

                    else -> {
                        Log.w(TAG, "ignored action=$action")
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "handle alarm failed, action=$action", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryPointAccessor {
        fun notificationScheduler(): NotificationScheduler
    }

    companion object {
        private const val TAG = "MealReminderAlarmRcvr"
    }
}
