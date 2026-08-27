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
        val targetDate = calculateTargetDate(currentWeight, targetWeight, strategy)

        // 生成建议
        val recommendation = generateRecommendation(trend)

        return WeightPrediction(
            predictedWeights = predictedWeights,
            targetDate = targetDate,
            confidence = calculateConfidence(weightHistory.size, trend),
            trend = trend,
            recommendation = recommendation
        )
    }

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

    private fun generateRecommendation(trend: WeightTrend): String {
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
            targetDate = calculateTargetDate(currentWeight, targetWeight, strategy),
            confidence = 0.6f,
            trend = WeightTrend.STEADILY_LOSING,
            recommendation = "基于您的目标设定，建议保持${strategy.displayName}策略"
        )
    }

    private fun defaultPrediction() = WeightPrediction(
        predictedWeights = emptyList(),
        targetDate = null,
        confidence = 0f,
        trend = WeightTrend.MAINTAINING,
        recommendation = "请先设置您的身体数据以获得预测"
    )

}
