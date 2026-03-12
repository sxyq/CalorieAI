package com.calorieai.app.data.repository

import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.model.AIConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConfigRepository @Inject constructor(
    private val aiConfigDao: AIConfigDao
) {
    fun getAllConfigs(): Flow<List<AIConfig>> = aiConfigDao.getAllConfigs()

    fun getDefaultConfig(): Flow<AIConfig?> = aiConfigDao.getDefaultConfig()

    suspend fun getConfigById(id: String): AIConfig? = aiConfigDao.getConfigById(id)

    suspend fun addConfig(config: AIConfig) {
        aiConfigDao.insertConfig(config)
    }

    suspend fun updateConfig(config: AIConfig) {
        aiConfigDao.updateConfig(config)
    }

    suspend fun deleteConfig(config: AIConfig) {
        aiConfigDao.deleteConfig(config)
    }

    suspend fun deleteConfigById(id: String) {
        aiConfigDao.deleteConfigById(id)
    }

    suspend fun setDefaultConfig(id: String) {
        aiConfigDao.setDefaultConfigExclusive(id)
    }
}
