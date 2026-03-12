package com.calorieai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calorieai.app.data.model.Converters
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.UserSettings

@Database(
    entities = [FoodRecord::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodRecordDao(): FoodRecordDao
    abstract fun userSettingsDao(): UserSettingsDao
}
