package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.calorieai.app.MainActivity
import com.calorieai.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class BaseCalorieWidget : AppWidgetProvider() {

    abstract val layoutResId: Int
    abstract val pendingIntentRequestCode: Int
    abstract val widgetType: String
    open val containerBackgroundResId: Int? = null
    open val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        refreshWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        refreshWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun refreshWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        coroutineScope.launch {
            try {
                val snapshot = WidgetDataProvider.loadTodaySnapshot(context)
                appWidgetIds.forEach { appWidgetId ->
                    val views = RemoteViews(context.packageName, layoutResId)
                    containerBackgroundResId?.let { bgRes ->
                        views.setInt(R.id.widget_container, "setBackgroundResource", bgRes)
                    }
                    bindData(context, views, snapshot)
                    setupClickIntent(context, views, appWidgetId)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    protected abstract fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    )

    protected fun setupClickIntent(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra("from_widget", true)
            .putExtra("widget_type", widgetType)
            .putExtra("app_widget_id", appWidgetId)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            pendingIntentRequestCode + appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
}
