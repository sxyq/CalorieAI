package com.calorieai.app.data.repository

import com.calorieai.app.data.local.AIConfigDao
import com.calorieai.app.data.local.entity.toEntity
import com.calorieai.app.data.local.entity.toModel
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.security.AIConfigSecretCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConfigRepository @Inject constructor(
    private val aiConfigDao: AIConfigDao,
    private val secretCipher: AIConfigSecretCipher
) {
    fun getAllConfigs(): Flow<List<AIConfig>> = aiConfigDao.getAllConfigs()
        .map { configs -> configs.map { it.toModel(secretCipher) } }

    fun getDefaultConfig(): Flow<AIConfig?> = aiConfigDao.getDefaultConfig()
        .map { config -> config?.toModel(secretCipher) }

    suspend fun getConfigById(id: String): AIConfig? = aiConfigDao.getConfigById(id)?.toModel(secretCipher)

    suspend fun getAllConfigsOnce(): List<AIConfig> {
        return aiConfigDao.getAllConfigsOnce().map { it.toModel(secretCipher) }
    }

    suspend fun addConfig(config: AIConfig) {
        aiConfigDao.insertConfig(sanitizeAIConfig(config).toEntity(secretCipher))
    }

    suspend fun updateConfig(config: AIConfig) {
        aiConfigDao.updateConfig(sanitizeAIConfig(config).toEntity(secretCipher))
    }

    suspend fun deleteConfig(config: AIConfig) {
        aiConfigDao.deleteConfigById(config.id)
    }

    suspend fun deleteConfigById(id: String) {
        aiConfigDao.deleteConfigById(id)
    }

    suspend fun deleteAll() {
        aiConfigDao.deleteAll()
    }

    suspend fun setDefaultConfig(id: String) {
        aiConfigDao.setDefaultConfigExclusive(id)
    }

    suspend fun migrateStoredApiKeysIfNeeded(): Int {
        val currentConfigs = aiConfigDao.getAllConfigsOnce()
        var migratedCount = 0
        currentConfigs.forEach { config ->
            val rawKey = config.apiKey.trim()
            if (rawKey.isNotBlank() && !secretCipher.isEncrypted(rawKey)) {
                aiConfigDao.updateConfig(config.copy(apiKey = secretCipher.encrypt(rawKey)))
                migratedCount++
            }
        }
        return migratedCount
    }
}
