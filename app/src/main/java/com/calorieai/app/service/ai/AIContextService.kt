package com.calorieai.app.service.ai

import com.calorieai.app.data.model.NutritionCalculator
import com.calorieai.app.data.model.UserBodyProfile
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
    private data class NutrientSnapshot(
        val protein: Float = 0f,
        val carbs: Float = 0f,
        val fat: Float = 0f,
        val fiber: Float = 0f,
        val calcium: Float = 0f,
        val iron: Float = 0f,
        val vitaminC: Float = 0f,
        val potassium: Float = 0f
    )

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

    /**
     * 获取AI个性化约束上下文（忌口/口味/预算/时长/特定人群）
     */
    suspend fun getDietaryConstraintContext(): String {
        val settings = userSettingsRepository.getSettingsOnce()
        val allergens = settings?.dietaryAllergens?.takeIf { it.isNotBlank() } ?: "无明确过敏原"
        val flavors = settings?.flavorPreferences?.takeIf { it.isNotBlank() } ?: "未设置口味偏好"
        val budget = settings?.budgetPreference?.takeIf { it.isNotBlank() } ?: "未设置预算偏好"
        val maxCooking = settings?.maxCookingMinutes?.let { "${it}分钟内" } ?: "未限制烹饪时长"
        val specialMode = when (settings?.specialPopulationMode) {
            "DIABETES" -> "控糖"
            "GOUT" -> "痛风"
            "PREGNANCY" -> "孕期"
            "CHILD" -> "儿童"
            "FITNESS" -> "健身"
            else -> "通用健康"
        }

        return buildString {
            appendLine("【个性化饮食约束】")
            appendLine("- 过敏原/忌口：$allergens")
            appendLine("- 口味偏好：$flavors")
            appendLine("- 预算偏好：$budget")
            appendLine("- 烹饪时长约束：$maxCooking")
            appendLine("- 特定人群模式：$specialMode")
        }
    }

    /**
     * 获取指定天数营养缺口分析上下文（自动对比推荐摄入）
     */
    suspend fun getNutritionGapContext(days: Long): String {
        val safeDays = days.coerceAtLeast(1)
        val (startTime, endTime) = getRange(safeDays)
        val foodRecords = foodRecordRepository.getRecordsBetweenSync(startTime, endTime)
        val settings = userSettingsRepository.getSettingsOnce()

        val profile = UserBodyProfile(
            weight = settings?.userWeight ?: 70f,
            gender = settings?.userGender ?: "MALE",
            age = settings?.userAge ?: 30,
            height = settings?.userHeight,
            activityLevel = settings?.activityLevel ?: "MODERATE"
        )
        val references = NutritionCalculator.calculateAll(profile).associateBy { it.id }

        val dayCount = safeDays.toFloat()
        val totals = NutrientSnapshot(
            protein = foodRecords.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = foodRecords.sumOf { it.carbs.toDouble() }.toFloat(),
            fat = foodRecords.sumOf { it.fat.toDouble() }.toFloat(),
            fiber = foodRecords.sumOf { it.fiber.toDouble() }.toFloat(),
            calcium = foodRecords.sumOf { it.calcium.toDouble() }.toFloat(),
            iron = foodRecords.sumOf { it.iron.toDouble() }.toFloat(),
            vitaminC = foodRecords.sumOf { it.vitaminC.toDouble() }.toFloat(),
            potassium = foodRecords.sumOf { it.potassium.toDouble() }.toFloat()
        )
        val avg = NutrientSnapshot(
            protein = totals.protein / dayCount,
            carbs = totals.carbs / dayCount,
            fat = totals.fat / dayCount,
            fiber = totals.fiber / dayCount,
            calcium = totals.calcium / dayCount,
            iron = totals.iron / dayCount,
            vitaminC = totals.vitaminC / dayCount,
            potassium = totals.potassium / dayCount
        )

        fun gapLine(id: String, name: String, value: Float): String {
            val ref = references[id]?.dailyRecommended ?: 0f
            if (ref <= 0f) return "- $name：平均 ${String.format("%.1f", value)}"
            val percent = (value / ref * 100f).coerceAtLeast(0f)
            val gap = (ref - value).coerceAtLeast(0f)
            val tag = when {
                percent < 70f -> "明显不足"
                percent < 90f -> "偏低"
                percent <= 120f -> "基本达标"
                else -> "偏高"
            }
            return "- $name：平均 ${String.format("%.1f", value)} / 建议 ${String.format("%.1f", ref)}（${String.format("%.0f", percent)}%，$tag，差额 ${String.format("%.1f", gap)}）"
        }

        return buildString {
            appendLine("【最近${safeDays}天营养缺口分析】")
            appendLine(gapLine("protein", "蛋白质(g/日)", avg.protein))
            appendLine(gapLine("fiber", "膳食纤维(g/日)", avg.fiber))
            appendLine(gapLine("calcium", "钙(mg/日)", avg.calcium))
            appendLine(gapLine("iron", "铁(mg/日)", avg.iron))
            appendLine(gapLine("vitamin_c", "维生素C(mg/日)", avg.vitaminC))
            appendLine(gapLine("potassium", "钾(mg/日)", avg.potassium))
            appendLine(gapLine("carbs", "碳水(g/日)", avg.carbs))
            appendLine(gapLine("fat", "脂肪(g/日)", avg.fat))
            appendLine("- 记录条数：${foodRecords.size}，统计区间：${formatDate(startTime)} 至 ${formatDate(endTime)}")
        }
    }

    /**
     * 获取增强版AI指导上下文（用于菜谱/咨询/下一餐推荐）
     */
    suspend fun getAdvancedDietGuidanceContext(action: String, recentDays: Long = 14): String {
        val baseContext = getQuickActionContext(action = action, recentDays = recentDays)
        val constraintContext = getDietaryConstraintContext()
        val gap7 = getNutritionGapContext(7)
        val gap30 = getNutritionGapContext(30)
        return buildString {
            appendLine(baseContext)
            appendLine()
            appendLine(constraintContext)
            appendLine()
            appendLine(gap7)
            appendLine()
            appendLine(gap30)
            appendLine()
            appendLine("请输出时必须包含：")
            appendLine("1) 营养缺口解释（7天与30天）")
            appendLine("2) 可执行补充方案")
            appendLine("3) 食材缺失时的智能替代建议，并重算热量与三大营养素")
            appendLine("4) 若为特定人群模式，请给出该模式下的风险提示与替代策略")
        }
    }
}
