package com.calorieai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.calorieai.app.data.model.AIFunctionConfig
import com.calorieai.app.data.model.AIFunctionConfigDefaults
import com.calorieai.app.data.model.AIFunctionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIFunctionConfigRepository @Inject constructor(
    private val aiConfigRepository: AIConfigRepository,
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "ai_function_configs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences("ai_function_configs_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getAllFunctionConfigs(): Flow<List<AIFunctionConfig>> = flow {
        val configs = AIFunctionType.entries.map { type ->
            getFunctionConfig(type)
        }
        emit(configs)
    }

    suspend fun getFunctionConfig(type: AIFunctionType): AIFunctionConfig {
        val savedConfigId = encryptedPrefs.getString(getPrefKey(type), null)
        val configId = savedConfigId ?: getDefaultConfigId(type)
        return AIFunctionConfig(
            functionType = type,
            configId = configId
        )
    }

    suspend fun getFunctionConfigId(type: AIFunctionType): String {
        return getFunctionConfig(type).configId
    }

    suspend fun setFunctionConfig(type: AIFunctionType, configId: String) {
        encryptedPrefs.edit()
            .putString(getPrefKey(type), configId)
            .apply()
    }

    private fun getDefaultConfigId(type: AIFunctionType): String {
        return when (type) {
            AIFunctionType.FOOD_IMAGE_ANALYSIS -> AIFunctionConfigDefaults.DEFAULT_IMAGE_ANALYSIS_CONFIG_ID
            AIFunctionType.FOOD_TEXT_ANALYSIS -> AIFunctionConfigDefaults.DEFAULT_TEXT_ANALYSIS_CONFIG_ID
            AIFunctionType.AI_CHAT -> AIFunctionConfigDefaults.DEFAULT_CHAT_CONFIG_ID
        }
    }

    private fun getPrefKey(type: AIFunctionType): String {
        return "ai_func_config_${type.name}"
    }
}
