package com.calorieai.app.di

import com.calorieai.app.data.local.FavoriteRecipeDao
import com.calorieai.app.data.local.FoodRecordDao
import com.calorieai.app.data.repository.FavoriteRecipeRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFoodRecordRepository(foodRecordDao: FoodRecordDao): FoodRecordRepository {
        return FoodRecordRepository(foodRecordDao)
    }

    @Provides
    @Singleton
    fun provideFavoriteRecipeRepository(favoriteRecipeDao: FavoriteRecipeDao): FavoriteRecipeRepository {
        return FavoriteRecipeRepository(favoriteRecipeDao)
    }
}