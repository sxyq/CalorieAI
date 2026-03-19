package com.calorieai.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calorieai.app.data.model.FavoriteRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {
    @Query("SELECT * FROM favorite_recipes ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRecipe>>

    @Query("SELECT * FROM favorite_recipes WHERE sourceRecordId = :sourceRecordId LIMIT 1")
    suspend fun getBySourceRecordId(sourceRecordId: String): FavoriteRecipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: FavoriteRecipe)

    @Update
    suspend fun update(recipe: FavoriteRecipe)

    @Delete
    suspend fun delete(recipe: FavoriteRecipe)

    @Query("DELETE FROM favorite_recipes WHERE sourceRecordId = :sourceRecordId")
    suspend fun deleteBySourceRecordId(sourceRecordId: String)

    @Query("DELETE FROM favorite_recipes")
    suspend fun deleteAll()
}
