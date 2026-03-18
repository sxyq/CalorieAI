package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.FavoriteRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {
    @Query("SELECT * FROM favorite_recipes ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRecipe>>
    
    @Query("SELECT * FROM favorite_recipes WHERE id = :id")
    suspend fun getFavoriteById(id: Long): FavoriteRecipe?
    
    @Query("SELECT * FROM favorite_recipes WHERE foodName LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    fun searchFavorites(keyword: String): Flow<List<FavoriteRecipe>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRecipe): Long
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteRecipe)
    
    @Query("DELETE FROM favorite_recipes WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE foodName = :foodName)")
    suspend fun isFavorite(foodName: String): Boolean
    
    @Query("DELETE FROM favorite_recipes")
    suspend fun deleteAll()
}
