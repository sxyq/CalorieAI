package com.calorieai.app.data.model

import androidx.compose.ui.graphics.Color

/**
 * 营养素参考值数据类
 * 基于用户个人信息（体重、性别、年龄）动态计算
 */
data class NutritionReference(
    val id: String,
    val name: String,
    val unit: String,
    val dailyRecommended: Float,
    val dailyMax: Float? = null,
    val icon: String,
    val color: Color
)

/**
 * 用户身体数据
 * 用于计算个性化营养素需求
 */
data class UserBodyProfile(
    val weight: Float,      // 体重(kg)
    val gender: String,     // "MALE" 或 "FEMALE"
    val age: Int,           // 年龄
    val height: Float? = null,  // 身高(cm)，可选
    val activityLevel: String = "MODERATE"  // 活动水平
)

/**
 * 营养素参考值计算器
 * 根据用户身体数据计算个性化营养素需求
 */
object NutritionCalculator {

    /**
     * 计算每日蛋白质需求
     * 公式: 体重(kg) × 系数
     * 一般成年人: 0.8-1.0g/kg
     * 运动人群: 1.2-2.0g/kg
     */
    fun calculateProtein(weight: Float, activityLevel: String = "MODERATE"): Float {
        val baseMultiplier = when (activityLevel) {
            "SEDENTARY" -> 0.8f      // 久坐
            "LIGHT" -> 1.0f          // 轻度活动
            "MODERATE" -> 1.2f       // 中度活动
            "ACTIVE" -> 1.5f         // 活跃
            "VERY_ACTIVE" -> 1.8f    // 非常活跃
            else -> 1.2f
        }
        return weight * baseMultiplier
    }

    /**
     * 计算每日碳水化合物需求
     * 公式: 基于总热量需求的45-65%
     * 默认按2000千卡计算，碳水占55%
     * 1g碳水 = 4千卡
     */
    fun calculateCarbs(weight: Float, activityLevel: String = "MODERATE"): Float {
        // 根据活动水平调整总热量需求
        val baseCalories = when (activityLevel) {
            "SEDENTARY" -> weight * 22
            "LIGHT" -> weight * 26
            "MODERATE" -> weight * 30
            "ACTIVE" -> weight * 34
            "VERY_ACTIVE" -> weight * 38
            else -> weight * 30
        }
        // 碳水占55%，1g碳水=4千卡
        return (baseCalories * 0.55f / 4).coerceIn(150f, 400f)
    }

    /**
     * 计算每日脂肪需求
     * 公式: 基于总热量需求的20-35%
     * 1g脂肪 = 9千卡
     */
    fun calculateFat(weight: Float, activityLevel: String = "MODERATE"): Float {
        val baseCalories = when (activityLevel) {
            "SEDENTARY" -> weight * 22
            "LIGHT" -> weight * 26
            "MODERATE" -> weight * 30
            "ACTIVE" -> weight * 34
            "VERY_ACTIVE" -> weight * 38
            else -> weight * 30
        }
        // 脂肪占30%，1g脂肪=9千卡
        return (baseCalories * 0.30f / 9).coerceIn(40f, 90f)
    }

    /**
     * 计算每日膳食纤维需求
     * 公式: 14g per 1000千卡
     * 最低25g，最高38g
     */
    fun calculateFiber(weight: Float, activityLevel: String = "MODERATE"): Float {
        val baseCalories = when (activityLevel) {
            "SEDENTARY" -> weight * 22
            "LIGHT" -> weight * 26
            "MODERATE" -> weight * 30
            "ACTIVE" -> weight * 34
            "VERY_ACTIVE" -> weight * 38
            else -> weight * 30
        }
        return (baseCalories / 1000 * 14).coerceIn(25f, 38f)
    }

    /**
     * 计算每日糖分上限
     * WHO建议: 不超过总热量的10%
     * 约50g为通用上限
     */
    fun calculateSugarLimit(weight: Float): Float {
        // 体重越大，糖分上限略高，但不超过50g
        return (weight * 0.7f).coerceIn(30f, 50f)
    }

    /**
     * 计算每日钠需求
     * 中国居民膳食指南: 2000mg (约5g盐)
     * 根据体重微调
     */
    fun calculateSodium(weight: Float): Float {
        // 基础2000mg，体重越大需求略增
        return (1800 + weight * 3).coerceIn(1500f, 2300f)
    }

    /**
     * 计算每日胆固醇上限
     * 通用建议: 300mg
     */
    fun calculateCholesterolLimit(): Float = 300f

    /**
     * 计算每日饱和脂肪上限
     * 不超过总热量的10%
     */
    fun calculateSaturatedFatLimit(weight: Float, activityLevel: String = "MODERATE"): Float {
        val baseCalories = when (activityLevel) {
            "SEDENTARY" -> weight * 22
            "LIGHT" -> weight * 26
            "MODERATE" -> weight * 30
            "ACTIVE" -> weight * 34
            "VERY_ACTIVE" -> weight * 38
            else -> weight * 30
        }
        // 饱和脂肪占10%，1g脂肪=9千卡
        return (baseCalories * 0.10f / 9).coerceIn(15f, 25f)
    }

    /**
     * 计算每日钙需求
     * 成年人: 800mg
     * 青少年/孕妇/老年人: 1000-1200mg
     */
    fun calculateCalcium(age: Int, gender: String): Float {
        return when {
            age < 18 -> 1000f  // 青少年
            age in 19..50 -> 800f  // 成年人
            age > 50 -> 1000f  // 老年人
            else -> 800f
        }
    }

    /**
     * 计算每日铁需求
     * 男性: 12mg
     * 女性: 20mg (生理期需求更高)
     */
    fun calculateIron(gender: String): Float {
        return if (gender == "FEMALE") 20f else 12f
    }

