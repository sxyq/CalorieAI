package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI配置实体
 * 支持多个AI配置，可切换默认配置
 */
@Entity(tableName = "ai_configs")
data class AIConfig(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,                   // 配置名称（自定义）
    val icon: String,                   // 图标（资源名或URL）
    val iconType: IconType = IconType.EMOJI,  // 图标类型
    val protocol: AIProtocol,           // 协议类型
    val apiUrl: String,                 // API地址
    val apiKey: String,                 // API密钥（加密存储）
    val modelId: String,                // 模型ID
    val isImageUnderstanding: Boolean,  // 是否启用图像理解
    val isDefault: Boolean = false      // 是否为默认配置
)

enum class AIProtocol {
    OPENAI,     // OpenAI协议
    CLAUDE,     // Claude协议
    KIMI,       // Moonshot Kimi
    GLM,        // Zhipu GLM
    QWEN,       // Alibaba Qwen
    DEEPSEEK,   // DeepSeek
    GEMINI      // Google Gemini
}

enum class IconType {
    EMOJI,      // Emoji图标
    RESOURCE,   // 本地资源图标
    URL         // 网络图标URL
}

/**
 * AI提供商图标资源
 * 用于显示模型提供商的商标图标
 */
object AIProviderIcons {
    const val OPENAI = "ic_openai"
    const val CLAUDE = "ic_claude"
    const val KIMI = "ic_kimi"
    const val GLM = "ic_glm"
    const val QWEN = "ic_qwen"
    const val DEEPSEEK = "ic_deepseek"
    const val GEMINI = "ic_gemini"
    const val CUSTOM = "ic_custom_ai"
}

/**
 * AI配置预设
 * 提供常用配置的快速选择
 */
object AIConfigPresets {
    // OpenAI 预设
    val OPENAI_GPT5 = AIConfig(
        name = "OpenAI GPT-5",
        icon = AIProviderIcons.OPENAI,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.OPENAI,
        apiUrl = "https://api.openai.com/v1/chat/completions",
        apiKey = "",
        modelId = "gpt-5",
        isImageUnderstanding = true
    )

    val OPENAI_GPT4O = AIConfig(
        name = "OpenAI GPT-4o",
        icon = AIProviderIcons.OPENAI,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.OPENAI,
        apiUrl = "https://api.openai.com/v1/chat/completions",
        apiKey = "",
        modelId = "gpt-4o",
        isImageUnderstanding = true
    )

    // Claude 预设
    val CLAUDE_4_6_OPUS = AIConfig(
        name = "Claude 4.6 Opus",
        icon = AIProviderIcons.CLAUDE,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.CLAUDE,
        apiUrl = "https://api.anthropic.com/v1/messages",
        apiKey = "",
        modelId = "claude-4-6-opus-20251001",
        isImageUnderstanding = true
    )

    val CLAUDE_3_5_SONNET = AIConfig(
        name = "Claude 3.5 Sonnet",
        icon = AIProviderIcons.CLAUDE,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.CLAUDE,
        apiUrl = "https://api.anthropic.com/v1/messages",
        apiKey = "",
        modelId = "claude-3-5-sonnet-20241022",
        isImageUnderstanding = true
    )

    // Kimi 预设
    val KIMI_K2_5 = AIConfig(
        name = "Kimi K2.5",
        icon = AIProviderIcons.KIMI,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.KIMI,
        apiUrl = "https://api.moonshot.cn/v1/chat/completions",
        apiKey = "",
        modelId = "kimi-k2-5",
        isImageUnderstanding = true
    )

    // GLM 预设
    val GLM_4_PLUS = AIConfig(
        name = "GLM-4 Plus",
        icon = AIProviderIcons.GLM,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.GLM,
        apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
        apiKey = "",
        modelId = "glm-4-plus",
        isImageUnderstanding = true
    )

    // Qwen 预设
    val QWEN_2_5_MAX = AIConfig(
        name = "Qwen 2.5 Max",
        icon = AIProviderIcons.QWEN,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.QWEN,
        apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
        apiKey = "",
        modelId = "qwen2.5-max",
        isImageUnderstanding = true
    )

    // DeepSeek 预设
    val DEEPSEEK_V3 = AIConfig(
        name = "DeepSeek V3",
        icon = AIProviderIcons.DEEPSEEK,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.DEEPSEEK,
        apiUrl = "https://api.deepseek.com/v1/chat/completions",
        apiKey = "",
        modelId = "deepseek-chat",
        isImageUnderstanding = false
    )

    val DEEPSEEK_R1 = AIConfig(
        name = "DeepSeek R1",
        icon = AIProviderIcons.DEEPSEEK,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.DEEPSEEK,
        apiUrl = "https://api.deepseek.com/v1/chat/completions",
        apiKey = "",
        modelId = "deepseek-reasoner",
        isImageUnderstanding = false
    )

    // Gemini 预设
    val GEMINI_2_0_PRO = AIConfig(
        name = "Gemini 2.0 Pro",
        icon = AIProviderIcons.GEMINI,
        iconType = IconType.RESOURCE,
        protocol = AIProtocol.GEMINI,
        apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-pro-exp:generateContent",
        apiKey = "",
        modelId = "gemini-2.0-pro-exp",
        isImageUnderstanding = true
    )

    // 所有预设列表
    val ALL_PRESETS = listOf(
        OPENAI_GPT5,
        OPENAI_GPT4O,
        CLAUDE_4_6_OPUS,
        CLAUDE_3_5_SONNET,
        KIMI_K2_5,
        GLM_4_PLUS,
        QWEN_2_5_MAX,
        DEEPSEEK_V3,
        DEEPSEEK_R1,
        GEMINI_2_0_PRO
    )

    // 图标选项（用于自定义图标选择）
    val ICON_OPTIONS = listOf("🤖", "🧠", "🔮", "⚡", "🎯", "🔥", "💎", "🌟")
}
