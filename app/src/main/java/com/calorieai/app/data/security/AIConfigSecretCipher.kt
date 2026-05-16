package com.calorieai.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.calorieai.app.utils.SecureLogger
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConfigSecretCipher @Inject constructor() {
    companion object {
        private const val TAG = "AIConfigSecretCipher"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "calorieai_ai_config_api_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFIX = "enc:v1:"
        private const val GCM_TAG_SIZE_BITS = 128
        private const val GCM_IV_SIZE_BYTES = 12
    }

    fun isEncrypted(rawValue: String): Boolean {
        return rawValue.startsWith(ENCRYPTED_PREFIX)
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank() || isEncrypted(plainText)) return plainText

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val payload = cipher.iv + cipherBytes
            ENCRYPTED_PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
        }.getOrElse { error ->
            SecureLogger.e(TAG, "Encrypt api key failed", error)
            throw IllegalStateException("Failed to encrypt AI config api key", error)
        }
    }

    fun decrypt(storedValue: String): String {
        if (storedValue.isBlank()) return storedValue
        if (!isEncrypted(storedValue)) return storedValue

        return runCatching {
            val payload = Base64.decode(storedValue.removePrefix(ENCRYPTED_PREFIX), Base64.NO_WRAP)
            require(payload.size > GCM_IV_SIZE_BYTES) { "Encrypted payload is invalid" }
            val iv = payload.copyOfRange(0, GCM_IV_SIZE_BYTES)
            val cipherBytes = payload.copyOfRange(GCM_IV_SIZE_BYTES, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
            String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8)
        }.getOrElse { error ->
            SecureLogger.e(TAG, "Decrypt api key failed", error)
            ""
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }
}
