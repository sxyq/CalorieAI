package com.calorieai.app.service.ai

import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI上下文服务
 * 为AI助手提供用户数据上下文
 */
@Singleton
class AIContextService @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val weightRecordRepository: WeightRecordRepository,
    private val waterRecordRepository: WaterRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) {
    private fun getRange(days: Long): Pair<Long, Long> {
        val today = LocalDate.now()
        val start = today.minusDays(days - 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val end = today.atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return start to end
    }

    private fun formatDate(millis: Long): String {
        return java.time.Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    /**
     * 获取用户最近一周的饮食记录上下文
     */
    suspend fun getWeeklyFoodContext(): String {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)
        
        val startTime = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val records = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        
        if (records.isEmpty()) {
            return "用户最近一周没有饮食记录。"
        }
        
        val formatter = DateTimeFormatter.ofPattern("MM-dd")
        
        val contextBuilder = StringBuilder()
        contextBuilder.append("用户最近一周的饮食记录：\n\n")
        
        var totalCalories = 0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        
        // 按日期分组
        val groupedByDate = records.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        
        groupedByDate.toSortedMap(reverseOrder()).forEach { (date, dayRecords) ->
            val dayCalories = dayRecords.sumOf { it.totalCalories }
            totalCalories += dayCalories
            totalProtein += dayRecords.sumOf { it.protein.toDouble() }
            totalCarbs += dayRecords.sumOf { it.carbs.toDouble() }
            totalFat += dayRecords.sumOf { it.fat.toDouble() }
            
            contextBuilder.append("【${date.format(formatter)}】总热量: ${dayCalories}kcal\n")
            dayRecords.forEach { record ->
                contextBuilder.append("  - ${record.foodName}: ${record.totalCalories}kcal")
                contextBuilder.append(" (蛋白质${record.protein.toInt()}g, 碳水${record.carbs.toInt()}g, 脂肪${record.fat.toInt()}g)\n")
            }
            contextBuilder.append("\n")
        }
        
        val days = groupedByDate.size
        contextBuilder.append("【周统计】\n")
        contextBuilder.append("平均每日热量: ${totalCalories / days}kcal\n")
        contextBuilder.append("平均每日蛋白质: ${(totalProtein / days).toInt()}g\n")
        contextBuilder.append("平均每日碳水: ${(totalCarbs / days).toInt()}g\n")
        contextBuilder.append("平均每日脂肪: ${(totalFat / days).toInt()}g\n")
        
        return contextBuilder.toString()
    }
    
    /**
     * 获取用户最近一周的运动记录上下文
     */
    suspend fun getWeeklyExerciseContext(): String {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)
        
        val startTime = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val records = exerciseRecordRepository.getRecordsBetweenSync(startTime, endTime)
        
        if (records.isEmpty()) {
            return "用户最近一周没有运动记录。"
        }
        
        val formatter = DateTimeFormatter.ofPattern("MM-dd")
        val contextBuilder = StringBuilder()
        contextBuilder.append("用户最近一周的运动记录：\n\n")
        
        var totalCalories = 0
        var totalMinutes = 0
        
        // 按日期分组
        val groupedByDate = records.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        
        groupedByDate.toSortedMap(reverseOrder()).forEach { (date, dayRecords) ->
            val dayCalories = dayRecords.sumOf { it.caloriesBurned }
            val dayMinutes = dayRecords.sumOf { it.durationMinutes }
            totalCalories += dayCalories
            totalMinutes += dayMinutes
            
            contextBuilder.append("【${date.format(formatter)}】消耗: ${dayCalories}kcal, 时长: ${dayMinutes}分钟\n")
            dayRecords.forEach { record ->
                contextBuilder.append("  - ${record.exerciseType.displayName}: ${record.caloriesBurned}kcal, ${record.durationMinutes}分钟")
                if (!record.notes.isNullOrBlank()) {
                    contextBuilder.append(" (${record.notes})")
                }
                contextBuilder.append("\n")
            }
        }
        
        contextBuilder.append("\n【周统计】总消耗: ${totalCalories}kcal, 总时长: ${totalMinutes}分钟\n")
        
        return contextBuilder.toString()
    }
    
    /**
     * 获取用户最近一周的体重变化上下文
     */
    suspend fun getWeeklyWeightContext(): String {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)
        
        val startTime = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val records = weightRecordRepository.getRecordsBetweenSync(startTime, endTime)
        
        if (records.isEmpty()) {
            return "用户最近一周没有体重记录。"
        }
        
        val formatter = DateTimeFormatter.ofPattern("MM-dd")
        val contextBuilder = StringBuilder()
        contextBuilder.append("用户最近一周的体重变化：\n\n")
        
        val sortedRecords = records.sortedBy { it.recordDate }
        sortedRecords.forEach { record ->
            val date = java.time.Instant.ofEpochMilli(record.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            contextBuilder.append("${date.format(formatter)}: ${record.weight}kg")
            if (!record.note.isNullOrBlank()) {
                contextBuilder.append(" (${record.note})")
            }
            contextBuilder.append("\n")
        }
        
        if (sortedRecords.size >= 2) {
            val firstWeight = sortedRecords.first().weight
            val lastWeight = sortedRecords.last().weight
            val change = lastWeight - firstWeight
            contextBuilder.append("\n体重变化: ${if (change >= 0) "+" else ""}${String.format("%.1f", change)}kg\n")
        }
        
        return contextBuilder.toString()
    }
    
    /**
     * 获取完整的健康评估上下文
     */
    suspend fun getHealthAssessmentContext(): String {
        val foodContext = getWeeklyFoodContext()
        val exerciseContext = getWeeklyExerciseContext()
        val weightContext = getWeeklyWeightContext()
        
        return """
            $foodContext
            
            $exerciseContext
            
            $weightContext
        """.trimIndent()
    }
    
    /**
     * 检查是否有足够的数据进行分析
     */
    suspend fun hasEnoughDataForAnalysis(): Boolean {
        val (startTime, endTime) = getRange(7)
        val foodRecords = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val waterRecords = waterRecordRepository.getRecordsBetweenSync(startTime, endTime)
        return foodRecords.isNotEmpty() || exerciseRecords.isNotEmpty() || weightRecords.isNotEmpty() || waterRecords.isNotEmpty()
    }

    suspend fun hasEnoughFoodDataForCalorieAssessment(minDays: Int = 3): Boolean {
        val (startTime, endTime) = getRange(14)
        val foodRecords = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        if (foodRecords.isEmpty()) return false
        val activeDays = foodRecords.groupBy {
            java.time.Instant.ofEpochMilli(it.recordTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.size
        return activeDays >= minDays
    }

    /**
     * 获取快捷功能使用的本地近期数据上下文（强制包含本地数据摘要）
     */
    suspend fun getQuickActionContext(action: String, recentDays: Long = 14): String {
        val (startTime, endTime) = getRange(recentDays)
        val foodRecords = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val exerciseRecords = exerciseRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val weightRecords = weightRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val waterRecords = waterRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val settings = userSettingsRepository.getSettingsOnce()

        val period = "${formatDate(startTime)} 至 ${formatDate(endTime)}"
        val sb = StringBuilder()
        sb.appendLine("【本地近期数据快照】")
        sb.appendLine("统计区间：$period（最近${recentDays}天）")
        sb.appendLine("用途：$action")
        sb.appendLine()

        val foodDays = foodRecords.groupBy {
            java.time.Instant.ofEpochMilli(it.recordTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val totalCalories = foodRecords.sumOf { it.totalCalories }
        val avgCalories = if (foodDays.isNotEmpty()) totalCalories / foodDays.size else 0
        sb.appendLine("### 饮食记录")
        sb.appendLine("- 记录条数：${foodRecords.size}")
        sb.appendLine("- 活跃天数：${foodDays.size}")
        sb.appendLine("- 总摄入热量：${totalCalories} kcal")
        sb.appendLine("- 日均摄入热量：${avgCalories} kcal")
        if (foodRecords.isNotEmpty()) {
            val latest = foodRecords.maxByOrNull { it.recordTime }
            latest?.let {
                sb.appendLine("- 最近一条：${formatDate(it.recordTime)} ${it.foodName}（${it.totalCalories} kcal）")
            }
        }
        sb.appendLine()

        val totalExerciseCalories = exerciseRecords.sumOf { it.caloriesBurned }
        val totalExerciseMinutes = exerciseRecords.sumOf { it.durationMinutes }
        sb.appendLine("### 运动记录")
        sb.appendLine("- 记录条数：${exerciseRecords.size}")
        sb.appendLine("- 总消耗热量：${totalExerciseCalories} kcal")
        sb.appendLine("- 总运动时长：${totalExerciseMinutes} 分钟")
        if (exerciseRecords.isNotEmpty()) {
            val latest = exerciseRecords.maxByOrNull { it.recordTime }
            latest?.let {
                sb.appendLine("- 最近一条：${formatDate(it.recordTime)} ${it.exerciseType.displayName}（${it.caloriesBurned} kcal）")
            }
        }
        sb.appendLine()

        val sortedWeight = weightRecords.sortedBy { it.recordDate }
        sb.appendLine("### 体重记录")
        sb.appendLine("- 记录条数：${sortedWeight.size}")
        if (sortedWeight.size >= 2) {
            val change = sortedWeight.last().weight - sortedWeight.first().weight
            sb.appendLine("- 体重变化：${if (change >= 0f) "+" else ""}${String.format("%.1f", change)} kg")
        }
        sortedWeight.lastOrNull()?.let {
            sb.appendLine("- 最近体重：${it.weight} kg（${formatDate(it.recordDate)}）")
        }
        sb.appendLine()

        val waterTotal = waterRecords.sumOf { it.amount }
        val waterByDay = waterRecords.groupBy {
            java.time.Instant.ofEpochMilli(it.recordTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val avgWater = if (waterByDay.isNotEmpty()) waterTotal / waterByDay.size else 0
        sb.appendLine("### 饮水记录")
        sb.appendLine("- 记录条数：${waterRecords.size}")
        sb.appendLine("- 总饮水量：${waterTotal} ml")
        sb.appendLine("- 日均饮水量：${avgWater} ml")
        sb.appendLine()

        sb.appendLine("### 目标与设定")
        sb.appendLine("- 每日热量目标：${settings?.dailyCalorieGoal ?: 2000} kcal")
        sb.appendLine("- 每日饮水目标：${settings?.dailyWaterGoal ?: 2000} ml")
        settings?.userWeight?.let { sb.appendLine("- 当前体重：${it} kg") }
        settings?.targetWeight?.let { sb.appendLine("- 目标体重：${it} kg") }
        settings?.goalType?.let { sb.appendLine("- 目标类型：$it") }
        sb.appendLine()

        sb.appendLine("请严格基于上述本地近期数据给出结论和建议；若数据不足，明确指出缺少哪些记录。")
        return sb.toString()
    }
}
