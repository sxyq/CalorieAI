package com.calorieai.app.service.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action.orEmpty()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (
                    action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
                    !canScheduleExactAlarms(context)
                ) {
                    Log.w(TAG, "reschedule skipped: exact alarm still not granted")
                    return@launch
                }

                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    EntryPointAccessor::class.java
                )
                val settings = entryPoint.userSettingsRepository().getSettingsOnce()
                if (settings == null) {
                    Log.w(TAG, "reschedule skipped: settings null, action=$action")
                    return@launch
                }

                entryPoint.reminderResyncCoordinator().sync(
                    settings = settings,
                    source = "broadcast:$action",
                    force = true
                )
                Log.i(TAG, "reschedule success, action=$action")
            } catch (t: Throwable) {
                Log.e(TAG, "reschedule failed, action=$action", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryPointAccessor {
        fun userSettingsRepository(): UserSettingsRepository
        fun reminderResyncCoordinator(): ReminderResyncCoordinator
    }

    companion object {
        private const val TAG = "NotificationReschedule"

        private fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            return runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
        }
    }
}

