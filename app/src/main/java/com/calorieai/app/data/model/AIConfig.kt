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
    const val ID_OPENAI_GPT5 = "openai_gpt5"
    const val ID_OPENAI_GPT4O = "openai_gpt4o"
    const val ID_CLAUDE_4_6_OPUS = "claude_4_6_opus"
    const val ID_CLAUDE_3_5_SONNET = "claude_3_5_sonnet"
    const val ID_KIMI_K2_5 = "kimi_k2_5"
    const val ID_GLM_4_PLUS = "glm_4_plus"
    const val ID_QWEN_2_5_MAX = "qwen_2_5_max"
    const val ID_DEEPSEEK_V3 = "deepseek_v3"
    const val ID_DEEPSEEK_R1 = "deepseek_r1"
    const val ID_GEMINI_2_0_PRO = "gemini_2_0_pro"
    const val ID_LONGCAT_FLASH_OMNI = "longcat_flash_omni"
    const val ID_LONGCAT_FLASH_CHAT = "longcat_flash_chat"
    const val ID_LONGCAT_FLASH_THINKING = "longcat_flash_thinking"
    const val ID_LONGCAT_FLASH_LITE = "longcat_flash_lite"

    private val templates = listOf(
        PresetTemplate(ID_OPENAI_GPT5, "OpenAI GPT-5", AIProviderIcons.OPENAI, IconType.RESOURCE, AIProtocol.OPENAI, "https://api.openai.com/v1/chat/completions", "gpt-5", true),
        PresetTemplate(ID_OPENAI_GPT4O, "OpenAI GPT-4o", AIProviderIcons.OPENAI, IconType.RESOURCE, AIProtocol.OPENAI, "https://api.openai.com/v1/chat/completions", "gpt-4o", true),
        PresetTemplate(ID_CLAUDE_4_6_OPUS, "Claude 4.6 Opus", AIProviderIcons.CLAUDE, IconType.RESOURCE, AIProtocol.CLAUDE, "https://api.anthropic.com/v1/messages", "claude-4-6-opus-20251001", true),
        PresetTemplate(ID_CLAUDE_3_5_SONNET, "Claude 3.5 Sonnet", AIProviderIcons.CLAUDE, IconType.RESOURCE, AIProtocol.CLAUDE, "https://api.anthropic.com/v1/messages", "claude-3-5-sonnet-20241022", true),
        PresetTemplate(ID_KIMI_K2_5, "Kimi K2.5", AIProviderIcons.KIMI, IconType.RESOURCE, AIProtocol.KIMI, "https://api.moonshot.cn/v1/chat/completions", "kimi-k2-5", true),
        PresetTemplate(ID_GLM_4_PLUS, "GLM-4 Plus", AIProviderIcons.GLM, IconType.RESOURCE, AIProtocol.GLM, "https://open.bigmodel.cn/api/paas/v4/chat/completions", "glm-4-plus", true),
        PresetTemplate(ID_QWEN_2_5_MAX, "Qwen 2.5 Max", AIProviderIcons.QWEN, IconType.RESOURCE, AIProtocol.QWEN, "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen2.5-max", true),
        PresetTemplate(ID_DEEPSEEK_V3, "DeepSeek V3", AIProviderIcons.DEEPSEEK, IconType.RESOURCE, AIProtocol.DEEPSEEK, "https://api.deepseek.com/v1/chat/completions", "deepseek-chat", false),
        PresetTemplate(ID_DEEPSEEK_R1, "DeepSeek R1", AIProviderIcons.DEEPSEEK, IconType.RESOURCE, AIProtocol.DEEPSEEK, "https://api.deepseek.com/v1/chat/completions", "deepseek-reasoner", false),
        PresetTemplate(ID_GEMINI_2_0_PRO, "Gemini 2.0 Pro", AIProviderIcons.GEMINI, IconType.RESOURCE, AIProtocol.GEMINI, "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-pro-exp:generateContent", "gemini-2.0-pro-exp", true),
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

    val OPENAI_GPT5: AIConfig get() = getById(ID_OPENAI_GPT5)!!
    val OPENAI_GPT4O: AIConfig get() = getById(ID_OPENAI_GPT4O)!!
    val CLAUDE_4_6_OPUS: AIConfig get() = getById(ID_CLAUDE_4_6_OPUS)!!
    val CLAUDE_3_5_SONNET: AIConfig get() = getById(ID_CLAUDE_3_5_SONNET)!!
    val KIMI_K2_5: AIConfig get() = getById(ID_KIMI_K2_5)!!
    val GLM_4_PLUS: AIConfig get() = getById(ID_GLM_4_PLUS)!!
    val QWEN_2_5_MAX: AIConfig get() = getById(ID_QWEN_2_5_MAX)!!
    val DEEPSEEK_V3: AIConfig get() = getById(ID_DEEPSEEK_V3)!!
    val DEEPSEEK_R1: AIConfig get() = getById(ID_DEEPSEEK_R1)!!
    val GEMINI_2_0_PRO: AIConfig get() = getById(ID_GEMINI_2_0_PRO)!!
    val LONGCAT_FLASH_OMNI: AIConfig get() = getById(ID_LONGCAT_FLASH_OMNI)!!
    val LONGCAT_FLASH_CHAT: AIConfig get() = getById(ID_LONGCAT_FLASH_CHAT)!!
    val LONGCAT_FLASH_THINKING: AIConfig get() = getById(ID_LONGCAT_FLASH_THINKING)!!
    val LONGCAT_FLASH_LITE: AIConfig get() = getById(ID_LONGCAT_FLASH_LITE)!!

    val ICON_OPTIONS = listOf("🤖", "🧠", "🔮", "⚡", "🎯", "🔥", "💎", "🌟")
}
