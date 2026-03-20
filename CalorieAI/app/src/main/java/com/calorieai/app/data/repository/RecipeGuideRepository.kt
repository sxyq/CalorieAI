package com.calorieai.app.data.repository

import com.calorieai.app.data.local.RecipeGuideDao
import com.calorieai.app.data.model.RecipeGuide
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeGuideRepository @Inject constructor(
    private val recipeGuideDao: RecipeGuideDao
) {
    fun getAll(): Flow<List<RecipeGuide>> = recipeGuideDao.getAll()

    suspend fun getAllOnce(): List<RecipeGuide> = recipeGuideDao.getAllOnce()

    suspend fun upsert(item: RecipeGuide) {
        recipeGuideDao.upsert(item)
    }

    suspend fun delete(item: RecipeGuide) {
        recipeGuideDao.delete(item)
    }

    suspend fun deleteAll() {
        recipeGuideDao.deleteAll()
    }
}

