package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val dailyCalorieGoal: Int = 2000,
    val userName: String? = null,
    val userGender: String? = null,
    val userAge: Int? = null,
    val userHeight: Float? = null,
    val userWeight: Float? = null,
    val dietaryPreference: String? = null,
    val breakfastReminderTime: String = "08:00",
    val lunchReminderTime: String = "12:00",
    val dinnerReminderTime: String = "18:00",
    val isNotificationEnabled: Boolean = true,
    val isDarkMode: Boolean? = null,  // null = 跟随系统
    val seedColor: String? = null,
    val selectedAIPresetId: String? = null,
    val customAIEndpoint: String? = null,
    val customAIModel: String? = null
)
