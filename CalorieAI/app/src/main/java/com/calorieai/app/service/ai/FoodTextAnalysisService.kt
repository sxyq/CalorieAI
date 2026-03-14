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

重要规则：
1. foodName 字段必须使用中文名称，即使用户输入的是英文
2. 必须严格使用英文标点符号（逗号、引号、冒号）和英文字段名

请以JSON格式返回结果，格式如下：
{"foodName":"食物中文名称","estimatedWeight":200,"calories":300,"protein":15.5,"carbs":25.0,"fat":12.0,"fiber":2.0,"sugar":5.0,"saturatedFat":2.0,"transFat":0.0,"cholesterol":30.0,"sodium":200.0,"potassium":150.0,"calcium":50.0,"iron":2.0,"zinc":1.0,"magnesium":30.0,"vitaminA":100.0,"vitaminC":10.0,"vitaminD":2.0,"vitaminE":3.0,"vitaminB1":0.5,"vitaminB2":0.6,"vitaminB6":0.8,"vitaminB12":1.0}

注意：
1. foodName 必须是中文，例如："汉堡王 皇堡"、"麦当劳 巨无霸"、"肯德基 炸鸡"
2. 如果用户没有提供具体重量，请根据常见份量估算
3. 营养成分请根据标准食物成分表计算
4. 如果某些营养素无法准确估算，可以填0
5. 只返回JSON，不要包含其他说明文字
6. 必须使用英文逗号、英文引号、英文冒号"""
    }

    /**
     * 分析食物文本描述
     */
    suspend fun analyzeFoodText(foodDescription: String): Result<TextFoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val config = aiConfigRepository.getDefaultConfig().firstOrNull()
                ?: return@withContext Result.failure(Exception("未配置AI服务"))

            val userPrompt = "请分析以下食物的热量和营养成分：$foodDescription"

            val responseText = aiApiClient.chat(
                config = config,
                systemPrompt = SYSTEM_PROMPT,
                userMessage = userPrompt,
                temperature = 0.3,
                maxTokens = 1000
            )

            val result = parseAnalysisResult(responseText)
            Result.success(result)

        } catch (e: AIApiException) {
            Result.failure(Exception("AI分析失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 AI 返回的文本中提取 JSON 并解析
     * 参考 Deadliner 的 extractJsonFromMarkdown 方法
     */
    private fun parseAnalysisResult(content: String): TextFoodAnalysisResult {
        val jsonString = extractJsonFromText(content)
        
        // 清理 JSON 字符串：替换中文标点为英文标点
        val cleanedJson = jsonString
            .replace("，", ",")  // 中文逗号 -> 英文逗号
            .replace(""", "\"")  // 中文左引号 -> 英文引号
            .replace(""", "\"")  // 中文右引号 -> 英文引号
            .replace("：", ":")  // 中文冒号 -> 英文冒号
            .replace("estimated weight", "estimatedWeight")  // 修正字段名
            .replace("饱和脂肪", "saturatedFat")
            .replace("反式脂肪", "transFat")
            .replace("胆固醇", "cholesterol")
            .replace("钠", "sodium")
            .replace("钾", "potassium")
            .replace("钙", "calcium")
            .replace("铁", "iron")
            .replace("锌", "zinc")
            .replace("镁", "magnesium")
            .replace("维生素A", "vitaminA")
            .replace("维生素C", "vitaminC")
            .replace("维生素D", "vitaminD")
            .replace("维生素E", "vitaminE")
            .replace("维生素B1", "vitaminB1")
            .replace("维生素B2", "vitaminB2")
            .replace("维生素B6", "vitaminB6")
            .replace("维生素B12", "vitaminB12")
            .replace("vitamin B2", "vitaminB2")
            .replace("vitamin B6", "vitaminB6")

        return try {
            gson.fromJson(cleanedJson, TextFoodAnalysisResult::class.java)
                ?: TextFoodAnalysisResult()
        } catch (e: Exception) {
            e.printStackTrace()
            TextFoodAnalysisResult(
                foodName = "未知食物",
                estimatedWeight = 0,
                calories = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f
            )
        }
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
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)