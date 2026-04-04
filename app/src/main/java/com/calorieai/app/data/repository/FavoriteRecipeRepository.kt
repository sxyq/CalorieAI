package com.calorieai.app.data.repository

import com.calorieai.app.data.local.FavoriteRecipeDao
import com.calorieai.app.data.model.FavoriteRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRecipeRepository @Inject constructor(
    private val favoriteRecipeDao: FavoriteRecipeDao
) {
    fun getAllFavorites(): Flow<List<FavoriteRecipe>> = favoriteRecipeDao.getAllFavorites()

    suspend fun getAllFavoritesOnce(): List<FavoriteRecipe> = favoriteRecipeDao.getAllFavorites().first()

    suspend fun getBySourceRecordId(sourceRecordId: String): FavoriteRecipe? {
        return favoriteRecipeDao.getBySourceRecordId(sourceRecordId)
    }

    suspend fun getById(id: String): FavoriteRecipe? {
        return favoriteRecipeDao.getById(id)
    }

    suspend fun upsert(recipe: FavoriteRecipe) {
        favoriteRecipeDao.insert(sanitizeFavoriteRecipe(recipe))
    }

    suspend fun delete(recipe: FavoriteRecipe) {
        favoriteRecipeDao.delete(recipe)
    }

    suspend fun deleteBySourceRecordId(sourceRecordId: String) {
        favoriteRecipeDao.deleteBySourceRecordId(sourceRecordId)
    }

    suspend fun deleteAll() {
        favoriteRecipeDao.deleteAll()
    }
}
