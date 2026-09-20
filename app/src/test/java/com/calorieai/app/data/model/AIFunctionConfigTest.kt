package com.calorieai.app.data.model

import com.calorieai.app.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class AIFunctionConfigTest {
    @Test
    fun everyDefaultFunctionUsesTheBuildConfiguredLunaModel() {
        assertEquals(BuildConfig.DEFAULT_AI_MODEL_ID, AIConfigPresets.DEFAULT_MODEL_ID)
        AIFunctionType.entries.forEach { type ->
            assertEquals(BuildConfig.DEFAULT_AI_MODEL_ID, type.getRecommendedModel())
        }
        assertEquals(
            setOf(BuildConfig.DEFAULT_AI_MODEL_ID),
            AIConfigPresets.ALL_PRESETS.map(AIConfig::modelId).toSet()
        )
    }
}
