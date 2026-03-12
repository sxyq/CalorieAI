package com.calorieai.app.di

import android.content.Context
import androidx.room.Room
import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.local.FoodRecordDao
import com.calorieai.app.data.local.UserSettingsDao
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
}
