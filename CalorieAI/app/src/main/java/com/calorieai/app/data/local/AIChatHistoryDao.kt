package com.calorieai.app.data.local

import androidx.room.*
import com.calorieai.app.data.model.AIChatHistory
import kotlinx.coroutines.flow.Flow

/**
 * AI对话历史数据访问对象
 */
@Dao
interface AIChatHistoryDao {
    
    @Insert
    suspend fun insert(history: AIChatHistory): Long
    
    @Update
    suspend fun update(history: AIChatHistory)
    
    @Delete
    suspend fun delete(history: AIChatHistory)
    
    @Query("SELECT * FROM ai_chat_history ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllHistory(): Flow<List<AIChatHistory>>
    
    @Query("SELECT * FROM ai_chat_history WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getHistoryBySessionId(sessionId: String): AIChatHistory?
    
    @Query("DELETE FROM ai_chat_history WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
    
    @Query("DELETE FROM ai_chat_history")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM ai_chat_history")
    suspend fun getHistoryCount(): Int
    
    @Query("SELECT * FROM ai_chat_history ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<AIChatHistory>
}
