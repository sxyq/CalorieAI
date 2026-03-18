package com.calorieai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AITokenUsage
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.Converters
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.local.dao.WaterRecordDao
import com.calorieai.app.data.local.dao.WeightRecordDao
import com.calorieai.app.data.model.AIChatHistory

@Database(
    entities = [FoodRecord::class, UserSettings::class, AIConfig::class, ExerciseRecord::class, AITokenUsage::class, WeightRecord::class, AIChatHistory::class, WaterRecord::class, APICallRecord::class],
    version = 15,
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
    abstract fun waterRecordDao(): WaterRecordDao
    abstract fun apiCallRecordDao(): APICallRecordDao
    
    companion object {
        /**
         * 从版本12迁移到版本13
         * 添加引导流程和用户目标相关字段
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加引导流程相关字段
                database.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingCompleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingCurrentStep INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingDataJson TEXT")
                
                // 添加用户目标相关字段
                database.execSQL("ALTER TABLE user_settings ADD COLUMN goalType TEXT")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN targetWeight REAL")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN weightLossStrategy TEXT")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN estimatedWeeksToGoal INTEGER")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN weeklyWeightChangeGoal REAL")
                
                // 添加用户身体档案字段
                database.execSQL("ALTER TABLE user_settings ADD COLUMN birthDate INTEGER")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN exerciseHabitsJson TEXT")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN bmr INTEGER")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN tdee INTEGER")
                database.execSQL("ALTER TABLE user_settings ADD COLUMN bmi REAL")
            }
        }
        
        /**
         * 从版本13迁移到版本14
         * 添加饮水记录表
         */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建饮水记录表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS water_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        recordTime INTEGER NOT NULL,
                        recordDate INTEGER NOT NULL,
                        note TEXT
                    )
                """)
                // 添加每日饮水目标字段
                database.execSQL("ALTER TABLE user_settings ADD COLUMN dailyWaterGoal INTEGER NOT NULL DEFAULT 2000")
                // 添加用户头像URI字段
                database.execSQL("ALTER TABLE user_settings ADD COLUMN userAvatarUri TEXT")
            }
        }

        /**
         * 从版本14迁移到版本15
         * 添加API调用记录表
         */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS api_call_records (
                        id TEXT PRIMARY KEY NOT NULL,
                        timestamp INTEGER NOT NULL,
                        configId TEXT NOT NULL,
                        configName TEXT NOT NULL,
                        modelId TEXT NOT NULL,
                        inputText TEXT NOT NULL,
                        outputText TEXT NOT NULL,
                        promptTokens INTEGER NOT NULL DEFAULT 0,
                        completionTokens INTEGER NOT NULL DEFAULT 0,
                        totalTokens INTEGER NOT NULL DEFAULT 0,
                        cost REAL NOT NULL DEFAULT 0.0,
                        duration INTEGER NOT NULL DEFAULT 0,
                        isSuccess INTEGER NOT NULL DEFAULT 1,
                        errorMessage TEXT
                    )
                """)
            }
        }
    }
}
