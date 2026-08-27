package com.calorieai.app.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object DateUtils {

    fun getDayRange(date: LocalDate): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return startOfDay to endOfDay
    }

    fun getDayRange(timestamp: Long): Pair<Long, Long> {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        return getDayRange(date)
    }

    fun getWeekDayLabel(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }
    
    fun getRelativeDateLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "今天"
            date == today.minusDays(1) -> "昨天"
            date == today.minusDays(2) -> "前天"
            date == today.plusDays(1) -> "明天"
            date == today.plusDays(2) -> "后天"
            else -> ""
        }
    }
}
