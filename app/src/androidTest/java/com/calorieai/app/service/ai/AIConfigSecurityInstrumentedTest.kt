package com.calorieai.app.service.ai

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.local.entity.AIConfigEntity
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.security.AIConfigSecretCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AIConfigSecurityInstrumentedTest {

    @Test
    fun legacyPlaintextApiKey_isMigratedAndDecryptedOnRead() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            val secretCipher = AIConfigSecretCipher()
            val repository = AIConfigRepository(database.aiConfigDao(), secretCipher)
            val initializer = AIDefaultConfigInitializer(repository)
            val plaintextApiKey = "test-key-do-not-use"
            database.aiConfigDao().insertConfig(
                AIConfigEntity(
                    id = "instrumented_test_config",
                    name = "Instrumentation test config",
                    icon = "test",
                    protocol = AIProtocol.OPENAI,
                    apiUrl = "https://example.invalid/v1/chat/completions",
                    apiKey = plaintextApiKey,
                    modelId = "test-model",
                    isImageUnderstanding = false,
                    isDefault = true
                )
            )

            initializer.initializeDefaultConfig()

            val storedConfig = database.aiConfigDao().getAllConfigsOnce().firstOrNull()
            assertTrue("stored AI config should exist", storedConfig != null)
            assertTrue(
                "api key in database should be encrypted",
                storedConfig != null && secretCipher.isEncrypted(storedConfig.apiKey)
            )

            val defaultConfig = repository.getDefaultConfig().first()
            assertTrue("default AI config should resolve", defaultConfig != null)
            assertEquals(
                "repository should return the decrypted api key",
                plaintextApiKey,
                defaultConfig?.apiKey
            )
        } finally {
            database.close()
        }
    }
}
