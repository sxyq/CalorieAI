package com.calorieai.app.service.notification

import com.calorieai.app.data.model.UserSettings
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ReminderResyncCoordinator @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) {
    private data class SyncRequest(
        val settings: UserSettings,
        val source: String,
        val force: Boolean,
        val signature: String
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requestMutex = Mutex()
    @Volatile
    private var pendingRequest: SyncRequest? = null
    @Volatile
    private var pendingJob: Job? = null
    @Volatile
    private var lastDispatchedSignature: String? = null

    fun sync(
        settings: UserSettings,
        source: String,
        force: Boolean = false
    ) {
        val signature = buildSignature(settings)
        if (!force && signature == lastDispatchedSignature) {
            return
        }

        pendingRequest = SyncRequest(
            settings = settings,
            source = source,
            force = force,
            signature = signature
        )
        pendingJob?.cancel()
        pendingJob = scope.launch {
            if (!force) {
                delay(DEBOUNCE_WINDOW_MS)
            }

            val request = requestMutex.withLock {
                val latest = pendingRequest ?: return@withLock null
                pendingRequest = null
                latest
            } ?: return@launch

            if (!request.force && request.signature == lastDispatchedSignature) {
                return@launch
            }

            notificationScheduler.syncReminders(
                settings = request.settings,
                source = request.source,
                force = request.force
            )
            lastDispatchedSignature = request.signature
        }
    }

    private fun buildSignature(settings: UserSettings): String {
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

    companion object {
        private const val DEBOUNCE_WINDOW_MS = 180L
    }
}
