package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import kotlinx.coroutines.flow.firstOrNull
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
    private val weightRecordRepository: WeightRecordRepository
) {
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
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)
        
        val startTime = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val foodRecords = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        return foodRecords.isNotEmpty()
    }
}
