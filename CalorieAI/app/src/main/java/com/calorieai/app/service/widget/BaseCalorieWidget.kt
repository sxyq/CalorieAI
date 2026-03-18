package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.calorieai.app.MainActivity
import com.calorieai.app.R

abstract class BaseCalorieWidget : AppWidgetProvider() {

    abstract val layoutResId: Int
    abstract val pendingIntentRequestCode: Int

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    open fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, layoutResId)
        bindBasicData(views)
        setupClickIntent(context, views)
        bindExtraData(views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    protected open fun bindBasicData(views: RemoteViews) {
        views.setTextViewText(R.id.widget_calories, "0")
        views.setTextViewText(R.id.widget_goal, "/ 2000")
    }

    protected open fun bindExtraData(views: RemoteViews) {}

    protected fun setupClickIntent(context: Context, views: RemoteViews) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            pendingIntentRequestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
}
