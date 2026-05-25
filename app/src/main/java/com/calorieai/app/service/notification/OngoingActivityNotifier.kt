package com.calorieai.app.service.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calorieai.app.MainActivity
import com.calorieai.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class OngoingActivityState(
    val notificationId: Int,
    val title: String,
    val message: String,
    val category: String = NotificationCompat.CATEGORY_STATUS,
    val progressCurrent: Int? = null,
    val progressMax: Int? = null,
    val isIndeterminate: Boolean = true,
    val actionLabel: String? = null,
    val requestPromotedOngoing: Boolean = true
)

@Singleton
class OngoingActivityNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationCapabilityManager: NotificationCapabilityManager
) {
    fun show(state: OngoingActivityState) {
        AppNotificationChannels.ensure(context)
        val manager = NotificationManagerCompat.from(context)
        if (!notificationCapabilityManager.canPostNotifications()) return

        val builder = NotificationCompat.Builder(
            context,
            AppNotificationChannels.CHANNEL_ID_ONGOING_ACTIVITY
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(state.title)
            .setContentText(state.message)
            .setCategory(state.category)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(buildContentIntent(state.notificationId))
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(state.message)
            )

        if (state.progressCurrent != null && state.progressMax != null) {
            builder.setProgress(state.progressMax, state.progressCurrent, state.isIndeterminate)
        }

        state.actionLabel
            ?.takeIf { it.isNotBlank() }
            ?.let { actionLabel ->
                builder.addAction(
                    R.drawable.ic_add,
                    actionLabel,
                    buildContentIntent(state.notificationId)
                )
            }

        if (state.requestPromotedOngoing && notificationCapabilityManager.canPostPromotedNotifications()) {
            builder.addExtras(
                Bundle().apply {
                    putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
                }
            )
            builder.setSubText("Live Update requested")
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= ANDROID_16_API) {
            Log.d(
                TAG,
                "ongoing notification promotable=${notification.hasPromotableCharacteristics()}, " +
                    "requested=${state.requestPromotedOngoing}"
            )
        }

        manager.notify(state.notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun buildContentIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "OngoingActivityNotifier"
        private const val ANDROID_16_API = 36
        private const val EXTRA_REQUEST_PROMOTED_ONGOING =
            "android.requestPromotedOngoing"
    }
}
