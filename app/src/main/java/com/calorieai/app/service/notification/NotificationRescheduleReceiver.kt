package com.calorieai.app.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    EntryPointAccessor::class.java
                )
                val settings = entryPoint.userSettingsRepository().getSettingsOnce()
                if (settings == null) {
                    Log.w(TAG, "reschedule skipped: settings null, action=$action")
                    return@launch
                }

                entryPoint.notificationScheduler().syncMealReminders(
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
        fun notificationScheduler(): NotificationScheduler
    }

    companion object {
        private const val TAG = "NotificationReschedule"
    }
}
