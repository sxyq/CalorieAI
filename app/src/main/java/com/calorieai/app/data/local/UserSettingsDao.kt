package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsSync(): UserSettings?

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsOnce(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettings)

    @Update
    suspend fun updateSettings(settings: UserSettings)

    @Query("UPDATE user_settings SET dailyCalorieGoal = :goal WHERE id = 1")
    suspend fun updateDailyGoal(goal: Int)

    @Query("UPDATE user_settings SET isNotificationEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationEnabled(enabled: Boolean)
}
