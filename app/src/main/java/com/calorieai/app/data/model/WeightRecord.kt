package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 体重记录
 * 用于存储用户的历史体重数据
 */
@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,  // 体重（kg）
    val recordDate: Long,  // 记录时间戳
    val note: String? = null  // 备注
)
