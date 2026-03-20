package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_configs")
data class AIConfig(
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

enum class AIProtocol {
    OPENAI, CLAUDE, KIMI, GLM, QWEN, DEEPSEEK, GEMINI, LONGCAT
}

enum class IconType {
    EMOJI, RESOURCE, URL
}

object AIProviderIcons {
    const val OPENAI = "ic_openai"
    const val CLAUDE = "ic_claude"
    const val KIMI = "ic_kimi"
    const val GLM = "ic_glm"
    const val QWEN = "ic_qwen"
    const val DEEPSEEK = "ic_deepseek"
    const val GEMINI = "ic_gemini"
    const val LONGCAT = "ic_longcat"
    const val CUSTOM = "ic_custom_ai"
}

private data class PresetTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val iconType: IconType,
    val protocol: AIProtocol,
    val apiUrl: String,
    val modelId: String,
    val isImageUnderstanding: Boolean
)

object AIConfigPresets {
    const val ID_LONGCAT_FLASH_OMNI = "longcat_flash_omni"
    const val ID_LONGCAT_FLASH_CHAT = "longcat_flash_chat"
    const val ID_LONGCAT_FLASH_THINKING = "longcat_flash_thinking"
    const val ID_LONGCAT_FLASH_LITE = "longcat_flash_lite"

    private val templates = listOf(
        PresetTemplate(ID_LONGCAT_FLASH_OMNI, "LongCat-Flash-Omni-2603", AIProviderIcons.LONGCAT, IconType.EMOJI, AIProtocol.LONGCAT, "https://api.longcat.chat/openai/v1/chat/completions", "LongCat-Flash-Omni-2603", true),
        PresetTemplate(ID_LONGCAT_FLASH_CHAT, "LongCat-Flash-Chat", AIProviderIcons.LONGCAT, IconType.EMOJI, AIProtocol.LONGCAT, "https://api.longcat.chat/openai/v1/chat/completions", "LongCat-Flash-Chat", false),
        PresetTemplate(ID_LONGCAT_FLASH_THINKING, "LongCat-Flash-Thinking-2601", AIProviderIcons.LONGCAT, IconType.EMOJI, AIProtocol.LONGCAT, "https://api.longcat.chat/openai/v1/chat/completions", "LongCat-Flash-Thinking-2601", false),
        PresetTemplate(ID_LONGCAT_FLASH_LITE, "LongCat-Flash-Lite", AIProviderIcons.LONGCAT, IconType.EMOJI, AIProtocol.LONGCAT, "https://api.longcat.chat/openai/v1/chat/completions", "LongCat-Flash-Lite", false)
    )

    private fun createPreset(template: PresetTemplate): AIConfig = AIConfig(
        id = template.id,
        name = template.name,
        icon = template.icon,
        iconType = template.iconType,
        protocol = template.protocol,
        apiUrl = template.apiUrl,
        apiKey = "",
        modelId = template.modelId,
        isImageUnderstanding = template.isImageUnderstanding,
        isPreset = true
    )

    private val presetMap: Map<String, AIConfig> by lazy {
        templates.associate { it.id to createPreset(it) }
    }

    val ALL_PRESETS: List<AIConfig> get() = presetMap.values.toList()

    fun getById(id: String): AIConfig? = presetMap[id]

    fun getByProtocol(protocol: AIProtocol): List<AIConfig> = ALL_PRESETS.filter { it.protocol == protocol }

    val LONGCAT_FLASH_OMNI: AIConfig get() = getById(ID_LONGCAT_FLASH_OMNI)!!
    val LONGCAT_FLASH_CHAT: AIConfig get() = getById(ID_LONGCAT_FLASH_CHAT)!!
    val LONGCAT_FLASH_THINKING: AIConfig get() = getById(ID_LONGCAT_FLASH_THINKING)!!
    val LONGCAT_FLASH_LITE: AIConfig get() = getById(ID_LONGCAT_FLASH_LITE)!!

    val ICON_OPTIONS = listOf("🤖", "🧠", "🔮", "⚡", "🎯", "🔥", "💎", "🌟")
}
