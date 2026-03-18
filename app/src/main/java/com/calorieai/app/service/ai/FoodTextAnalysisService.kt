package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodTextAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository
) {
    private val gson = Gson()

    companion object {
        private const val CONCURRENT_CALLS = 5
        
        private const val SYSTEM_PROMPT = """你是一个专业的营养师，擅长分析食物的热量和营养成分。请根据用户输入的食物描述，分析并提供详细的营养信息。

【严格要求 - 必须遵守】
1. foodName 字段必须使用中文名称
2. 必须严格使用英文标点符号（逗号、引号、冒号）
3. 所有数值必须是纯数字，不能带引号，不能使用字符串
4. 所有营养素字段必须返回，不能省略
5. 如果无法准确估算，使用合理的估计值（不能全部填0）

【13种必需营养素字段】
- 基础营养素（3种）：protein, carbs, fat
- 扩展营养素（10种）：fiber, sugar, saturatedFat, cholesterol, sodium, potassium, calcium, iron, vitaminA, vitaminC

【JSON格式示例】
{"foodName":"番茄炒蛋","estimatedWeight":200,"calories":185,"protein":13.4,"carbs":7.9,"fat":12.0,"fiber":2.5,"sugar":3.0,"saturatedFat":4.0,"cholesterol":160.8,"sodium":257.3,"potassium":492.3,"calcium":15.0,"iron":1.0,"vitaminA":3975.0,"vitaminC":12.3}

【格式检查清单】
✓ 所有字段名使用英文
✓ 所有数值不带引号（如：13.4 而不是 "13.4"）
✓ 使用英文逗号分隔
✓ 使用英文引号包裹字符串值
✓ 使用英文冒号分隔键值
✓ 返回完整的13种营养素数据

【禁止事项】
✗ 不要使用中文字段名
✗ 不要使用中文标点符号
✗ 不要将数字用引号包裹
✗ 不要省略任何营养素字段
✗ 不要返回说明文字，只返回JSON"""
    }

    suspend fun analyzeFoodText(
        foodDescription: String,
        maxRetries: Int = 2,
        onRetry: ((attempt: Int, maxAttempts: Int) -> Unit)? = null
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))

            val userPrompt = "请分析以下食物的热量和营养成分：$foodDescription"

            // 并发调用5次
            val results = mutableListOf<FoodAnalysisResult>()
            val errors = mutableListOf<String>()
            
            val deferredResults = (1..CONCURRENT_CALLS).map { 
                async {
                    try {
                        val responseText = aiApiClient.chat(
                            config = config,
                            systemPrompt = SYSTEM_PROMPT,
                            userMessage = userPrompt,
                            temperature = 0.3,
                            maxTokens = 1000
                        )
                        val result = parseAnalysisResult(responseText)
                        val validation = validateNutritionData(result)
                        if (validation.isValid) {
                            Result.success(result)
                        } else {
                            Result.failure<FoodAnalysisResult>(Exception(validation.errorMessage))
                        }
                    } catch (e: Exception) {
                        Result.failure<FoodAnalysisResult>(e)
                    }
                }
            }
            
            deferredResults.awaitAll().forEach { result ->
                result.fold(
                    onSuccess = { results.add(it) },
                    onFailure = { errors.add(it.message ?: "未知错误") }
                )
            }

            if (results.isEmpty()) {
                return@withContext Result.failure(Exception("所有分析尝试失败: ${errors.take(3).joinToString("; ")}"))
            }

            // 计算平均值
            val averagedResult = calculateAverageResult(results)
            Result.success(averagedResult)

        } catch (e: AIApiException) {
            Result.failure(Exception("AI分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 计算多次分析结果的平均值
     */
    private fun calculateAverageResult(results: List<FoodAnalysisResult>): FoodAnalysisResult {
        if (results.isEmpty()) return FoodAnalysisResult(foodName = "未知食物")
        if (results.size == 1) return results.first()
        
        // 选择出现最多的食物名称
        val foodNames = results.map { it.foodName }.groupingBy { it }.eachCount()
        val mostCommonName = foodNames.maxByOrNull { it.value }?.key ?: "未知食物"
        
        // 计算数值字段的平均值
        fun averageOf(selector: (FoodAnalysisResult) -> Float): Float {
            val values = results.map(selector)
            return values.sum() / values.size
        }
        
        return FoodAnalysisResult(
            foodName = mostCommonName,
            estimatedWeight = (results.map { it.estimatedWeight }.sum() / results.size),
            calories = averageOf { it.calories },
            protein = averageOf { it.protein },
            carbs = averageOf { it.carbs },
            fat = averageOf { it.fat },
            fiber = averageOf { it.fiber },
            sugar = averageOf { it.sugar },
            saturatedFat = averageOf { it.saturatedFat },
            cholesterol = averageOf { it.cholesterol },
            sodium = averageOf { it.sodium },
            potassium = averageOf { it.potassium },
            calcium = averageOf { it.calcium },
            iron = averageOf { it.iron },
            vitaminA = averageOf { it.vitaminA },
            vitaminC = averageOf { it.vitaminC }
        )
    }

    private fun parseAnalysisResult(content: String): FoodAnalysisResult {
        val jsonString = extractJsonFromText(content)
        
        var cleanedJson = jsonString
            .replace("，", ",")
            .replace("：", ":")
            .replace("estimated weight", "estimatedWeight")
        
        cleanedJson = cleanedJson
            .replace(""", "\"")
            .replace(""", "\"")
            .replace("'", "\"")
            .replace("'", "\"")
        
        val fieldNameMappings = mapOf(
            "estimated weight" to "estimatedWeight",
            "食物名称" to "foodName",
            "estimatedWeight" to "estimatedWeight",
            "热量" to "calories",
            "卡路里" to "calories",
            "蛋白质" to "protein",
            "碳水" to "carbs",
            "碳水化合物" to "carbs",
            "脂肪" to "fat",
            "膳食纤维" to "fiber",
            "纤维" to "fiber",
            "糖分" to "sugar",
            "糖" to "sugar",
            "饱和脂肪" to "saturatedFat",
            "胆固醇" to "cholesterol",
            "钠" to "sodium",
            "钾" to "potassium",
            "钙" to "calcium",
            "铁" to "iron",
            "维生素A" to "vitaminA",
            "维生素C" to "vitaminC"
        )
        
        for ((chineseName, englishName) in fieldNameMappings) {
            cleanedJson = cleanedJson.replace("\"$chineseName\":", "\"$englishName\":")
        }
        
        val numericFields = listOf(
            "calories", "protein", "carbs", "fat", "fiber", "sugar",
            "saturatedFat", "cholesterol", "sodium", "potassium",
            "calcium", "iron", "vitaminA", "vitaminC"
        )
        
        for (field in numericFields) {
            val regex = Regex("\"$field\"\\s*:\\s*['\"]([^'\"]+)['\"]")
            cleanedJson = regex.replace(cleanedJson) { matchResult ->
                val value = matchResult.groupValues[1]
                try {
                    value.toFloat()
                    "\"$field\":$value"
                } catch (e: Exception) {
                    matchResult.value
                }
            }
        }
        
        if (!cleanedJson.trimEnd().endsWith("}")) {
            cleanedJson = cleanedJson.trimEnd() + "}"
        }

        return try {
            gson.fromJson(cleanedJson, FoodAnalysisResult::class.java)
                ?: FoodAnalysisResult(foodName = "未知食物")
        } catch (e: Exception) {
            FoodAnalysisResult(foodName = "未知食物")
        }
    }
    
    private fun validateNutritionData(result: FoodAnalysisResult): ValidationResult {
        if (result.foodName.isBlank()) {
            return ValidationResult(false, "食物名称为空")
        }
        if (result.calories <= 0) {
            return ValidationResult(false, "热量数据无效")
        }
        
        val basicNutrients = listOf(result.protein, result.carbs, result.fat)
        if (basicNutrients.all { it <= 0 }) {
            return ValidationResult(false, "基础营养素数据无效")
        }
        
        return ValidationResult(true, "数据有效")
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )

    private fun extractJsonFromText(raw: String): String {
        val idx = raw.indexOf('{')
        if (idx >= 0) {
            var depth = 0
            for (i in idx until raw.length) {
                when (raw[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return raw.substring(idx, i + 1).trim()
                    }
                }
            }
        }

        val jsonFenceRegex = Regex("```json\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        jsonFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        val anyFenceRegex = Regex("```\\s*([\\s\\S]*?)```")
        anyFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        return raw.trim()
    }
}
