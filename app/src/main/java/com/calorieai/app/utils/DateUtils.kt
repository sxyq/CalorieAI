package com.calorieai.app.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

object DateUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    private val fullFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    
    fun getTodayRange(): Pair<Long, Long> {
        return getDayRange(LocalDate.now())
    }

    fun getDayRange(date: LocalDate): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return startOfDay to endOfDay
    }
    
    fun getWeekRange(): Pair<Long, Long> {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)
        val startTime = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return startTime to endTime
    }
    
    fun getMonthRange(): Pair<Long, Long> {
        val today = LocalDate.now()
        val monthAgo = today.minusMonths(1)
        val startTime = monthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return startTime to endTime
    }
    
    fun getYearRange(): Pair<Long, Long> {
        val today = LocalDate.now()
        val yearAgo = today.minusYears(1)
        val startTime = yearAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return startTime to endTime
    }
    
    fun getDateRange(days: Int): Pair<Long, Long> {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong())
        val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return startTime to endTime
    }
    
    fun getCalendarWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis
        
        return startTime to endTime
    }
    
    fun getCalendarMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis
        
        return startTime to endTime
    }
    
    fun formatDisplay(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return date.format(displayFormatter)
    }
    
    fun formatFull(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return date.format(fullFormatter)
    }
    
    fun formatTime(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return date.format(timeFormatter)
    }
    
    fun formatRelative(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "刚刚"
            diff < 3_600_000 -> "${diff / 60_000}分钟前"
            diff < 86_400_000 -> "${diff / 3_600_000}小时前"
            diff < 604_800_000 -> "${diff / 86_400_000}天前"
            else -> formatDisplay(timestamp)
        }
    }
    
    fun isToday(timestamp: Long): Boolean {
        val today = LocalDate.now()
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        return date == today
    }
    
    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = LocalDate.now().minusDays(1)
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        return date == yesterday
    }
    
    fun isThisWeek(timestamp: Long): Boolean {
        val (start, end) = getCalendarWeekRange()
        return timestamp in start..end
    }
    
    fun isThisMonth(timestamp: Long): Boolean {
        val (start, end) = getCalendarMonthRange()
        return timestamp in start..end
    }
    
    fun getDaysBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val startDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTimestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        val endDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(endTimestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()
    }
    
    fun getDatesInRange(startTimestamp: Long, endTimestamp: Long): List<LocalDate> {
        val startDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTimestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        val endDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(endTimestamp),
            ZoneId.systemDefault()
        ).toLocalDate()
        
        val dates = mutableListOf<LocalDate>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
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
