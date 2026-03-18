package com.calorieai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI功能配置实体
 * 存储不同功能使用的AI配置ID
 */
@Entity(tableName = "ai_function_configs")
data class AIFunctionConfig(
    @PrimaryKey
    val functionType: AIFunctionType,
    val configId: String,  // 对应的AIConfig ID
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * AI功能类型
 */
enum class AIFunctionType {
    FOOD_IMAGE_ANALYSIS,    // 拍照识别食物
    FOOD_TEXT_ANALYSIS,     // 文本导入食物
    AI_CHAT                 // AI对话助手
}

/**
 * 获取功能类型的显示名称
 */
fun AIFunctionType.getDisplayName(): String = when (this) {
    AIFunctionType.FOOD_IMAGE_ANALYSIS -> "拍照识别"
    AIFunctionType.FOOD_TEXT_ANALYSIS -> "AI导入"
    AIFunctionType.AI_CHAT -> "AI对话"
}

/**
 * 获取功能类型的描述
 */
fun AIFunctionType.getDescription(): String = when (this) {
    AIFunctionType.FOOD_IMAGE_ANALYSIS -> "用于拍照识别食物营养成分"
    AIFunctionType.FOOD_TEXT_ANALYSIS -> "用于文本描述导入食物"
    AIFunctionType.AI_CHAT -> "用于AI营养助手对话"
}

/**
 * 获取功能类型的推荐模型
 */
fun AIFunctionType.getRecommendedModel(): String = when (this) {
    AIFunctionType.FOOD_IMAGE_ANALYSIS -> "LongCat-Flash-Omni-2603"
    AIFunctionType.FOOD_TEXT_ANALYSIS -> "LongCat-Flash-Lite"
    AIFunctionType.AI_CHAT -> "LongCat-Flash-Thinking-2601"
}

/**
 * 默认功能配置
 * 拍照识别：OMNI（多模态）
 * AI导入：LITE（轻量快速）
 * AI对话：THINKING（深度思考）
 */
object AIFunctionConfigDefaults {
    // 默认配置ID（对应AIConfigPresets中的配置）
    const val DEFAULT_IMAGE_ANALYSIS_CONFIG_ID = AIConfigPresets.ID_LONGCAT_FLASH_OMNI
    const val DEFAULT_TEXT_ANALYSIS_CONFIG_ID = AIConfigPresets.ID_LONGCAT_FLASH_LITE
    const val DEFAULT_CHAT_CONFIG_ID = AIConfigPresets.ID_LONGCAT_FLASH_THINKING
}
