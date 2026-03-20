package com.calorieai.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calorieai.app.data.model.RecipePlan
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipePlanDao {
    @Query("SELECT * FROM recipe_plans ORDER BY startDateEpochDay DESC, updatedAt DESC")
    fun getAll(): Flow<List<RecipePlan>>

    @Query("SELECT * FROM recipe_plans")
    suspend fun getAllOnce(): List<RecipePlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecipePlan)

    @Delete
    suspend fun delete(item: RecipePlan)

    @Query("DELETE FROM recipe_plans")
    suspend fun deleteAll()
}

