package com.calorieai.app.di

import android.content.Context
import androidx.room.Room
import com.calorieai.app.data.local.AIChatHistoryDao
import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.local.AITokenUsageDao
import com.calorieai.app.data.local.APICallRecordDao
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.local.ExerciseRecordDao
import com.calorieai.app.data.local.FoodRecordDao
import com.calorieai.app.data.local.FavoriteRecipeDao
import com.calorieai.app.data.local.UserSettingsDao
import com.calorieai.app.data.local.dao.WaterRecordDao
import com.calorieai.app.data.local.dao.WeightRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calorieai_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_12_13, 
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFoodRecordDao(database: AppDatabase): FoodRecordDao {
        return database.foodRecordDao()
    }

    @Provides
    fun provideUserSettingsDao(database: AppDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }

    @Provides
    fun provideAIConfigDao(database: AppDatabase): AIConfigDao {
        return database.aiConfigDao()
    }

    @Provides
    fun provideExerciseRecordDao(database: AppDatabase): ExerciseRecordDao {
        return database.exerciseRecordDao()
    }

    @Provides
    fun provideAITokenUsageDao(database: AppDatabase): AITokenUsageDao {
        return database.aiTokenUsageDao()
    }

    @Provides
    fun provideWeightRecordDao(database: AppDatabase): WeightRecordDao {
        return database.weightRecordDao()
    }

    @Provides
    fun provideAIChatHistoryDao(database: AppDatabase): AIChatHistoryDao {
        return database.aiChatHistoryDao()
    }

    @Provides
    fun provideWaterRecordDao(database: AppDatabase): WaterRecordDao {
        return database.waterRecordDao()
    }

    @Provides
    fun provideAPICallRecordDao(database: AppDatabase): APICallRecordDao {
        return database.apiCallRecordDao()
    }

    @Provides
    fun provideFavoriteRecipeDao(database: AppDatabase): FavoriteRecipeDao {
        return database.favoriteRecipeDao()
    }
}
