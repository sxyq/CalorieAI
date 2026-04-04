package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 饮水记录数据模型
 */
@Entity(
    tableName = "water_records",
    indices = [
        Index(value = ["recordTime"]),
        Index(value = ["recordDate"])
    ]
)
data class WaterRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Int, // 饮水量（毫升）
    val recordTime: Long = System.currentTimeMillis(),
    val recordDate: Long = System.currentTimeMillis(), // 记录日期（当天0点）
    val note: String? = null // 备注
)

/**
 * 饮水统计信息
 */
data class WaterStats(
    val todayAmount: Int, // 今日饮水量
    val targetAmount: Int, // 目标饮水量
    val weeklyAverage: Float, // 周平均饮水量
    val monthlyAverage: Float, // 月平均饮水量
    val streakDays: Int // 连续达标天数
)
