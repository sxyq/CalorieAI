package com.calorieai.app.service.ai.common

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResponseSanitizerTest {
    private val gson = Gson()

    @Test
    fun parseFoodItems_handlesWrappedChineseFieldsAndUnits() {
        val raw = """
            ```json
            {
              "data": {
                "items": [
                  {
                    "食物名称": "米饭",
                    "热量": "230kcal",
                    "蛋白质": "4.3g",
                    "碳水": "50g",
                    "脂肪": "0.5g"
                  }
                ]
              }
            }
            ```
        """.trimIndent()

        val parsed = AIResponseSanitizer.parseFoodItems(raw, gson)

        assertEquals(1, parsed.size)
        assertEquals("米饭", parsed.first().foodName)
        assertTrue(parsed.first().calories >= 200f)
        assertTrue(parsed.first().carbs > 0f)
    }

    @Test
    fun parseFoodItems_handlesSingleQuotesAndTrailingComma() {
        val raw = """
            [
              {'food_name':'鸡蛋','calories':'86','protein':'7g','carbs':'1','fat':'6',},
            ]
        """.trimIndent()

        val parsed = AIResponseSanitizer.parseFoodItems(raw, gson)

        assertEquals(1, parsed.size)
        val item = parsed.first()
        assertEquals("鸡蛋", item.foodName)
        assertTrue(item.calories > 0f)
        assertTrue(item.protein > 0f)
        assertTrue(item.fat > 0f)
    }
}
