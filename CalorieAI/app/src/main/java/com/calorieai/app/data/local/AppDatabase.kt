package com.calorieai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AITokenUsage
import com.calorieai.app.data.model.Converters
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.WeightRecordDao
import com.calorieai.app.data.model.AIChatHistory

@Database(
    entities = [FoodRecord::class, UserSettings::class, AIConfig::class, ExerciseRecord::class, AITokenUsage::class, WeightRecord::class, AIChatHistory::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodRecordDao(): FoodRecordDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun aiConfigDao(): AIConfigDao
    abstract fun exerciseRecordDao(): ExerciseRecordDao
    abstract fun aiTokenUsageDao(): AITokenUsageDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun aiChatHistoryDao(): AIChatHistoryDao
}
