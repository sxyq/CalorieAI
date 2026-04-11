package com.calorieai.app.domain.stats

import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.RecipePlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StatsSnapshotUseCaseTest {
    private val useCase = StatsSnapshotUseCase()
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun buildBasicSnapshot_returnsHeatmapAndTopFoods() {
        val today = LocalDate.now()
        val foodRecords = listOf(
            foodRecord(today.minusDays(1), "Rice", 300, MealType.LUNCH),
            foodRecord(today.minusDays(1), "Chicken", 220, MealType.DINNER),
            foodRecord(today, "Rice", 320, MealType.LUNCH),
            foodRecord(today, "Egg", 90, MealType.BREAKFAST),
            foodRecord(today, "Egg", 95, MealType.BREAKFAST_SNACK)
        )
        val exerciseRecords = listOf(
            ExerciseRecord(
                exerciseType = ExerciseType.RUNNING,
                durationMinutes = 30,
                caloriesBurned = 280,
                recordTime = toEpochMillis(today)
            )
        )

        val snapshot = useCase.buildBasicSnapshot(
            foodRecords = foodRecords,
            exerciseRecords = exerciseRecords,
            favoriteRecipes = listOf(
                FavoriteRecipe(
                    sourceRecordId = "src1",
                    foodName = "Rice",
                    userInput = "one bowl of rice",
                    totalCalories = 300,
                    protein = 6f,
                    carbs = 66f,
                    fat = 1f,
                    useCount = 3
                )
            ),
            pantryIngredients = emptyList(),
            recipePlans = listOf(
                RecipePlan(
                    title = "Test plan",
                    startDateEpochDay = today.toEpochDay(),
                    endDateEpochDay = today.plusDays(2).toEpochDay(),
                    menuText = "Simple menu"
                )
            ),
            selectedOverviewDate = today,
            selectedMonthOffset = 0,
            targetCalories = 2000,
            bmr = 1500,
            tdee = 2100,
            userWeight = 70f,
            weeklyGoalDays = 5
        )

        assertEquals(140, snapshot.dailyMealRecords.size)
        assertTrue(snapshot.dailyMealRecords.any { it.level > 0 })
        assertTrue(snapshot.topFoodRows.isNotEmpty())
        assertEquals("Rice", snapshot.topFoodRows.first().foodName)
        assertTrue(snapshot.foodRecordTableRows.isNotEmpty())
    }

    private fun foodRecord(
        date: LocalDate,
        name: String,
        calories: Int,
        mealType: MealType
    ): FoodRecord {
        return FoodRecord(
            foodName = name,
            userInput = "$name test",
            totalCalories = calories,
            protein = 10f,
            carbs = 20f,
            fat = 5f,
            mealType = mealType,
            recordTime = toEpochMillis(date)
        )
    }

    private fun toEpochMillis(date: LocalDate): Long {
        return date.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
    }
}
