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
    // 通知设置
    val enableGoalReminder: Boolean = true,
    val enableStreakReminder: Boolean = false,
    // 备份设置
    val enableAutoBackup: Boolean = false,
    val lastBackupTime: String? = null,
    val enableCloudSync: Boolean = false
)
