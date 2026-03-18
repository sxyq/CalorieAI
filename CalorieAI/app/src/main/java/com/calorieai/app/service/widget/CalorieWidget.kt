package com.calorieai.app.service.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.calorieai.app.R

class CalorieWidget : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie
    override val pendingIntentRequestCode: Int = 0
}
