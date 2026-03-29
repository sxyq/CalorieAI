package com.calorieai.app.domain.recipe

import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.repository.RecipePlanRepository
import com.calorieai.app.service.ai.AIChatService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class RecipePersonalization(
    val dietaryAllergens: String,
    val flavorPreferences: String,
    val budgetPreference: String,
    val maxCookingMinutes: String,
    val specialPopulationMode: String,
    val weeklyRecordGoalDays: String
)

@Singleton
class MealPlanUseCase @Inject constructor(
    private val recipePlanRepository: RecipePlanRepository,
    private val aiChatService: AIChatService
) {
    fun observePlans(): Flow<List<RecipePlan>> = recipePlanRepository.getAll()

    suspend fun savePlan(
        title: String,
        startDate: LocalDate,
        days: Int,
        menuText: String,
        generatedByAI: Boolean
    ): Result<Unit> {
        if (title.isBlank() || menuText.isBlank()) {
            return Result.failure(IllegalArgumentException("菜单标题和内容不能为空"))
        }

        return runCatching {
            val safeDays = days.coerceAtLeast(1)
            val endDate = startDate.plusDays((safeDays - 1).toLong())
            val now = System.currentTimeMillis()
            recipePlanRepository.upsert(
                RecipePlan(
                    title = title.trim(),
                    startDateEpochDay = startDate.toEpochDay(),
                    endDateEpochDay = endDate.toEpochDay(),
                    menuText = menuText.trim(),
                    generatedByAI = generatedByAI,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun removePlan(item: RecipePlan) {
        recipePlanRepository.delete(item)
    }

    suspend fun generateRecipeSuggestion(
        pantrySummary: String,
        personalization: RecipePersonalization
    ): Result<String> {
        return runCatching {
            aiChatService.recommendRecipesWithPantry(
                buildRecipeRequestContext(
                    pantrySummary = pantrySummary,
                    personalization = personalization
                )
            )
        }
    }

    suspend fun generateAndSavePlan(
        pantrySummary: String,
        personalization: RecipePersonalization,
        days: Int,
        startDate: LocalDate
    ): Result<String> {
        return runCatching {
            val safeDays = days.coerceIn(1, 14)
            val response = aiChatService.generatePantryMealPlan(
                pantrySummary = buildRecipeRequestContext(
                    pantrySummary = pantrySummary,
                    personalization = personalization
                ),
                days = safeDays
            )
            savePlan(
                title = "AI生成${safeDays}天菜单",
                startDate = startDate,
                days = safeDays,
                menuText = response,
                generatedByAI = true
            ).getOrThrow()
            response
        }
    }

    fun buildPantrySummary(lines: List<String>): String {
        if (lines.isEmpty()) {
            return "暂无本地食材库存。请直接基于近期健康数据与饮食偏好推荐菜谱，并附可选采购清单。"
        }
        return lines.joinToString(separator = "\n")
    }

    private fun buildRecipeRequestContext(
        pantrySummary: String,
        personalization: RecipePersonalization
    ): String {
        val modeLabel = when (personalization.specialPopulationMode) {
            "DIABETES" -> "控糖"
            "GOUT" -> "痛风"
            "PREGNANCY" -> "孕期"
            "CHILD" -> "儿童"
            "FITNESS" -> "健身"
            else -> "通用健康"
        }

        return """
【本地食材信息】
$pantrySummary

【已保存的饮食偏好】
- 过敏原/忌口：${personalization.dietaryAllergens.ifBlank { "未填写" }}
- 口味偏好：${personalization.flavorPreferences.ifBlank { "未填写" }}
- 预算偏好：${personalization.budgetPreference.ifBlank { "未填写" }}
- 烹饪时长上限：${personalization.maxCookingMinutes.ifBlank { "未填写" }} 分钟
- 特定人群模式：$modeLabel
- 每周记录目标：${personalization.weeklyRecordGoalDays.ifBlank { "5" }} 天
        """.trimIndent()
    }
}

