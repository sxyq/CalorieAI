package com.calorieai.app.data.repository

import com.calorieai.app.data.local.FavoriteRecipeDao
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import kotlinx.coroutines.flow.Flow

class FavoriteRecipeRepository(private val favoriteRecipeDao: FavoriteRecipeDao) {
    fun getAllFavorites(): Flow<List<FavoriteRecipe>> {
        return favoriteRecipeDao.getAllFavorites()
    }
    
    suspend fun getFavoriteById(id: Long): FavoriteRecipe? {
        return favoriteRecipeDao.getFavoriteById(id)
    }
    
    fun searchFavorites(keyword: String): Flow<List<FavoriteRecipe>> {
        return favoriteRecipeDao.searchFavorites(keyword)
    }
    
    suspend fun addFavorite(favorite: FavoriteRecipe): Long {
        return favoriteRecipeDao.insertFavorite(favorite)
    }
    
    suspend fun addFavoriteFromFoodRecord(record: FoodRecord): Long {
        val favorite = FavoriteRecipe.fromFoodRecord(record)
        return favoriteRecipeDao.insertFavorite(favorite)
    }
    
    suspend fun removeFavorite(favorite: FavoriteRecipe) {
        favoriteRecipeDao.deleteFavorite(favorite)
    }
    
    suspend fun deleteFavorite(favorite: FavoriteRecipe) {
        favoriteRecipeDao.deleteFavorite(favorite)
    }
    
    suspend fun removeFavoriteById(id: Long) {
        favoriteRecipeDao.deleteFavoriteById(id)
    }
    
    suspend fun isFavorite(foodName: String): Boolean {
        return favoriteRecipeDao.isFavorite(foodName)
    }
    
    suspend fun clearAllFavorites() {
        favoriteRecipeDao.deleteAll()
    }
}