package com.calorieai.app.service.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodTextImportPostProcessorTest {

    @Test
    fun process_acceptsNameAndWeightOnlyItems() {
        val raw = """
            {
              "items": [
                {"foodName": "tomato", "estimatedWeight": "100g"},
                {"foodName": "cucumber", "estimatedWeight": "140g"},
                {"foodName": "egg", "estimatedWeight": "50g"}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "100g tomato 140g cucumber 50g egg"
        )

        assertEquals(3, items.size)
        assertEquals("tomato", items[0].foodName)
        assertEquals(100, items[0].estimatedWeight)
        assertEquals("cucumber", items[1].foodName)
        assertEquals(140, items[1].estimatedWeight)
        assertEquals("egg", items[2].foodName)
        assertEquals(50, items[2].estimatedWeight)
    }

    @Test
    fun process_backfillsMissingWeightFromInputHints() {
        val raw = """
            {
              "items": [
                {"foodName": "tomato"},
                {"foodName": "cucumber"},
                {"foodName": "egg"}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "100g tomato 140g cucumber 50g egg"
        )

        assertEquals(3, items.size)
        assertEquals(listOf(100, 140, 50), items.map { it.estimatedWeight })
    }

    @Test
    fun process_fallsBackToExplicitInputItemsWhenResponseIsMalformed() {
        val items = FoodTextImportPostProcessor.process(
            responseText = "analysis: tomato, cucumber, egg",
            foodDescription = "100g tomato 140g cucumber 50g egg"
        )

        assertEquals(3, items.size)
        assertEquals(listOf("tomato", "cucumber", "egg"), items.map { it.foodName })
        assertEquals(listOf(100, 140, 50), items.map { it.estimatedWeight })
        assertTrue(items.all { it.calories == 0f })
    }

    @Test
    fun process_preservesNutritionAndComputesCaloriesFromMacros() {
        val raw = """
            {
              "items": [
                {"foodName": "tomato", "estimatedWeight": 100, "protein": 1.0, "carbs": 3.9, "fat": 0.2},
                {"foodName": "cucumber", "estimatedWeight": 140, "protein": 1.0, "carbs": 5.0, "fat": 0.2},
                {"foodName": "egg", "estimatedWeight": 50, "protein": 6.3, "carbs": 0.6, "fat": 5.3}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "100g tomato 140g cucumber 50g egg"
        )

        assertEquals(3, items.size)
        assertTrue(items.all { it.calories > 0f })
    }

    @Test
    fun process_supportsOtherExplicitWeightCombos() {
        val raw = """
            {
              "items": [
                {"foodName": "lettuce"},
                {"foodName": "corn"},
                {"foodName": "chicken breast"}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "120g lettuce 80g corn 30g chicken breast"
        )

        assertEquals(3, items.size)
        assertEquals(listOf("lettuce", "corn", "chicken breast"), items.map { it.foodName })
        assertEquals(listOf(120, 80, 30), items.map { it.estimatedWeight })
    }

    @Test
    fun process_supportsCommaAndPlusSeparatedInputHints() {
        val malformed = "analysis: tomato, cucumber, egg"

        val commaItems = FoodTextImportPostProcessor.process(
            responseText = malformed,
            foodDescription = "100g tomato,140g cucumber,50g egg"
        )
        val plusItems = FoodTextImportPostProcessor.process(
            responseText = malformed,
            foodDescription = "100g tomato+140g cucumber+50g egg"
        )

        assertEquals(listOf(100, 140, 50), commaItems.map { it.estimatedWeight })
        assertEquals(listOf(100, 140, 50), plusItems.map { it.estimatedWeight })
        assertEquals(listOf("tomato", "cucumber", "egg"), commaItems.map { it.foodName })
        assertEquals(listOf("tomato", "cucumber", "egg"), plusItems.map { it.foodName })
    }

    @Test
    fun process_appendsMissingInputHintsWhenResponseIsTruncated() {
        val raw = """
            {
              "items": [
                {"foodName": "tomato", "estimatedWeight": 100, "calories": 18},
                {"foodName": "cucumber", "estimatedWeight": 140, "calories": 21}
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "100g tomato 140g cucumber 50g egg"
        )

        assertEquals(3, items.size)
        assertEquals(listOf("tomato", "cucumber", "egg"), items.map { it.foodName })
        assertEquals(listOf(100, 140, 50), items.map { it.estimatedWeight })
    }

    @Test
    fun process_fallsBackToSinglePlaceholderForNonSplitInput() {
        val items = FoodTextImportPostProcessor.process(
            responseText = "{\"items\":[{",
            foodDescription = "tomato egg rice bowl"
        )

        assertEquals(1, items.size)
        assertEquals("tomato egg rice bowl", items.first().foodName)
    }

    @Test
    fun process_replacesFieldLikeFoodNameWithInputHint() {
        val raw = """
            {
              "items": [
                {"foodName": "estimatedWeight", "estimatedWeight": 40, "calories": 150},
                {"foodName": "banana", "estimatedWeight": 120}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "250g yogurt 40g oats 120g banana"
        )

        assertEquals(3, items.size)
        assertEquals(listOf("yogurt", "oats", "banana"), items.map { it.foodName })
        assertEquals(listOf(250, 40, 120), items.map { it.estimatedWeight })
    }

    @Test
    fun process_prefersBestSingleItemInsteadOfMergingDuplicateFragments() {
        val raw = """
            {
              "items": [
                {"foodName": "burger meal", "estimatedWeight": 450, "calories": 780, "protein": 35, "carbs": 105, "fat": 32},
                {"foodName": "burger meal", "estimatedWeight": 500, "calories": 850, "protein": 35, "carbs": 80, "fat": 35},
                {"foodName": "burger meal", "estimatedWeight": 350, "calories": 650, "protein": 25, "carbs": 50, "fat": 28}
              ]
            }
        """.trimIndent()

        val items = FoodTextImportPostProcessor.process(
            responseText = raw,
            foodDescription = "burger meal"
        )

        assertEquals(1, items.size)
        assertEquals("burger meal", items.first().foodName)
        assertEquals(500, items.first().estimatedWeight)
        assertEquals(850f, items.first().calories, 0.001f)
    }
}
