package com.calorieai.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 获取相对日期标签
 * @param date 目标日期
 * @return 相对标签（前天、昨天、今天）或空字符串（非近期日期）
 */
fun getRelativeDateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    val daysDiff = ChronoUnit.DAYS.between(today, date)

    return when (daysDiff) {
        -2L -> "前天"
        -1L -> "昨天"
        0L -> "今天"
        else -> ""  // 其他日期不显示相对标签
    }
}

/**
 * 获取星期标签
 * @param date 目标日期
 * @return 星期标签（周一、周二...周日）
 */
fun getWeekDayLabel(date: LocalDate): String {
    return when (date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> ""
    }
}

/**
 * 获取日期范围
 * @param centerDate 中心日期
 * @param range 范围天数（前后各多少天）
 * @return 日期列表
 */
fun getDateRange(centerDate: LocalDate = LocalDate.now(), range: Int = 2): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    for (i in -range..range) {
        dates.add(centerDate.plusDays(i.toLong()))
    }
    return dates
}

/**
 * 格式化日期
 * @param date 目标日期
 * @param pattern 格式模式
 * @return 格式化后的字符串
 */
fun formatDate(date: LocalDate, pattern: String = "yyyy-MM-dd"): String {
    return date.format(DateTimeFormatter.ofPattern(pattern))
}
