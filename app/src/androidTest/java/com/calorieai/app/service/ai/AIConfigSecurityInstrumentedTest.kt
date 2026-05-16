package com.calorieai.app.service.ai

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.security.AIConfigSecretCipher
import com.calorieai.app.service.ai.common.AIApiClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AIConfigSecurityInstrumentedTest {

    @Test
    fun encryptedApiKey_canStillCallModel() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calorieai_database"
        ).build()

        try {
            val secretCipher = AIConfigSecretCipher()
            val repository = AIConfigRepository(database.aiConfigDao(), secretCipher)
            val initializer = AIDefaultConfigInitializer(repository)
            val client = AIApiClient(OkHttpClient())

            initializer.initializeDefaultConfig()

            val storedConfig = database.aiConfigDao().getAllConfigsOnce().firstOrNull()
            assertTrue("stored AI config should exist", storedConfig != null)
            assertTrue(
                "api key in database should be encrypted",
                storedConfig != null && secretCipher.isEncrypted(storedConfig.apiKey)
            )

            val defaultConfig = repository.getDefaultConfig().firstOrNull()
            assertTrue("default AI config should resolve", defaultConfig != null)
            assertFalse(
                "repository should return decrypted api key",
                defaultConfig != null && secretCipher.isEncrypted(defaultConfig.apiKey)
            )
            assertTrue(
                "decrypted api key should not be blank",
                defaultConfig != null && defaultConfig.apiKey.isNotBlank()
            )

            val result = client.testConnection(
                config = requireNotNull(defaultConfig),
                timeoutSeconds = 20
            )
            assertTrue("AI connection test failed: ${result.message}", result.success)
        } finally {
            database.close()
        }
    }
}
