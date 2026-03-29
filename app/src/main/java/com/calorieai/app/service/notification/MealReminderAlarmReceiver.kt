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
        if (action != MealReminderContract.ACTION_MEAL_REMINDER) {
            Log.w(TAG, "ignored action=$action")
            return
        }

        val rawMealType = intent?.getStringExtra(MealReminderContract.EXTRA_MEAL_TYPE)
        val rawReminderTime = intent?.getStringExtra(MealReminderContract.EXTRA_REMINDER_TIME)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    EntryPointAccessor::class.java
                )
                entryPoint.notificationScheduler().onMealReminderTriggered(
                    rawMealType = rawMealType,
                    rawReminderTime = rawReminderTime,
                    source = action
                )
            } catch (t: Throwable) {
                Log.e(
                    TAG,
                    "handle alarm failed: mealType=$rawMealType, reminderTime=$rawReminderTime",
                    t
                )
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
