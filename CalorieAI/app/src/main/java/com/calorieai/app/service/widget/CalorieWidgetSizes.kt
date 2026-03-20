package com.calorieai.app.service.widget

import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.calorieai.app.R
import kotlin.math.max
import kotlin.math.min
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
        val progress = calculateProgress(snapshot.calorieIntake, snapshot.calorieGoal)
        views.setTextViewText(R.id.widget_small_icon, "🍱")
        views.setTextViewText(R.id.widget_small_title, "今日摄入")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.calorieIntake} 千卡")
        views.setTextViewText(R.id.widget_small_subtitle, "目标 ${snapshot.calorieGoal} 千卡")
        views.setTextViewText(R.id.widget_small_tag, "$progress%")
        views.setProgressBar(R.id.widget_small_progress, 100, progress, false)
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
        val progress = calculateProgress(snapshot.waterIntakeMl, snapshot.waterGoalMl)
        views.setTextViewText(R.id.widget_small_icon, "💧")
        views.setTextViewText(R.id.widget_small_title, "饮水记录")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.waterIntakeMl} ml")
        views.setTextViewText(R.id.widget_small_subtitle, "目标 ${snapshot.waterGoalMl} ml")
        views.setTextViewText(R.id.widget_small_tag, "$progress%")
        views.setProgressBar(R.id.widget_small_progress, 100, progress, false)
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
        val progress = calculateProgress(snapshot.exerciseBurned, 300)
        views.setTextViewText(R.id.widget_small_icon, "🔥")
        views.setTextViewText(R.id.widget_small_title, "运动消耗")
        views.setTextViewText(R.id.widget_small_value, "${snapshot.exerciseBurned} 千卡")
        views.setTextViewText(R.id.widget_small_subtitle, "${snapshot.exerciseMinutes} 分钟 · 目标300千卡")
        views.setTextViewText(R.id.widget_small_tag, "$progress%")
        views.setProgressBar(R.id.widget_small_progress, 100, progress, false)
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
        val progress = calculateProgress(macroCalories, snapshot.calorieGoal)
        views.setTextViewText(R.id.widget_small_icon, "🥗")
        views.setTextViewText(R.id.widget_small_title, "营养摄入")
        views.setTextViewText(R.id.widget_small_value, "${macroCalories} 千卡")
        views.setTextViewText(
            R.id.widget_small_subtitle,
            "P${snapshot.protein.roundToInt()} C${snapshot.carbs.roundToInt()} F${snapshot.fat.roundToInt()} g"
        )
        views.setTextViewText(R.id.widget_small_tag, "$progress%")
        views.setProgressBar(R.id.widget_small_progress, 100, progress, false)
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
        val calorieProgress = calculateProgress(snapshot.calorieIntake, snapshot.calorieGoal)
        views.setTextViewText(R.id.widget_date, snapshot.dateLabel)
        views.setTextViewText(R.id.widget_calories, snapshot.calorieIntake.toString())
        views.setTextViewText(R.id.widget_goal, "/ ${snapshot.calorieGoal} 千卡")
        views.setTextViewText(R.id.widget_water, "${snapshot.waterIntakeMl} ml")
        views.setTextViewText(R.id.widget_exercise, "${snapshot.exerciseBurned} 千卡")
        views.setTextViewText(R.id.widget_meals, "${snapshot.mealCount} 条记录")
        views.setTextViewText(R.id.widget_status_chip, "进度 $calorieProgress%")
        views.setProgressBar(R.id.widget_calorie_progress, 100, calorieProgress, false)
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

        val calorieProgress = calculateProgress(snapshot.calorieIntake, snapshot.calorieGoal)
        val waterProgress = calculateProgress(snapshot.waterIntakeMl, snapshot.waterGoalMl)
        val exerciseProgress = calculateProgress(snapshot.exerciseBurned, 300)
        val proteinProgress = calculateProgress(max(snapshot.protein, 0f).roundToInt(), 100)
        val carbsProgress = calculateProgress(max(snapshot.carbs, 0f).roundToInt(), 250)
        val fatProgress = calculateProgress(max(snapshot.fat, 0f).roundToInt(), 70)

        views.setProgressBar(R.id.widget_calorie_progress, 100, calorieProgress, false)
        views.setProgressBar(R.id.widget_water_progress, 100, waterProgress, false)
        views.setProgressBar(R.id.widget_exercise_progress, 100, exerciseProgress, false)
        views.setProgressBar(R.id.widget_protein_progress, 100, proteinProgress, false)
        views.setProgressBar(R.id.widget_carbs_progress, 100, carbsProgress, false)
        views.setProgressBar(R.id.widget_fat_progress, 100, fatProgress, false)

        if (remaining < 0) {
            views.setTextColor(R.id.widget_remaining, Color.parseColor("#FF9D9D"))
        } else {
            views.setTextColor(R.id.widget_remaining, Color.parseColor("#AEE2FF"))
        }
    }
}

private fun calculateProgress(current: Int, goal: Int): Int {
    if (goal <= 0) return 0
    return min(100, max(0, (current * 100f / goal).roundToInt()))
}
