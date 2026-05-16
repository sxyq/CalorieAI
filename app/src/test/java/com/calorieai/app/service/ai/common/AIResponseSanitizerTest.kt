package com.calorieai.app.service.ai.common

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResponseSanitizerTest {

    private val gson = Gson()

    @Test
    fun parseFoodItems_extractsSingleItemFromTruncatedResponse() {
        val raw = """
            {
              "items": [    {
                "foodName": "番茄炒蛋盖饭",
                "estimatedWeight": 500,
                "calories": 280,
                "protein": 12,
                "carbs": 45,
                "fat": 10,
                "fiber": 3，
                "sugar": 5,
                "saturatedFat": 2，
                "cholesterol": 15，
                "sodium": 300,
                "potassium": 400，
                "calcium": 50,
                "iron": 2,
                "vitaminA": 10,
                "vitaminC": 5
        """.trimIndent()

        val items = AIResponseSanitizer.parseFoodItems(raw, gson)

        assertEquals(1, items.size)
        assertEquals("番茄炒蛋盖饭", items.first().foodName)
        assertEquals(500, items.first().estimatedWeight)
        assertEquals(280f, items.first().calories, 0.001f)
    }

    @Test
    fun parseFoodItems_extractsArrayStyleFieldPairs() {
        val raw = """
            {
              "items": [
                {
                  "foodName": "生菜",
                  "estimatedWeight": 120,
                  "calories": 15,
                  ["protein", 1.3],
                  ["carbs", 2.9],
                  ["fat", 0.3]
                }
              ]
            }
        """.trimIndent()

        val items = AIResponseSanitizer.parseFoodItems(raw, gson)

        assertEquals(1, items.size)
        assertEquals("生菜", items.first().foodName)
        assertEquals(120, items.first().estimatedWeight)
        assertEquals(1.3f, items.first().protein, 0.001f)
        assertEquals(2.9f, items.first().carbs, 0.001f)
        assertEquals(0.3f, items.first().fat, 0.001f)
    }

    @Test
    fun parseFoodItems_extractsMultipleItemsFromIncompleteBatch() {
        val raw = """
            {
              "items": [
                {
                  "foodName": "番茄",
                  "estimatedWeight": 100,
                  "calories": 25,
                  "protein": 1.4 "carbs": 4.8,
                  "fat": 0.3
                },
                {
                  "Chinese name food": "黄瓜",
                  "foodName": "黄瓜",
                  "estimatedWeight": 140,
                  "calories": 23,
                  "protein": 1.2
                },
                {
        """.trimIndent()

        val items = AIResponseSanitizer.parseFoodItems(raw, gson)

        assertTrue(items.size >= 2)
        assertEquals("番茄", items.first().foodName)
        assertEquals(100, items.first().estimatedWeight)
        assertEquals("黄瓜", items[1].foodName)
        assertEquals(140, items[1].estimatedWeight)
    }
}
