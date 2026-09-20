package com.calorieai.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class UiProfileTest {
    @Test
    fun fullProfileExposesAllMainTabs() {
        assertEquals(
            listOf(MainTab.HOME, MainTab.RECIPES, MainTab.OVERVIEW, MainTab.MY),
            UiProfile(isSimplified = false).visibleTabs
        )
    }

    @Test
    fun simplifiedProfileOnlyExposesHomeAndMy() {
        assertEquals(
            listOf(MainTab.HOME, MainTab.MY),
            UiProfile(isSimplified = true).visibleTabs
        )
    }

    @Test
    fun simplifiedProfileDisablesRestrictedPresentationFeatures() {
        val profile = UiProfile(isSimplified = true)

        assertEquals(false, profile.allowsRecipes)
        assertEquals(false, profile.allowsOverview)
        assertEquals(false, profile.allowsWater)
        assertEquals(false, profile.allowsExercise)
        assertEquals(false, profile.allowsAi)
        assertEquals(false, profile.allowsNutritionOcr)
        assertEquals(true, profile.showsSimplifiedStats)
    }

    @Test
    fun fullProfileRestoresRestrictedPresentationFeatures() {
        val profile = UiProfile(isSimplified = false)

        assertEquals(true, profile.allowsRecipes)
        assertEquals(true, profile.allowsOverview)
        assertEquals(true, profile.allowsWater)
        assertEquals(true, profile.allowsExercise)
        assertEquals(true, profile.allowsAi)
        assertEquals(true, profile.allowsNutritionOcr)
        assertEquals(false, profile.showsSimplifiedStats)
    }
}
