package com.calorieai.app.service.widget

import android.content.Context
import android.widget.RemoteViews
import com.calorieai.app.R
import kotlin.math.max
import kotlin.math.roundToInt

class CalorieWidgetSmall : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_small
    override val pendingIntentRequestCode: Int = 200
    override val widgetType: String = "small_food"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_small_food

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        views.setTextViewText(R.id.widget_small_icon, "🍱")
        views.setTextViewText(R.id.widget_small_title, "今日摄入")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.calorieIntake} 千卡")
        views.setTextViewText(R.id.widget_small_subtitle, "目标 ${snapshot.calorieGoal} 千卡")
    }
}

class WaterWidgetSmall : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_small
    override val pendingIntentRequestCode: Int = 210
    override val widgetType: String = "small_water"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_small_water

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        views.setTextViewText(R.id.widget_small_icon, "💧")
        views.setTextViewText(R.id.widget_small_title, "饮水记录")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.waterIntakeMl} ml")
        views.setTextViewText(R.id.widget_small_subtitle, "目标 ${snapshot.waterGoalMl} ml")
    }
}

class ExerciseWidgetSmall : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_small
    override val pendingIntentRequestCode: Int = 220
    override val widgetType: String = "small_exercise"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_small_exercise

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        views.setTextViewText(R.id.widget_small_icon, "🔥")
        views.setTextViewText(R.id.widget_small_title, "运动消耗")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.exerciseBurned} 千卡")
        views.setTextViewText(R.id.widget_small_subtitle, "${snapshot.exerciseMinutes} 分钟")
    }
}

class NutritionWidgetSmall : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_small
    override val pendingIntentRequestCode: Int = 230
    override val widgetType: String = "small_nutrition"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_small_nutrition

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        val macroCalories = (snapshot.protein * 4f + snapshot.carbs * 4f + snapshot.fat * 9f).roundToInt()
        views.setTextViewText(R.id.widget_small_icon, "🥗")
        views.setTextViewText(R.id.widget_small_title, "营养摄入")
        views.setTextViewText(R.id.widget_small_value, "${macroCalories} 千卡")
        views.setTextViewText(
            R.id.widget_small_subtitle,
            "P${snapshot.protein.roundToInt()} C${snapshot.carbs.roundToInt()} F${snapshot.fat.roundToInt()} g"
        )
    }
}

class CalorieWidgetMedium : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_medium
    override val pendingIntentRequestCode: Int = 300
    override val widgetType: String = "medium_overview"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_medium

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        views.setTextViewText(R.id.widget_date, snapshot.dateLabel)
        views.setTextViewText(R.id.widget_calories, snapshot.calorieIntake.toString())
        views.setTextViewText(R.id.widget_goal, "/ ${snapshot.calorieGoal} 千卡")
        views.setTextViewText(R.id.widget_water, "${snapshot.waterIntakeMl} ml")
        views.setTextViewText(R.id.widget_exercise, "${snapshot.exerciseBurned} 千卡")
        views.setTextViewText(R.id.widget_meals, "${snapshot.mealCount} 条记录")
    }
}

class CalorieWidgetLarge : BaseCalorieWidget() {

    override val layoutResId: Int = R.layout.widget_calorie_large
    override val pendingIntentRequestCode: Int = 400
    override val widgetType: String = "large_dashboard"
    override val containerBackgroundResId: Int = R.drawable.widget_bg_large

    override fun bindData(
        context: Context,
        views: RemoteViews,
        snapshot: WidgetDataProvider.TodaySnapshot
    ) {
        val remaining = snapshot.remainingCalories
        val remainingText = if (remaining >= 0) {
            "剩余 ${remaining} 千卡"
        } else {
            "超出 ${kotlin.math.abs(remaining)} 千卡"
        }

        views.setTextViewText(R.id.widget_date, snapshot.dateLabel)
        views.setTextViewText(R.id.widget_calories, snapshot.calorieIntake.toString())
        views.setTextViewText(R.id.widget_goal, "目标 ${snapshot.calorieGoal} 千卡")
        views.setTextViewText(R.id.widget_remaining, remainingText)
        views.setTextViewText(R.id.widget_water, "${snapshot.waterIntakeMl} ml")
        views.setTextViewText(R.id.widget_water_goal, "目标 ${snapshot.waterGoalMl} ml")
        views.setTextViewText(R.id.widget_exercise, "${snapshot.exerciseBurned} 千卡")
        views.setTextViewText(R.id.widget_exercise_minutes, "${snapshot.exerciseMinutes} 分钟")
        views.setTextViewText(R.id.widget_meals, "${snapshot.mealCount} 条饮食记录")
        views.setTextViewText(R.id.widget_protein, "${max(snapshot.protein, 0f).roundToInt()} g")
        views.setTextViewText(R.id.widget_carbs, "${max(snapshot.carbs, 0f).roundToInt()} g")
        views.setTextViewText(R.id.widget_fat, "${max(snapshot.fat, 0f).roundToInt()} g")
    }
}
