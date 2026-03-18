package com.calorieai.app.service

import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * AI预测服务
 * 提供体重预测、目标达成时间预测等功能
 */
@Singleton
class AIPredictionService @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val weightRecordRepository: WeightRecordRepository
) {

    /**
     * 体重预测结果
     */
    data class WeightPrediction(
        val predictedWeights: List<PredictedWeight>,  // 预测体重列表
        val targetDate: Date?,                        // 预计达成目标日期
        val confidence: Float,                        // 预测置信度 (0-1)
        val trend: WeightTrend,                       // 体重趋势
        val recommendation: String                    // 建议
    )

    /**
     * 预测体重数据点
     */
    data class PredictedWeight(
        val date: Date,
        val weight: Float,
        val confidenceInterval: Pair<Float, Float>  // 置信区间 (min, max)
    )

    /**
     * 体重趋势
     */
    enum class WeightTrend {
        RAPIDLY_LOSING,   // 快速减重
        STEADILY_LOSING,  // 稳定减重
        MAINTAINING,      // 维持
        GAINING,          // 增重
        FLUCTUATING       // 波动
    }

    /**
     * 预测体重变化
     * @param weeks 预测周数
     */
    suspend fun predictWeight(weeks: Int = 12): WeightPrediction {
        val userSettings = userSettingsRepository.getSettings().first() ?: return defaultPrediction()
        val weightHistory = weightRecordRepository.getRecentRecords(30).first()

        if (weightHistory.isEmpty()) {
            return predictBasedOnSettings(userSettings, weeks)
        }

        // 计算当前趋势
        val trend = calculateTrend(weightHistory)
        val currentWeight = weightHistory.first().weight
        val targetWeight = userSettings.targetWeight ?: currentWeight

        // 基于趋势和策略预测
        val strategy = userSettings.weightLossStrategy?.let { WeightLossStrategy.fromString(it) }
            ?: WeightLossStrategy.MODERATE

        val predictedWeights = mutableListOf<PredictedWeight>()
        val calendar = Calendar.getInstance()
        var predictedWeight = currentWeight

        for (week in 1..weeks) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)

            // 应用趋势和策略
            val weeklyChange = when (trend) {
                WeightTrend.RAPIDLY_LOSING -> -strategy.weeklyChange * 1.2f
                WeightTrend.STEADILY_LOSING -> -strategy.weeklyChange
                WeightTrend.MAINTAINING -> 0f
                WeightTrend.GAINING -> strategy.weeklyChange * 0.5f
                WeightTrend.FLUCTUATING -> -strategy.weeklyChange * 0.8f
            }

            predictedWeight += weeklyChange

            // 确保不低于目标体重太多
            if (targetWeight < currentWeight && predictedWeight < targetWeight) {
                predictedWeight = targetWeight
            }

            // 计算置信区间
            val confidenceRange = 0.5f + (week * 0.1f)  // 随时间增加不确定性
            val minWeight = predictedWeight * (1 - confidenceRange * 0.05f)
            val maxWeight = predictedWeight * (1 + confidenceRange * 0.05f)

            predictedWeights.add(
                PredictedWeight(
                    date = calendar.time,
                    weight = predictedWeight,
                    confidenceInterval = Pair(minWeight, maxWeight)
                )
            )
        }

        // 计算目标达成日期
        val targetDate = calculateTargetDate(currentWeight, targetWeight, trend, strategy)

        // 生成建议
        val recommendation = generateRecommendation(trend, strategy, currentWeight, targetWeight)

        return WeightPrediction(
            predictedWeights = predictedWeights,
            targetDate = targetDate,
            confidence = calculateConfidence(weightHistory.size, trend),
            trend = trend,
            recommendation = recommendation
        )
    }

    /**
     * 计算BMI预测
     */
    suspend fun predictBMI(): BMIPrediction {
        val userSettings = userSettingsRepository.getSettings().first() ?: return defaultBMIPrediction()
        val height = userSettings.userHeight ?: 170f
        val weightPrediction = predictWeight(4)

        val currentBMI = calculateBMI(weightPrediction.predictedWeights.first().weight, height)
        val predictedBMI = weightPrediction.predictedWeights.lastOrNull()?.let { record ->
            calculateBMI(record.weight, height)
        } ?: currentBMI

        return BMIPrediction(
            currentBMI = currentBMI,
            predictedBMI = predictedBMI,
            category = getBMICategory(currentBMI),
            targetCategory = getBMICategory(predictedBMI),
            healthRisk = getBMIHealthRisk(currentBMI)
        )
    }

    /**
     * BMI预测结果
     */
    data class BMIPrediction(
        val currentBMI: Float,
        val predictedBMI: Float,
        val category: BMICategory,
        val targetCategory: BMICategory,
        val healthRisk: HealthRisk
    )

    enum class BMICategory {
        UNDERWEIGHT,    // 偏瘦 < 18.5
        NORMAL,         // 正常 18.5 - 24
        OVERWEIGHT,     // 偏胖 24 - 28
        OBESE           // 肥胖 > 28
    }

    enum class HealthRisk {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    /**
     * 计算每日建议热量摄入
     */
    suspend fun calculateRecommendedCalories(): CalorieRecommendation {
        val userSettings = userSettingsRepository.getSettings().first() ?: return defaultCalorieRecommendation()

        val bmr = calculateBMR(userSettings)
        val tdee = calculateTDEE(bmr, userSettings.activityLevel)

        val goalType = userSettings.goalType?.let { com.calorieai.app.data.model.GoalType.fromString(it) }
        val strategy = userSettings.weightLossStrategy?.let { WeightLossStrategy.fromString(it) }
            ?: WeightLossStrategy.MODERATE

        val recommendedCalories = when (goalType) {
            com.calorieai.app.data.model.GoalType.LOSE_WEIGHT -> {
                (tdee * (1 - strategy.calorieDeficitPercent)).toInt()
            }
            com.calorieai.app.data.model.GoalType.GAIN_WEIGHT -> {
                (tdee * 1.15).toInt()
            }
            com.calorieai.app.data.model.GoalType.GAIN_MUSCLE -> {
                (tdee * 1.1).toInt()
            }
            else -> tdee
        }

        return CalorieRecommendation(
            bmr = bmr,
            tdee = tdee,
            recommended = recommendedCalories.coerceIn(1200, 4000),
            deficit = tdee - recommendedCalories,
            proteinGrams = (userSettings.userWeight?.times(1.6))?.toInt() ?: 100,
            carbGrams = (recommendedCalories * 0.45 / 4).toInt(),
            fatGrams = (recommendedCalories * 0.3 / 9).toInt()
        )
    }

    /**
     * 热量建议
     */
    data class CalorieRecommendation(
        val bmr: Int,           // 基础代谢率
        val tdee: Int,          // 每日总消耗
        val recommended: Int,   // 建议摄入
        val deficit: Int,       // 热量缺口
        val proteinGrams: Int,  // 建议蛋白质(g)
        val carbGrams: Int,     // 建议碳水(g)
        val fatGrams: Int       // 建议脂肪(g)
    )

    // 私有辅助方法

    private fun calculateTrend(weightHistory: List<com.calorieai.app.data.model.WeightRecord>): WeightTrend {
        if (weightHistory.size < 2) return WeightTrend.MAINTAINING

        val recent = weightHistory.take(7)
        val weightChanges = recent.zipWithNext { a, b -> a.weight - b.weight }
        val avgChange = weightChanges.average()

        return when {
            avgChange < -0.5 -> WeightTrend.RAPIDLY_LOSING
            avgChange < -0.1 -> WeightTrend.STEADILY_LOSING
            avgChange > 0.2 -> WeightTrend.GAINING
            weightChanges.any { abs(it) > 0.5 } -> WeightTrend.FLUCTUATING
            else -> WeightTrend.MAINTAINING
        }
    }

    private fun calculateTargetDate(
        currentWeight: Float,
        targetWeight: Float,
        trend: WeightTrend,
        strategy: WeightLossStrategy
    ): Date? {
        if (currentWeight <= targetWeight) return null

        val weightToLose = currentWeight - targetWeight
        val weeksNeeded = weightToLose / strategy.weeklyChange

        return Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, weeksNeeded.toInt().coerceAtLeast(1))
        }.time
    }

    private fun calculateConfidence(dataPoints: Int, trend: WeightTrend): Float {
        val baseConfidence = (dataPoints / 30f).coerceIn(0.3f, 1f)
        val trendFactor = when (trend) {
            WeightTrend.STEADILY_LOSING, WeightTrend.MAINTAINING -> 1f
            WeightTrend.RAPIDLY_LOSING -> 0.9f
            WeightTrend.FLUCTUATING -> 0.7f
            WeightTrend.GAINING -> 0.8f
        }
        return baseConfidence * trendFactor
    }

    private fun generateRecommendation(
        trend: WeightTrend,
        strategy: WeightLossStrategy,
        currentWeight: Float,
        targetWeight: Float
    ): String {
        return when (trend) {
            WeightTrend.RAPIDLY_LOSING -> "减重速度较快，建议适当增加热量摄入，避免肌肉流失"
            WeightTrend.STEADILY_LOSING -> "减重进度良好，继续保持当前节奏"
            WeightTrend.MAINTAINING -> "体重维持稳定，如需减重建议调整饮食结构"
            WeightTrend.GAINING -> "体重呈上升趋势，建议增加运动量或控制饮食"
            WeightTrend.FLUCTUATING -> "体重波动较大，建议保持规律的饮食和作息"
        }
    }

    private fun predictBasedOnSettings(userSettings: UserSettings, weeks: Int): WeightPrediction {
        val currentWeight = userSettings.userWeight ?: 70f
        val targetWeight = userSettings.targetWeight ?: currentWeight
        val strategy = userSettings.weightLossStrategy?.let { WeightLossStrategy.fromString(it) }
            ?: WeightLossStrategy.MODERATE

        val predictedWeights = mutableListOf<PredictedWeight>()
        val calendar = Calendar.getInstance()
        var weight = currentWeight

        for (week in 1..weeks) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            weight -= strategy.weeklyChange

            if (weight < targetWeight) weight = targetWeight

            predictedWeights.add(
                PredictedWeight(
                    date = calendar.time,
                    weight = weight,
                    confidenceInterval = Pair(weight * 0.95f, weight * 1.05f)
                )
            )
        }

        return WeightPrediction(
            predictedWeights = predictedWeights,
            targetDate = calculateTargetDate(currentWeight, targetWeight, WeightTrend.STEADILY_LOSING, strategy),
            confidence = 0.6f,
            trend = WeightTrend.STEADILY_LOSING,
            recommendation = "基于您的目标设定，建议保持${strategy.displayName}策略"
        )
    }

    private fun calculateBMR(userSettings: UserSettings): Int {
        val weight = userSettings.userWeight ?: 70f
        val height = userSettings.userHeight ?: 170f
        val age = userSettings.userAge ?: 30

        return if (userSettings.userGender == "MALE") {
            (10 * weight + 6.25 * height - 5 * age + 5).toInt()
        } else {
            (10 * weight + 6.25 * height - 5 * age - 161).toInt()
        }
    }

    private fun calculateTDEE(bmr: Int, activityLevel: String?): Int {
        val multiplier = when (activityLevel) {
            "SEDENTARY" -> 1.2f
            "LIGHT" -> 1.375f
            "MODERATE" -> 1.55f
            "ACTIVE" -> 1.725f
            "VERY_ACTIVE" -> 1.9f
            else -> 1.2f
        }
        return (bmr * multiplier).toInt()
    }

    private fun calculateBMI(weight: Float, heightCm: Float): Float {
        val heightM = heightCm / 100
        return weight / (heightM * heightM)
    }

    private fun getBMICategory(bmi: Float): BMICategory {
        return when {
            bmi < 18.5f -> BMICategory.UNDERWEIGHT
            bmi < 24f -> BMICategory.NORMAL
            bmi < 28f -> BMICategory.OVERWEIGHT
            else -> BMICategory.OBESE
        }
    }

    private fun getBMIHealthRisk(bmi: Float): HealthRisk {
        return when {
            bmi < 18.5f -> HealthRisk.MEDIUM
            bmi < 24f -> HealthRisk.LOW
            bmi < 28f -> HealthRisk.MEDIUM
            bmi < 32f -> HealthRisk.HIGH
            else -> HealthRisk.VERY_HIGH
        }
    }

    private fun defaultPrediction() = WeightPrediction(
        predictedWeights = emptyList(),
        targetDate = null,
        confidence = 0f,
        trend = WeightTrend.MAINTAINING,
        recommendation = "请先设置您的身体数据以获得预测"
    )

    private fun defaultBMIPrediction() = BMIPrediction(
        currentBMI = 22f,
        predictedBMI = 22f,
        category = BMICategory.NORMAL,
        targetCategory = BMICategory.NORMAL,
        healthRisk = HealthRisk.LOW
    )

    private fun defaultCalorieRecommendation() = CalorieRecommendation(
        bmr = 1500,
        tdee = 2000,
        recommended = 1800,
        deficit = 200,
        proteinGrams = 100,
        carbGrams = 200,
        fatGrams = 60
    )
}
