package com.calorieai.app.data.model

/**
 * 营养素参考摄入量
 * 基于中国居民膳食指南和WHO建议
 */
data class NutritionReference(
    val name: String,           // 营养素名称
    val unit: String,           // 单位
    val dailyRecommended: Float, // 每日推荐摄入量
    val dailyMax: Float? = null, // 每日最大摄入量（可选）
    val icon: String,           // 图标emoji
    val color: NutritionColor   // 颜色分类
)

enum class NutritionColor {
    PROTEIN,    // 蛋白质 - 蓝色
    CARB,       // 碳水 - 绿色
    FAT,        // 脂肪 - 黄色
    VITAMIN,    // 维生素 - 橙色
    MINERAL,    // 矿物质 - 紫色
    OTHER       // 其他 - 灰色
}

/**
 * 默认营养素参考值（基于成人每日推荐摄入量）
 */
object NutritionReferences {
    // 基础营养素
    val PROTEIN = NutritionReference(
        name = "蛋白质",
        unit = "g",
        dailyRecommended = 60f,
        icon = "🥩",
        color = NutritionColor.PROTEIN
    )

    val CARBS = NutritionReference(
        name = "碳水化合物",
        unit = "g",
        dailyRecommended = 300f,
        icon = "🍚",
        color = NutritionColor.CARB
    )

    val FAT = NutritionReference(
        name = "脂肪",
        unit = "g",
        dailyRecommended = 60f,
        dailyMax = 80f,
        icon = "🥑",
        color = NutritionColor.FAT
    )

    // 扩展营养素
    val FIBER = NutritionReference(
        name = "膳食纤维",
        unit = "g",
        dailyRecommended = 25f,
        icon = "🌾",
        color = NutritionColor.CARB
    )

    val SUGAR = NutritionReference(
        name = "糖分",
        unit = "g",
        dailyRecommended = 50f,
        dailyMax = 50f,
        icon = "🍬",
        color = NutritionColor.CARB
    )

    val SODIUM = NutritionReference(
        name = "钠",
        unit = "mg",
        dailyRecommended = 2000f,
        dailyMax = 2300f,
        icon = "🧂",
        color = NutritionColor.MINERAL
    )

    val CHOLESTEROL = NutritionReference(
        name = "胆固醇",
        unit = "mg",
        dailyRecommended = 300f,
        dailyMax = 300f,
        icon = "🥚",
        color = NutritionColor.FAT
    )

    val SATURATED_FAT = NutritionReference(
        name = "饱和脂肪",
        unit = "g",
        dailyRecommended = 20f,
        dailyMax = 25f,
        icon = "🧈",
        color = NutritionColor.FAT
    )

    val CALCIUM = NutritionReference(
        name = "钙",
        unit = "mg",
        dailyRecommended = 800f,
        icon = "🥛",
        color = NutritionColor.MINERAL
    )

    val IRON = NutritionReference(
        name = "铁",
        unit = "mg",
        dailyRecommended = 12f,
        icon = "🥬",
        color = NutritionColor.MINERAL
    )

    val VITAMIN_C = NutritionReference(
        name = "维生素C",
        unit = "mg",
        dailyRecommended = 100f,
        icon = "🍊",
        color = NutritionColor.VITAMIN
    )

    val VITAMIN_A = NutritionReference(
        name = "维生素A",
        unit = "μg",
        dailyRecommended = 800f,
        icon = "🥕",
        color = NutritionColor.VITAMIN
    )

    val POTASSIUM = NutritionReference(
        name = "钾",
        unit = "mg",
        dailyRecommended = 2000f,
        icon = "🍌",
        color = NutritionColor.MINERAL
    )

    // 所有营养素列表
    val ALL = listOf(
        PROTEIN, CARBS, FAT, FIBER, SUGAR,
        SODIUM, CHOLESTEROL, SATURATED_FAT,
        CALCIUM, IRON, VITAMIN_C, VITAMIN_A, POTASSIUM
    )

    // 基础营养素（始终显示）
    val BASIC = listOf(PROTEIN, CARBS, FAT)

    // 扩展营养素（可选显示）
    val EXTENDED = listOf(
        FIBER, SUGAR, SODIUM, CHOLESTEROL,
        SATURATED_FAT, CALCIUM, IRON, VITAMIN_C, VITAMIN_A, POTASSIUM
    )
}

/**
 * 计算营养素摄入进度
 */
fun calculateNutritionProgress(current: Float, recommended: Float): Float {
    return (current / recommended).coerceIn(0f, 1.5f)
}

/**
 * 获取营养素状态
 */
fun getNutritionStatus(current: Float, recommended: Float, max: Float? = null): NutritionStatus {
    val percentage = current / recommended
    return when {
        max != null && current > max -> NutritionStatus.EXCESS
        percentage < 0.5f -> NutritionStatus.LOW
        percentage < 0.8f -> NutritionStatus.BELOW_TARGET
        percentage <= 1.2f -> NutritionStatus.GOOD
        percentage <= 1.5f -> NutritionStatus.HIGH
        else -> NutritionStatus.EXCESS
    }
}

enum class NutritionStatus {
    LOW,           // 过低
    BELOW_TARGET,  // 略低于目标
    GOOD,          // 良好
    HIGH,          // 偏高
    EXCESS         // 过量
}

fun NutritionStatus.getColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        NutritionStatus.LOW -> androidx.compose.ui.graphics.Color(0xFF2196F3)      // 蓝色
        NutritionStatus.BELOW_TARGET -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // 绿色
        NutritionStatus.GOOD -> androidx.compose.ui.graphics.Color(0xFF8BC34A)     // 浅绿
        NutritionStatus.HIGH -> androidx.compose.ui.graphics.Color(0xFFFF9800)     // 橙色
        NutritionStatus.EXCESS -> androidx.compose.ui.graphics.Color(0xFFF44336)   // 红色
    }
}

fun NutritionStatus.getLabel(): String {
    return when (this) {
        NutritionStatus.LOW -> "不足"
        NutritionStatus.BELOW_TARGET -> "偏低"
        NutritionStatus.GOOD -> "适宜"
        NutritionStatus.HIGH -> "偏高"
        NutritionStatus.EXCESS -> "过量"
    }
}
