package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.calorieai.app.R

class CalorieWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // 第一个小组件被添加时
    }

    override fun onDisabled(context: Context) {
        // 最后一个小组件被移除时
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calorie)
            
            // 设置今日摄入（这里需要从数据库读取，简化处理）
            views.setTextViewText(R.id.widget_calories, "0")
            views.setTextViewText(R.id.widget_goal, "/ 2000")
            
            // 设置点击事件 - 打开应用
            val intent = Intent(context, com.calorieai.app.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // 更新小组件
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
