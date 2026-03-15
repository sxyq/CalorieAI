package com.calorieai.app.service.ai

import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.service.ai.common.AIApiClient
import com.calorieai.app.service.ai.common.AIApiException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 食物文本分析服务
 * 根据用户输入的食物描述，使用 AI 分析热量和营养成分
 */
@Singleton
class FoodTextAnalysisService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiConfigRepository: AIConfigRepository
) {
    private val gson = Gson()

    companion object {
        private const val SYSTEM_PROMPT = """你是一个专业的营养师，擅长分析食物的热量和营养成分。请根据用户输入的食物描述，分析并提供详细的营养信息。

【严格要求 - 必须遵守】
1. foodName 字段必须使用中文名称，即使用户输入的是英文
2. 必须严格使用英文标点符号（逗号、引号、冒号）和英文字段名
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

    /**
     * 分析食物文本描述
     * @param foodDescription 食物描述
     * @param maxRetries 最大重试次数（默认2次）
     * @param onRetry 重试回调，用于显示提示
     */
    suspend fun analyzeFoodText(
        foodDescription: String,
        maxRetries: Int = 2,
        onRetry: ((attempt: Int, maxAttempts: Int) -> Unit)? = null
    ): Result<TextFoodAnalysisResult> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                    ?: return@withContext Result.failure(Exception("未配置AI服务"))

                val userPrompt = "请分析以下食物的热量和营养成分：$foodDescription"

                // 使用chatRaw获取响应和原始JSON以提取token使用量
                val (responseText, rawResponse) = aiApiClient.chatRaw(
                    config = config,
                    systemPrompt = SYSTEM_PROMPT,
                    userMessage = userPrompt,
                    temperature = 0.3,
                    maxTokens = 1000
                )

                val result = parseAnalysisResult(responseText)
                
                // 检查解析结果是否有效
                val validationResult = validateNutritionData(result)
                if (validationResult.isValid) {
                    // 提取token使用量
                    val usage = aiApiClient.extractOpenAIUsage(rawResponse)
                    
                    return@withContext Result.success(result.copy(
                        promptTokens = usage?.promptTokens ?: 0,
                        completionTokens = usage?.completionTokens ?: 0
                    ))
                } else {
                    // 解析结果无效，需要重试
                    lastException = Exception(validationResult.errorMessage)
                    if (attempt < maxRetries) {
                        onRetry?.invoke(attempt + 1, maxRetries + 1)
                        // 等待一段时间后重试
                        kotlinx.coroutines.delay(1000L * (attempt + 1))
                    }
                }

            } catch (e: AIApiException) {
                lastException = Exception("AI分析失败: ${e.message}")
                if (attempt < maxRetries) {
                    onRetry?.invoke(attempt + 1, maxRetries + 1)
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    onRetry?.invoke(attempt + 1, maxRetries + 1)
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }
        
        // 所有重试都失败了
        Result.failure(lastException ?: Exception("AI分析失败，请稍后重试"))
    }

    /**
     * 从 AI 返回的文本中提取 JSON 并解析
     * 参考 Deadliner 的 extractJsonFromMarkdown 方法
     */
    private fun parseAnalysisResult(content: String): TextFoodAnalysisResult {
        val jsonString = extractJsonFromText(content)
        
        // 清理 JSON 字符串：替换中文标点为英文标点，替换中文字段名为英文字段名
        var cleanedJson = jsonString
            .replace("，", ",")  // 中文逗号 -> 英文逗号
            .replace("：", ":")  // 中文冒号 -> 英文冒号
            .replace("estimated weight", "estimatedWeight")
        
        // 替换各种中文引号为英文引号（包括全角和半角）
        cleanedJson = cleanedJson
            .replace(""", "\"")
            .replace(""", "\"")
            .replace("'", "\"")
            .replace("'", "\"")
        
        // 替换中文字段名为英文字段名（按长度从长到短，避免部分替换）
        val fieldNameMappings = listOf(
            "estimatedWeight" to listOf("estimated weight", "estimatedWeight", "estimated_weight"),
            "saturatedFat" to listOf("饱和脂肪", "saturatedFat", "saturated_fat"),
            "transFat" to listOf("反式脂肪", "transFat", "trans_fat"),
            "cholesterol" to listOf("胆固醇", "cholesterol"),
            "sodium" to listOf("钠", "sodium"),
            "potassium" to listOf("钾", "potassium"),
            "calcium" to listOf("钙", "calcium"),
            "iron" to listOf("铁", "iron"),
            "zinc" to listOf("锌", "zinc"),
            "magnesium" to listOf("镁", "magnesium"),
            "vitaminA" to listOf("维生素A", "vitaminA", "vitamin A", "vitamin_A"),
            "vitaminC" to listOf("维生素C", "vitaminC", "vitamin C", "vitamin_C"),
            "vitaminD" to listOf("维生素D", "vitaminD", "vitamin D", "vitamin_D"),
            "vitaminE" to listOf("维生素E", "vitaminE", "vitamin E", "vitamin_E"),
            "vitaminB1" to listOf("维生素B1", "vitaminB1", "vitamin B1", "vitamin_B1", "thiamine"),
            "vitaminB2" to listOf("维生素B2", "vitaminB2", "vitamin B2", "vitamin_B2", "riboflavin"),
            "vitaminB6" to listOf("维生素B6", "vitaminB6", "vitamin B6", "vitamin_B6", "pyridoxine"),
            "vitaminB12" to listOf("维生素B12", "vitaminB12", "vitamin B12", "vitamin_B12", "cobalamin"),
            "foodName" to listOf("食物名称", "foodName", "food_name"),
            "estimatedWeight" to listOf("估计重量", "estimatedWeight"),
            "calories" to listOf("热量", "卡路里", "calories"),
            "protein" to listOf("蛋白质", "protein"),
            "carbs" to listOf("碳水化合物", "碳水", "carbs", "carbohydrates"),
            "fat" to listOf("脂肪", "fat"),
            "fiber" to listOf("膳食纤维", "纤维", "fiber"),
            "sugar" to listOf("糖分", "糖", "sugar")
        )
        
        for ((englishName, chineseNames) in fieldNameMappings) {
            for (chineseName in chineseNames) {
                // 替换 "中文字段名": 为 "英文字段名":
                cleanedJson = cleanedJson.replace("\"$chineseName\":", "\"$englishName\":")
                // 替换 '中文字段名': 为 "英文字段名":
                cleanedJson = cleanedJson.replace("'$chineseName':", "\"$englishName\":")
            }
        }
        
        // 修复AI返回的字符串数字（如 "4" -> 4.0）
        // 将 "字段名":"数字" 替换为 "字段名":数字
        val numericFields = listOf(
            "calories", "protein", "carbs", "fat", "fiber", "sugar",
            "saturatedFat", "transFat", "cholesterol", "sodium", "potassium",
            "calcium", "iron", "zinc", "magnesium", "vitaminA", "vitaminC",
            "vitaminD", "vitaminE", "vitaminB1", "vitaminB2", "vitaminB6", "vitaminB12"
        )
        for (field in numericFields) {
            // 匹配 "字段名":"数字" 或 "字段名":'数字'
            val regex = Regex("\"$field\"\\s*:\\s*['\"]([^'\"]+)['\"]")
            cleanedJson = regex.replace(cleanedJson) { matchResult ->
                val value = matchResult.groupValues[1]
                // 尝试转换为数字，如果失败则保留原值
                try {
                    value.toFloat()
                    "\"$field\":$value"
                } catch (e: Exception) {
                    matchResult.value
                }
            }
        }
        
        // 确保JSON以 } 结尾（修复AI可能遗漏的闭合括号）
        if (!cleanedJson.trimEnd().endsWith("}")) {
            cleanedJson = cleanedJson.trimEnd() + "}"
        }

        return try {
            gson.fromJson(cleanedJson, TextFoodAnalysisResult::class.java)
                ?: TextFoodAnalysisResult()
        } catch (e: Exception) {
            e.printStackTrace()
            // 尝试更宽松的解析
            try {
                // 手动提取关键字段
                val foodName = extractField(cleanedJson, "foodName") ?: "未知食物"
                val calories = extractFloatField(cleanedJson, "calories") ?: 0f
                val protein = extractFloatField(cleanedJson, "protein") ?: 0f
                val carbs = extractFloatField(cleanedJson, "carbs") ?: 0f
                val fat = extractFloatField(cleanedJson, "fat") ?: 0f
                
                TextFoodAnalysisResult(
                    foodName = foodName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            } catch (e2: Exception) {
                TextFoodAnalysisResult(
                    foodName = "未知食物",
                    estimatedWeight = 0,
                    calories = 0f,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f
                )
            }
        }
    }
    
    /**
     * 验证营养素数据是否有效
     * 检查关键营养素是否为0
     */
    private fun validateNutritionData(result: TextFoodAnalysisResult): ValidationResult {
        // 检查基本信息
        if (result.foodName.isBlank()) {
            return ValidationResult(false, "食物名称为空")
        }
        if (result.calories <= 0) {
            return ValidationResult(false, "热量数据无效")
        }
        
        // 检查基础营养素（3种关键营养素不能同时为0）
        val basicNutrients = listOf(result.protein, result.carbs, result.fat)
        if (basicNutrients.all { it <= 0 }) {
            return ValidationResult(false, "基础营养素（蛋白质、碳水、脂肪）数据无效，全部为0")
        }
        
        // 检查扩展营养素（如果超过5种为0，认为数据不完整）
        val extendedNutrients = listOf(
            result.fiber, result.sugar, result.saturatedFat, 
            result.cholesterol, result.sodium, result.potassium,
            result.calcium, result.iron, result.vitaminA, result.vitaminC
        )
        val zeroCount = extendedNutrients.count { it <= 0 }
        if (zeroCount >= 8) {
            return ValidationResult(false, "扩展营养素数据不完整，过多字段为0")
        }
        
        return ValidationResult(true, "数据有效")
    }
    
    /**
     * 验证结果数据类
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )
    
    /**
     * 从JSON字符串中提取字符串字段
     */
    private fun extractField(json: String, fieldName: String): String? {
        val regex = Regex("\"$fieldName\"\\s*:\\s*\"([^\"]+)\"")
        return regex.find(json)?.groupValues?.get(1)
    }
    
    /**
     * 从JSON字符串中提取Float字段
     */
    private fun extractFloatField(json: String, fieldName: String): Float? {
        val regex = Regex("\"$fieldName\"\\s*:\\s*([0-9.]+)")
        return regex.find(json)?.groupValues?.get(1)?.toFloatOrNull()
    }

    /**
     * 从可能包含 Markdown 代码块的文本中提取 JSON
     * 参考 Deadliner 的 AIUtils.extractJsonFromMarkdown()
     */
    private fun extractJsonFromText(raw: String): String {
        // 1. 查找第一个 { 并配对闭合的 }
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

        // 2. 尝试从 ```json ... ``` 代码块中提取
        val jsonFenceRegex = Regex("```json\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        jsonFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        val anyFenceRegex = Regex("```\\s*([\\s\\S]*?)```")
        anyFenceRegex.find(raw)?.let { return it.groups[1]!!.value.trim() }

        // 3. 兜底返回原文
        return raw.trim()
    }
}

/**
 * 文本食物分析结果
 */
data class TextFoodAnalysisResult(
    @SerializedName("foodName") val foodName: String = "",
    @SerializedName("estimatedWeight") val estimatedWeight: Int = 0,
    @SerializedName("calories") val calories: Float = 0f,  // 改为Float，因为AI可能返回小数
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("fiber") val fiber: Float = 0f,
    @SerializedName("sugar") val sugar: Float = 0f,
    @SerializedName("sodium") val sodium: Float = 0f,
    @SerializedName("cholesterol") val cholesterol: Float = 0f,
    @SerializedName("saturatedFat") val saturatedFat: Float = 0f,
    @SerializedName("calcium") val calcium: Float = 0f,
    @SerializedName("iron") val iron: Float = 0f,
    @SerializedName("vitaminC") val vitaminC: Float = 0f,
    @SerializedName("vitaminA") val vitaminA: Float = 0f,
    @SerializedName("potassium") val potassium: Float = 0f,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0
)