    /**
     * 计算每日维生素C需求
     * 成年人: 100mg
     * 吸烟者/压力大者: 更高
     */
    fun calculateVitaminC(weight: Float): Float {
        // 基础100mg，体重越大需求略增
        return (80 + weight * 0.3f).coerceIn(100f, 150f)
    }

    /**
     * 计算每日维生素A需求
     * 男性: 800μg
     * 女性: 700μg
     */
    fun calculateVitaminA(gender: String): Float {
        return if (gender == "FEMALE") 700f else 800f
    }

    /**
     * 计算每日钾需求
     * 成年人: 2000mg
     */
    fun calculatePotassium(weight: Float): Float {
        // 基础2000mg，体重越大需求略增
        return (1800 + weight * 4).coerceIn(2000f, 3500f)
    }

    /**
     * 根据用户身体数据计算所有营养素参考值
     */
    fun calculateAll(userProfile: UserBodyProfile): List<NutritionReference> {
        return listOf(
            NutritionReference(
                id = "protein",
                name = "蛋白质",
                unit = "g",
                dailyRecommended = calculateProtein(userProfile.weight, userProfile.activityLevel),
                dailyMax = null,
                icon = "💪",
                color = Color(0xFF4CAF50)
            ),
            NutritionReference(
                id = "carbs",
                name = "碳水化合物",
                unit = "g",
                dailyRecommended = calculateCarbs(userProfile.weight, userProfile.activityLevel),
                dailyMax = null,
                icon = "🍞",
                color = Color(0xFFFF9800)
            ),
            NutritionReference(
                id = "fat",
                name = "脂肪",
                unit = "g",
                dailyRecommended = calculateFat(userProfile.weight, userProfile.activityLevel),
                dailyMax = null,
                icon = "🥑",
                color = Color(0xFFFFC107)
            ),
            NutritionReference(
                id = "fiber",
                name = "膳食纤维",
                unit = "g",
                dailyRecommended = calculateFiber(userProfile.weight, userProfile.activityLevel),
                dailyMax = null,
                icon = "🌾",
                color = Color(0xFF8BC34A)
            ),
            NutritionReference(
                id = "sugar",
                name = "糖分",
                unit = "g",
                dailyRecommended = calculateSugarLimit(userProfile.weight),
                dailyMax = calculateSugarLimit(userProfile.weight),
                icon = "🍯",
                color = Color(0xFFE91E63)
            ),
            NutritionReference(
                id = "sodium",
                name = "钠",
                unit = "mg",
                dailyRecommended = calculateSodium(userProfile.weight),
                dailyMax = calculateSodium(userProfile.weight),
                icon = "🧂",
                color = Color(0xFF9E9E9E)
            ),
            NutritionReference(
                id = "cholesterol",
                name = "胆固醇",
                unit = "mg",
                dailyRecommended = calculateCholesterolLimit(),
                dailyMax = calculateCholesterolLimit(),
                icon = "🥚",
                color = Color(0xFFFF5722)
            ),
            NutritionReference(
                id = "saturated_fat",
                name = "饱和脂肪",
                unit = "g",
                dailyRecommended = calculateSaturatedFatLimit(userProfile.weight, userProfile.activityLevel),
                dailyMax = calculateSaturatedFatLimit(userProfile.weight, userProfile.activityLevel),
                icon = "🧈",
                color = Color(0xFFFF9800)
            ),
            NutritionReference(
                id = "calcium",
                name = "钙",
                unit = "mg",
                dailyRecommended = calculateCalcium(userProfile.age, userProfile.gender),
                dailyMax = null,
                icon = "🥛",
                color = Color(0xFF2196F3)
            ),
            NutritionReference(
                id = "iron",
                name = "铁",
                unit = "mg",
                dailyRecommended = calculateIron(userProfile.gender),
                dailyMax = null,
                icon = "🥩",
                color = Color(0xFF795548)
            ),
            NutritionReference(
                id = "vitamin_c",
                name = "维生素C",
                unit = "mg",
                dailyRecommended = calculateVitaminC(userProfile.weight),
                dailyMax = null,
                icon = "🍊",
                color = Color(0xFFFF9800)
            ),
            NutritionReference(
                id = "vitamin_a",
                name = "维生素A",
                unit = "μg",
                dailyRecommended = calculateVitaminA(userProfile.gender),
                dailyMax = null,
                icon = "🥕",
                color = Color(0xFFFFA726)
            ),
            NutritionReference(
                id = "potassium",
                name = "钾",
                unit = "mg",
                dailyRecommended = calculatePotassium(userProfile.weight),
                dailyMax = null,
                icon = "🍌",
                color = Color(0xFF4CAF50)
            )
        )
    }

    /**
     * 获取默认营养素参考值（当用户数据不可用时）
     */
    fun getDefaultReferences(): List<NutritionReference> {
        // 使用默认体重70kg，男性，30岁，中度活动
        val defaultProfile = UserBodyProfile(
            weight = 70f,
            gender = "MALE",
            age = 30,
            activityLevel = "MODERATE"
        )
        return calculateAll(defaultProfile)
    }
}

/**
 * 计算营养素摄入状态
 */
fun calculateNutritionStatus(
    currentValue: Float,
    reference: NutritionReference
): NutritionStatus {
    val progress = if (reference.dailyRecommended > 0) {
        currentValue / reference.dailyRecommended
    } else 0f

    return when {
        progress < 0.3f -> NutritionStatus.LOW
        progress < 0.7f -> NutritionStatus.MEDIUM
        else -> NutritionStatus.GOOD
    }
}

enum class NutritionStatus {
    LOW, MEDIUM, GOOD
}
