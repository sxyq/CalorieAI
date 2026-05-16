package com.calorieai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AIProtocol
import com.calorieai.app.data.model.IconType
import com.calorieai.app.data.security.AIConfigSecretCipher

@Entity(tableName = "ai_configs")
data class AIConfigEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val iconType: IconType = IconType.EMOJI,
    val protocol: AIProtocol,
    val apiUrl: String,
    val apiKey: String,
    val modelId: String,
    val isImageUnderstanding: Boolean,
    val isDefault: Boolean = false,
    val isPreset: Boolean = false
)

fun AIConfigEntity.toModel(secretCipher: AIConfigSecretCipher): AIConfig {
    return AIConfig(
        id = id,
        name = name,
        icon = icon,
        iconType = iconType,
        protocol = protocol,
        apiUrl = apiUrl,
        apiKey = secretCipher.decrypt(apiKey),
        modelId = modelId,
        isImageUnderstanding = isImageUnderstanding,
        isDefault = isDefault,
        isPreset = isPreset
    )
}

fun AIConfig.toEntity(secretCipher: AIConfigSecretCipher): AIConfigEntity {
    return AIConfigEntity(
        id = id,
        name = name,
        icon = icon,
        iconType = iconType,
        protocol = protocol,
        apiUrl = apiUrl,
        apiKey = secretCipher.encrypt(apiKey),
        modelId = modelId,
        isImageUnderstanding = isImageUnderstanding,
        isDefault = isDefault,
        isPreset = isPreset
    )
}
