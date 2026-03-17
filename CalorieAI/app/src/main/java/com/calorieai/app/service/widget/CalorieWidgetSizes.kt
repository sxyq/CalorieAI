package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.calorieai.app.R
import java.text.SimpleDateFormat
import java.util.*

class CalorieWidgetSmall : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calorie_small)
            
            views.setTextViewText(R.id.widget_calories, "0")
            views.setTextViewText(R.id.widget_goal, "/ 2000 千卡")
            
            val intent = Intent(context, com.calorieai.app.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class CalorieWidgetMedium : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calorie_medium)
            
            views.setTextViewText(R.id.widget_calories, "0")
            views.setTextViewText(R.id.widget_goal, "/ 2000")
            
            val intent = Intent(context, com.calorieai.app.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                1,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class CalorieWidgetLarge : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calorie_large)
            
            views.setTextViewText(R.id.widget_calories, "0")
            views.setTextViewText(R.id.widget_goal, "目标: 2000 千卡")
            views.setTextViewText(R.id.widget_date, SimpleDateFormat("M月d日", Locale.getDefault()).format(Date()))
            views.setTextViewText(R.id.widget_protein, "0g")
            views.setTextViewText(R.id.widget_carbs, "0g")
            views.setTextViewText(R.id.widget_fat, "0g")
            
            val intent = Intent(context, com.calorieai.app.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                2,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
