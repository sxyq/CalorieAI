package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.calorieai.app.R

class CalorieWidgetSmall : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_small
    override val pendingIntentRequestCode: Int = 0

    override fun bindBasicData(views: RemoteViews) {
        views.setTextViewText(R.id.widget_calories, "0")
        views.setTextViewText(R.id.widget_goal, "/ 2000 千卡")
    }
}

class CalorieWidgetMedium : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_medium
    override val pendingIntentRequestCode: Int = 1
}

class CalorieWidgetLarge : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_large
    override val pendingIntentRequestCode: Int = 2

    override fun bindBasicData(views: RemoteViews) {
        views.setTextViewText(R.id.widget_calories, "0")
        views.setTextViewText(R.id.widget_goal, "目标: 2000 千卡")
    }

    override fun bindExtraData(views: RemoteViews) {
        views.setTextViewText(R.id.widget_date, java.text.SimpleDateFormat("M月d日", java.util.Locale.getDefault()).format(java.util.Date()))
        views.setTextViewText(R.id.widget_protein, "0g")
        views.setTextViewText(R.id.widget_carbs, "0g")
        views.setTextViewText(R.id.widget_fat, "0g")
    }
}
