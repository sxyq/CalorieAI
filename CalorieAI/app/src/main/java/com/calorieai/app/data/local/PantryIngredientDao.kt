package com.calorieai.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calorieai.app.data.model.PantryIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryIngredientDao {
    @Query(
        """
        SELECT * FROM pantry_ingredients
        ORDER BY
            CASE WHEN expiresAt IS NULL THEN 1 ELSE 0 END,
            expiresAt ASC,
            updatedAt DESC
        """
    )
    fun getAll(): Flow<List<PantryIngredient>>

    @Query("SELECT * FROM pantry_ingredients")
    suspend fun getAllOnce(): List<PantryIngredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PantryIngredient)

    @Delete
    suspend fun delete(item: PantryIngredient)

    @Query("DELETE FROM pantry_ingredients")
    suspend fun deleteAll()
}

