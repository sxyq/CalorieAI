package com.calorieai.app.service.widget

import android.content.Context
import android.widget.RemoteViews
import com.calorieai.app.R

class CalorieWidget : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie
    override val pendingIntentRequestCode: Int = 100
    override val widgetType: String = "legacy_overview"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_medium

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        views.setTextViewText(R.id.widget_calories, snapshot.calorieIntake.toString())
        views.setTextViewText(R.id.widget_goal, "/ ${snapshot.calorieGoal}")
    }
}
