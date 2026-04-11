package com.calorieai.app.ui.screens.ai

enum class AIQuickAction {
    CALORIE_ASSESSMENT,
    MEAL_PLANNING,
    WEEKLY_MEAL_PLANNING,
    NEXT_MEAL_RECOMMENDATION,
    HEALTH_CONSULT
}

object QuickActionRouter {
    fun dispatch(viewModel: AIChatViewModel, action: AIQuickAction) {
        when (action) {
            AIQuickAction.CALORIE_ASSESSMENT -> viewModel.startCalorieAssessment()
            AIQuickAction.MEAL_PLANNING -> viewModel.startMealPlanning()
            AIQuickAction.WEEKLY_MEAL_PLANNING -> viewModel.startWeeklyMealPlanning()
            AIQuickAction.NEXT_MEAL_RECOMMENDATION -> viewModel.startNextMealRecommendation()
            AIQuickAction.HEALTH_CONSULT -> viewModel.startHealthConsult()
        }
    }
}
