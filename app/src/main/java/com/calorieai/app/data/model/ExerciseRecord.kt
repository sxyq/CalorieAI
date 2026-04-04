package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 运动记录实体
 */
@Entity(
    tableName = "exercise_records",
    indices = [Index(value = ["recordTime"])]
)
data class ExerciseRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    
    val exerciseType: ExerciseType,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String? = null,
    val recordTime: Long = System.currentTimeMillis()
)

/**
 * 运动类型枚举
 */
enum class ExerciseType(
    val displayName: String,
    val emoji: String,
    val caloriesPerMinute: Int
) {
    RUNNING("跑步", "🏃", 10),
    WALKING("快走", "🚶", 4),
    CYCLING("骑行", "🚴", 8),
    SWIMMING("游泳", "🏊", 12),
    YOGA("瑜伽", "🧘", 3),
    WEIGHT_TRAINING("力量训练", "🏋️", 6),
    HIIT("HIIT", "🔥", 15),
    DANCING("跳舞", "💃", 7),
    HIKING("徒步", "🥾", 6),
    SKIPPING("跳绳", "🪢", 12),
    PILATES("普拉提", "🤸", 4),
    ELLIPTICAL("椭圆机", "🏃", 8),
    ROWING("划船", "🚣", 10),
    BOXING("拳击", "🥊", 11),
    SKATING("滑冰", "⛸️", 7),
    SKIING("滑雪", "⛷️", 8),
    BASKETBALL("篮球", "🏀", 8),
    FOOTBALL("足球", "⚽", 9),
    BADMINTON("羽毛球", "🏸", 6),
    TENNIS("网球", "🎾", 8),
    TABLE_TENNIS("乒乓球", "🏓", 5),
    GOLF("高尔夫", "⛳", 4),
    VOLLEYBALL("排球", "🏐", 4),
    BASEBALL("棒球", "⚾", 5),
    CLIMBING("攀岩", "🧗", 9),
    SURFING("冲浪", "🏄", 7),
    SKATEBOARDING("滑板", "🛹", 6),
    OTHER("其他", "🎯", 5);
    
    companion object {
        fun getByName(name: String): ExerciseType {
            return entries.find { it.name == name } ?: OTHER
        }
    }
}

fun getExerciseTypeDisplayName(type: ExerciseType): String {
    return "${type.emoji} ${type.displayName}"
}
