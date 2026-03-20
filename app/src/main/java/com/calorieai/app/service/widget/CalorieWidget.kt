package com.calorieai.app.service.widget

import android.content.Context
import android.widget.RemoteViews
import com.calorieai.app.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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
        val progress = calculateLegacyProgress(snapshot.calorieIntake, snapshot.calorieGoal)
        views.setTextViewText(R.id.widget_calories, snapshot.calorieIntake.toString())
        views.setTextViewText(R.id.widget_goal, "/ ${snapshot.calorieGoal} 千卡")
        views.setTextViewText(R.id.widget_status_chip, "进度 $progress%")
        views.setProgressBar(R.id.widget_calorie_progress, 100, progress, false)
    }
}

private fun calculateLegacyProgress(current: Int, goal: Int): Int {
    if (goal <= 0) return 0
    return min(100, max(0, (current * 100f / goal).roundToInt()))
}
