package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.AIConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AIConfigDao {
    @Query("SELECT * FROM ai_configs")
    fun getAllConfigs(): Flow<List<AIConfig>>

    @Query("SELECT * FROM ai_configs WHERE isDefault = 1 LIMIT 1")
    fun getDefaultConfig(): Flow<AIConfig?>

    @Query("SELECT * FROM ai_configs WHERE id = :id")
    suspend fun getConfigById(id: String): AIConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AIConfig)

    @Query("DELETE FROM ai_configs")
    suspend fun deleteAll()

    @Update
    suspend fun updateConfig(config: AIConfig)

    @Delete
    suspend fun deleteConfig(config: AIConfig)

    @Query("DELETE FROM ai_configs WHERE id = :id")
    suspend fun deleteConfigById(id: String)

    @Query("UPDATE ai_configs SET isDefault = 0")
    suspend fun clearDefaultConfig()

    @Query("UPDATE ai_configs SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultConfig(id: String)

    @Transaction
    suspend fun setDefaultConfigExclusive(id: String) {
        clearDefaultConfig()
        setDefaultConfig(id)
    }
}
