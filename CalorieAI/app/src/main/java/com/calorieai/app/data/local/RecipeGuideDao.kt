package com.calorieai.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calorieai.app.data.model.RecipeGuide
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeGuideDao {
    @Query("SELECT * FROM recipe_guides ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<RecipeGuide>>

    @Query("SELECT * FROM recipe_guides")
    suspend fun getAllOnce(): List<RecipeGuide>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecipeGuide)

    @Delete
    suspend fun delete(item: RecipeGuide)

    @Query("DELETE FROM recipe_guides")
    suspend fun deleteAll()
}

