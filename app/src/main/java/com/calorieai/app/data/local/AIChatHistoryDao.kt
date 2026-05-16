package com.calorieai.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calorieai.app.data.model.AIChatHistory
import kotlinx.coroutines.flow.Flow

/**
 * AI对话历史数据访问对象
 */
@Dao
interface AIChatHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: AIChatHistory): Long
    
    @Update
    suspend fun update(history: AIChatHistory)
    
    @Query("SELECT * FROM ai_chat_history ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllHistory(): Flow<List<AIChatHistory>>

    @Query("SELECT * FROM ai_chat_history ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getAllHistoryOnce(): List<AIChatHistory>
    
    @Query("SELECT * FROM ai_chat_history WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getHistoryBySessionId(sessionId: String): AIChatHistory?
    
    @Query("DELETE FROM ai_chat_history WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    @Query("DELETE FROM ai_chat_history")
    suspend fun deleteAll()

}
