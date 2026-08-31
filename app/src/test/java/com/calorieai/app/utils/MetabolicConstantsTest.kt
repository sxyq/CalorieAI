package com.calorieai.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class MetabolicConstantsTest {

    @Test
    fun getBmiCategory_handlesMissingValueAndBoundaries() {
        assertEquals("未设置", MetabolicConstants.getBmiCategory(null))
        assertEquals("偏瘦", MetabolicConstants.getBmiCategory(18.49f))
        assertEquals("正常", MetabolicConstants.getBmiCategory(18.5f))
        assertEquals("正常", MetabolicConstants.getBmiCategory(23.99f))
        assertEquals("偏胖", MetabolicConstants.getBmiCategory(24f))
        assertEquals("偏胖", MetabolicConstants.getBmiCategory(27.99f))
        assertEquals("肥胖", MetabolicConstants.getBmiCategory(28f))
    }

    @Test
    fun dateRanges_areLeftClosedAndRightOpen() {
        val zone = ZoneOffset.UTC
        val day = DateUtils.getDayRangeExclusive(LocalDate.of(2024, 2, 29), zone)
        val month = DateUtils.getMonthRangeExclusive(LocalDate.of(2024, 2, 29), zone)

        assertEquals(1709164800000L, day.startInclusive)
        assertEquals(1709251200000L, day.endExclusive)
        assertEquals(1706745600000L, month.startInclusive)
        assertEquals(1709251200000L, month.endExclusive)
    }
}
