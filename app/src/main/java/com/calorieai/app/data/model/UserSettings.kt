package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val dailyCalorieGoal: Int = 2000,
    val userName: String? = null,
    val userId: String? = null,
    val userGender: String? = null,
    val userAge: Int? = null,
    val userHeight: Float? = null,
    val userWeight: Float? = null,
    val activityLevel: String = "SEDENTARY",
    val dietaryPreference: String? = null,
    val dietaryAllergens: String? = null, // 过敏原，逗号分隔
    val flavorPreferences: String? = null, // 口味偏好，逗号分隔
    val budgetPreference: String? = null, // 预算偏好：经济/均衡/高品质
    val maxCookingMinutes: Int? = null, // 单餐可接受最长烹饪时长（分钟）
    val specialPopulationMode: String = "GENERAL", // GENERAL/DIABETES/GOUT/PREGNANCY/CHILD/FITNESS
    val weeklyRecordGoalDays: Int = 5, // 每周记录目标天数
    val breakfastReminderTime: String = "08:00",
    val lunchReminderTime: String = "12:00",
    val dinnerReminderTime: String = "18:00",
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean? = null,  // null = 跟随系统
    val seedColor: String? = null,
    val selectedAIPresetId: String? = null,
    val customAIEndpoint: String? = null,
    val customAIModel: String? = null,
    // 界面外观设置
    val themeMode: String = "SYSTEM",  // LIGHT, DARK, SYSTEM
    val useDeadlinerStyle: Boolean = true,
    val hideDividers: Boolean = false,
    val fontSize: String = "MEDIUM",  // SMALL, MEDIUM, LARGE
    val enableAnimations: Boolean = true,
    // 交互与行为设置
    val feedbackType: String = "BOTH",  // NONE, VIBRATION, SOUND, BOTH
    val enableVibration: Boolean = true,
    val enableSound: Boolean = false,
    val backgroundBehavior: String = "STANDARD",  // STANDARD, KEEP_ALIVE, BATTERY_SAVER
    val startupPage: String = "HOME",  // HOME, STATS, ADD
    val enableQuickAdd: Boolean = false,
    val enableLongPressHomeToAdd: Boolean = true,
    val enableLongPressOverviewToStats: Boolean = true,
    val enableLongPressMyToProfileEdit: Boolean = true,
    // 通知设置
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = false,
    // 备份设置
    val enableAutoBackup: Boolean = false,
    val lastBackupTime: String? = null,
    val enableCloudSync: Boolean = false,
    // AI助手设置
    val showAIWidget: Boolean = true,  // 是否显示AI助手悬浮按钮
    // 壁纸设置
    val wallpaperType: String = "SOLID",  // GRADIENT, SOLID, IMAGE
    val wallpaperColor: String? = "#FFFFFF",  // 纯色壁纸颜色
    val wallpaperGradientStart: String? = null,  // 渐变起始颜色
    val wallpaperGradientEnd: String? = null,  // 渐变结束颜色
    val wallpaperImageUri: String? = null,  // 图片壁纸URI
    
    // ==================== 引导流程相关字段 ====================
    // 引导完成标记
    val onboardingCompleted: Boolean = false,
    // 当前引导步骤（1-6）
    val onboardingCurrentStep: Int = 1,
    // 引导数据JSON（临时存储各步骤数据）
    val onboardingDataJson: String? = null,
    
    // ==================== 用户目标相关字段 ====================
    // 目标类型：LOSE_WEIGHT(减脂), GAIN_MUSCLE(增肌), GAIN_WEIGHT(增重), MAINTAIN(保持现状)
    val goalType: String? = null,
    // 目标体重（kg）
    val targetWeight: Float? = null,
    // 减肥策略：AGGRESSIVE(激进), MODERATE(平和), GENTLE(温和)
    val weightLossStrategy: String? = null,
    // 预计达成目标周数
    val estimatedWeeksToGoal: Int? = null,
    // 每周目标减重/增重（kg）
    val weeklyWeightChangeGoal: Float? = null,
    
    // ==================== 用户身体档案 ====================
    // 出生日期（时间戳）
    val birthDate: Long? = null,
    // 运动习惯列表（JSON数组）
    val exerciseHabitsJson: String? = null,
    // BMR基础代谢率
    val bmr: Int? = null,
    // TDEE每日总能量消耗
    val tdee: Int? = null,
    // BMI指数
    val bmi: Float? = null,
    // 每日饮水目标（ml）
    val dailyWaterGoal: Int = 2000,
    // 用户头像URI
    val userAvatarUri: String? = null
)

/**
 * 目标类型枚举
 */
enum class GoalType(val displayName: String, val description: String) {
    LOSE_WEIGHT("减脂", "减少体脂，塑造身材"),
    GAIN_MUSCLE("增肌", "增加肌肉量，增强力量"),
    GAIN_WEIGHT("增重", "健康增重，改善体质"),
    MAINTAIN("保持现状", "维持当前体重和健康状态");
    
    companion object {
        fun fromString(value: String?): GoalType? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * 减肥策略枚举
 */
enum class WeightLossStrategy(
    val displayName: String,
    val description: String,
    val weeklyChange: Float,  // 每周体重变化（kg）
    val calorieDeficitPercent: Float  // 热量缺口百分比
) {
    AGGRESSIVE("激进", "快速见效，需要较强意志力", 0.7f, 0.25f),
    MODERATE("平和", "效率与安全平衡", 0.5f, 0.20f),
    GENTLE("温和", "循序渐进，容易坚持", 0.3f, 0.15f);
    
    companion object {
        fun fromString(value: String?): WeightLossStrategy? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * 性别枚举
 */
enum class Gender(val displayName: String, val emoji: String) {
    MALE("男", "👨"),
    FEMALE("女", "👩"),
    OTHER("其他", "🧑");
    
    companion object {
        fun fromString(value: String?): Gender? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * 活动水平枚举
 */
enum class ActivityLevel(
    val displayName: String,
    val description: String,
    val multiplier: Float
) {
    SEDENTARY("久坐不动", "几乎不运动", 1.2f),
    LIGHT("轻度活动", "每周运动1-3天", 1.375f),
    MODERATE("中度活动", "每周运动3-5天", 1.55f),
    ACTIVE("高度活动", "每周运动6-7天", 1.725f),
    VERY_ACTIVE("极度活动", "每天高强度运动", 1.9f);
    
    companion object {
        fun fromString(value: String?): ActivityLevel? {
            return entries.find { it.name == value }
        }
    }
}
