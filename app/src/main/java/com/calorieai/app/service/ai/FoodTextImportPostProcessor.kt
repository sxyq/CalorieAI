package com.calorieai.app.service.ai

import com.calorieai.app.data.model.FoodAnalysisResult
import com.calorieai.app.service.ai.common.AIResponseSanitizer
import com.google.gson.Gson

internal object FoodTextImportPostProcessor {
    private val gson = Gson()
    private val reservedFieldNames = setOf(
        "items",
        "item",
        "foodname",
        "estimatedweight",
        "calories",
        "protein",
        "carbs",
        "fat",
        "fiber",
        "sugar",
        "saturatedfat",
        "cholesterol",
        "sodium",
        "potassium",
        "calcium",
        "iron",
        "vitamina",
        "vitaminc",
        "description"
    )

    private val explicitQuantityRegex = Regex(
        pattern = "(?i)(\\d+(?:\\.\\d+)?)\\s*(g|kg|ml|l|克|千克|毫升|升)\\s*([^\\d,，、+\\n]+?)(?=(?:\\s*\\d+(?:\\.\\d+)?\\s*(?:g|kg|ml|l|克|千克|毫升|升))|$)"
    )

    fun process(responseText: String, foodDescription: String): List<FoodAnalysisResult> {
        val inputHints = extractInputHints(foodDescription)
        val parsedItems = parseBatchAnalysisResult(responseText)

        val candidates = when {
            parsedItems.isNotEmpty() -> {
                val intentAlignedItems = alignItemsWithUserIntent(parsedItems, foodDescription)
                val hydratedItems = backfillFromInputHints(intentAlignedItems, inputHints)
                val completedItems = appendMissingInputHints(hydratedItems, inputHints, foodDescription)
                orderItemsByInputHints(completedItems, inputHints, foodDescription)
            }
            inputHints.isNotEmpty() -> {
                inputHints.map { hint ->
                    FoodAnalysisResult(
                        foodName = hint.foodName,
                        estimatedWeight = hint.estimatedWeight
                    )
                }
            }
            else -> emptyList()
        }

        val normalizedCandidates = candidates.mapIndexed { index, item ->
            normalizeNutritionData(
                result = item,
                foodDescription = foodDescription,
                index = index,
                inputHint = inputHints.getOrNull(index)
            )
        }
        val importableItems = normalizedCandidates.filter(::isImportableItem)
        if (importableItems.isNotEmpty()) {
            return importableItems
        }
        return buildSingleItemFallback(foodDescription, normalizedCandidates)
    }

    private fun parseBatchAnalysisResult(content: String): List<FoodAnalysisResult> {
        val sanitizedItems = AIResponseSanitizer.parseFoodItems(content, gson)
        if (sanitizedItems.isNotEmpty()) return sanitizedItems
        return AIResponseSanitizer.parseSingleFoodItem(content, gson)?.let { listOf(it) }.orEmpty()
    }

    private fun alignItemsWithUserIntent(
        items: List<FoodAnalysisResult>,
        foodDescription: String
    ): List<FoodAnalysisResult> {
        if (items.isEmpty()) return items
        if (shouldSplitByExplicitQuantity(foodDescription)) {
            return items
        }
        pickBestSingleItem(items, foodDescription)?.let { return listOf(it) }
        return listOf(mergeToSingleItem(items, foodDescription))
    }

    private fun shouldSplitByExplicitQuantity(input: String): Boolean {
        val normalized = input.lowercase()
        val quantityRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(g|克|kg|千克|ml|毫升|l|升|个|只|份|片|块|勺|杯|碗)")
        val quantityCount = quantityRegex.findAll(normalized).count()
        if (quantityCount >= 2) return true

        val hasSplitter = listOf("，", ",", "、", "和", "及", "+", " plus ").any { normalized.contains(it) }
        val numberCount = Regex("\\d+(?:\\.\\d+)?").findAll(normalized).count()
        return hasSplitter && numberCount >= 2
    }

    private fun mergeToSingleItem(
        items: List<FoodAnalysisResult>,
        foodDescription: String
    ): FoodAnalysisResult {
        if (items.size == 1) return items.first()
        val name = deriveMergedName(foodDescription, items)
        val weight = items.sumOf { it.estimatedWeight.toDouble() }.toInt().coerceAtLeast(1)
        return FoodAnalysisResult(
            foodName = name,
            estimatedWeight = weight,
            calories = items.sumOf { it.calories.toDouble() }.toFloat(),
            protein = items.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = items.sumOf { it.carbs.toDouble() }.toFloat(),
            fat = items.sumOf { it.fat.toDouble() }.toFloat(),
            fiber = items.sumOf { it.fiber.toDouble() }.toFloat(),
            sugar = items.sumOf { it.sugar.toDouble() }.toFloat(),
            saturatedFat = items.sumOf { it.saturatedFat.toDouble() }.toFloat(),
            cholesterol = items.sumOf { it.cholesterol.toDouble() }.toFloat(),
            sodium = items.sumOf { it.sodium.toDouble() }.toFloat(),
            potassium = items.sumOf { it.potassium.toDouble() }.toFloat(),
            calcium = items.sumOf { it.calcium.toDouble() }.toFloat(),
            iron = items.sumOf { it.iron.toDouble() }.toFloat(),
            vitaminA = items.sumOf { it.vitaminA.toDouble() }.toFloat(),
            vitaminC = items.sumOf { it.vitaminC.toDouble() }.toFloat()
        )
    }

    private fun deriveMergedName(
        foodDescription: String,
        items: List<FoodAnalysisResult>
    ): String {
        val cleaned = foodDescription.trim()
        if (cleaned.isNotEmpty()) return cleaned.take(30)
        return items.maxByOrNull { it.calories }?.foodName ?: "混合食物"
    }

    private fun pickBestSingleItem(
        items: List<FoodAnalysisResult>,
        foodDescription: String
    ): FoodAnalysisResult? {
        if (items.size <= 1) return items.firstOrNull()

        val cleanedInput = cleanFoodName(foodDescription)
        if (cleanedInput.isBlank()) return null

        val cleanedNames = items.map { cleanFoodName(it.foodName) }.filter { it.isNotBlank() }
        if (cleanedNames.isNotEmpty() && cleanedNames.distinct().size == 1) {
            return items.maxWithOrNull(singleItemComparator())?.copy(foodName = cleanedInput)
        }

        return items
            .filter {
                val cleanedName = cleanFoodName(it.foodName)
                cleanedName.equals(cleanedInput, ignoreCase = true) ||
                    cleanedName.contains(cleanedInput, ignoreCase = true)
            }
            .maxWithOrNull(singleItemComparator())
            ?.copy(foodName = cleanedInput)
    }

    private fun singleItemScore(item: FoodAnalysisResult): Int {
        var score = 0
        if (item.estimatedWeight > 0) score += 2
        if (item.calories > 0f) score += 2
        if (item.protein > 0f) score += 1
        if (item.carbs > 0f) score += 1
        if (item.fat > 0f) score += 1
        if (item.fiber > 0f) score += 1
        if (item.sugar > 0f) score += 1
        if (item.saturatedFat > 0f) score += 1
        if (item.cholesterol > 0f) score += 1
        if (item.sodium > 0f) score += 1
        if (item.potassium > 0f) score += 1
        if (item.calcium > 0f) score += 1
        if (item.iron > 0f) score += 1
        if (item.vitaminA > 0f) score += 1
        if (item.vitaminC > 0f) score += 1
        return score
    }

    private fun singleItemComparator(): Comparator<FoodAnalysisResult> {
        return compareBy<FoodAnalysisResult> { singleItemScore(it) }
            .thenBy { it.calories }
            .thenBy { it.estimatedWeight }
    }

    private fun backfillFromInputHints(
        items: List<FoodAnalysisResult>,
        inputHints: List<InputFoodHint>
    ): List<FoodAnalysisResult> {
        if (items.isEmpty() || inputHints.isEmpty()) return items

        return items.mapIndexed { index, item ->
            val hint = inputHints.getOrNull(index)
            val matchedHint = when {
                !isInvalidFoodName(item.foodName) -> hint
                item.estimatedWeight > 0 -> inputHints.firstOrNull { it.estimatedWeight == item.estimatedWeight } ?: hint
                else -> hint
            }
            if (hint == null) {
                item
            } else {
                item.copy(
                    foodName = cleanFoodName(item.foodName).takeUnless(::isInvalidFoodName) ?: matchedHint?.foodName ?: hint.foodName,
                    estimatedWeight = item.estimatedWeight.takeIf { it > 0 } ?: matchedHint?.estimatedWeight ?: hint.estimatedWeight
                )
            }
        }
    }

    private fun appendMissingInputHints(
        items: List<FoodAnalysisResult>,
        inputHints: List<InputFoodHint>,
        foodDescription: String
    ): List<FoodAnalysisResult> {
        if (items.isEmpty() || inputHints.isEmpty()) return items
        if (!shouldSplitByExplicitQuantity(foodDescription)) return items
        if (items.size >= inputHints.size) return items

        val usedNames = items.map { cleanFoodName(it.foodName) }.filter { it.isNotBlank() }.toSet()
        val missingHints = inputHints.filterNot { cleanFoodName(it.foodName) in usedNames }
        return items + missingHints.map { hint ->
            FoodAnalysisResult(
                foodName = hint.foodName,
                estimatedWeight = hint.estimatedWeight
            )
        }
    }

    private fun orderItemsByInputHints(
        items: List<FoodAnalysisResult>,
        inputHints: List<InputFoodHint>,
        foodDescription: String
    ): List<FoodAnalysisResult> {
        if (items.isEmpty() || inputHints.isEmpty()) return items
        if (!shouldSplitByExplicitQuantity(foodDescription)) return items

        val remaining = items.toMutableList()
        val ordered = mutableListOf<FoodAnalysisResult>()
        inputHints.forEach { hint ->
            val matchIndex = remaining.indexOfFirst {
                cleanFoodName(it.foodName).equals(cleanFoodName(hint.foodName), ignoreCase = true)
            }
            if (matchIndex >= 0) {
                ordered += remaining.removeAt(matchIndex)
            } else {
                ordered += FoodAnalysisResult(
                    foodName = hint.foodName,
                    estimatedWeight = hint.estimatedWeight
                )
            }
        }
        return ordered + remaining
    }

    private fun normalizeNutritionData(
        result: FoodAnalysisResult,
        foodDescription: String,
        index: Int,
        inputHint: InputFoodHint?
    ): FoodAnalysisResult {
        val normalizedFoodName = cleanFoodName(result.foodName).takeUnless(::isInvalidFoodName).orEmpty().ifBlank {
            inputHint?.foodName ?: deriveFallbackName(foodDescription, index)
        }

        val estimatedWeight = when {
            result.estimatedWeight > 0 -> result.estimatedWeight
            inputHint?.estimatedWeight != null && inputHint.estimatedWeight > 0 -> inputHint.estimatedWeight
            else -> 0
        }

        val protein = sanitizeNumeric(result.protein)
        val carbs = sanitizeNumeric(result.carbs)
        val fat = sanitizeNumeric(result.fat)
        var calories = sanitizeNumeric(result.calories)

        if (calories <= 0f) {
            val estimatedCalories = protein * 4f + carbs * 4f + fat * 9f
            if (estimatedCalories > 0f) {
                calories = estimatedCalories
            }
        }

        return result.copy(
            foodName = normalizedFoodName.take(30),
            estimatedWeight = estimatedWeight.coerceAtLeast(0),
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = sanitizeNumeric(result.fiber),
            sugar = sanitizeNumeric(result.sugar),
            saturatedFat = sanitizeNumeric(result.saturatedFat),
            cholesterol = sanitizeNumeric(result.cholesterol),
            sodium = sanitizeNumeric(result.sodium),
            potassium = sanitizeNumeric(result.potassium),
            calcium = sanitizeNumeric(result.calcium),
            iron = sanitizeNumeric(result.iron),
            vitaminA = sanitizeNumeric(result.vitaminA),
            vitaminC = sanitizeNumeric(result.vitaminC)
        )
    }

    private fun deriveFallbackName(foodDescription: String, index: Int): String {
        val cleaned = foodDescription.trim().take(30)
        if (cleaned.isNotBlank()) return cleaned
        return "未命名食物${index + 1}"
    }

    private fun sanitizeNumeric(value: Float): Float {
        if (value.isNaN() || value.isInfinite()) return 0f
        return value.coerceAtLeast(0f)
    }

    private fun isImportableItem(result: FoodAnalysisResult): Boolean {
        if (isInvalidFoodName(result.foodName)) {
            return false
        }

        val hasWeight = result.estimatedWeight > 0
        val hasCalories = result.calories > 0f
        val hasMacroNutrients = result.protein > 0f || result.carbs > 0f || result.fat > 0f
        return hasWeight || hasCalories || hasMacroNutrients
    }

    private fun buildSingleItemFallback(
        foodDescription: String,
        candidates: List<FoodAnalysisResult>
    ): List<FoodAnalysisResult> {
        if (shouldSplitByExplicitQuantity(foodDescription)) return emptyList()

        val fallbackName = foodDescription.trim().take(30)
        if (fallbackName.isBlank()) return emptyList()

        val seed = candidates.firstOrNull()
        return listOf(
            FoodAnalysisResult(
                foodName = seed?.foodName?.takeUnless(::isInvalidFoodName) ?: fallbackName,
                estimatedWeight = seed?.estimatedWeight?.coerceAtLeast(0) ?: 0,
                calories = seed?.calories ?: 0f,
                protein = seed?.protein ?: 0f,
                carbs = seed?.carbs ?: 0f,
                fat = seed?.fat ?: 0f,
                fiber = seed?.fiber ?: 0f,
                sugar = seed?.sugar ?: 0f,
                saturatedFat = seed?.saturatedFat ?: 0f,
                cholesterol = seed?.cholesterol ?: 0f,
                sodium = seed?.sodium ?: 0f,
                potassium = seed?.potassium ?: 0f,
                calcium = seed?.calcium ?: 0f,
                iron = seed?.iron ?: 0f,
                vitaminA = seed?.vitaminA ?: 0f,
                vitaminC = seed?.vitaminC ?: 0f
            )
        )
    }

    private fun extractInputHints(foodDescription: String): List<InputFoodHint> {
        val normalizedInput = foodDescription
            .replace(Regex("[,，、+＋]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return explicitQuantityRegex.findAll(normalizedInput).mapNotNull { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return@mapNotNull null
            val unit = match.groupValues[2].lowercase()
            val foodName = match.groupValues[3].trim().trim(',', '，', '、')
            if (foodName.isBlank()) return@mapNotNull null

            val estimatedWeight = when (unit) {
                "kg", "千克" -> (amount * 1000).toInt()
                "l", "升" -> (amount * 1000).toInt()
                else -> amount.toInt()
            }.coerceAtLeast(0)

            InputFoodHint(
                foodName = foodName.take(30),
                estimatedWeight = estimatedWeight
            )
        }.toList()
    }

    private fun isQuantityLikeName(name: String): Boolean {
        val trimmed = cleanFoodName(name)
        if (trimmed.isBlank()) return false
        return Regex(
            "^\\d+(?:\\.\\d+)?\\s*(g|kg|ml|l|克|千克|毫升|升|个|只|份|片|块|勺|杯|碗)?$",
            RegexOption.IGNORE_CASE
        ).matches(trimmed)
    }

    private fun isInvalidFoodName(name: String): Boolean {
        val trimmed = cleanFoodName(name)
        if (trimmed.isBlank()) return true
        if (isQuantityLikeName(trimmed)) return true
        val normalized = trimmed.lowercase().replace(Regex("[^a-z]"), "")
        return normalized in reservedFieldNames
    }

    private fun cleanFoodName(name: String): String {
        return name.trim().trim(',', '，', '、', '+', '＋', '"', '\'')
    }

    private data class InputFoodHint(
        val foodName: String,
        val estimatedWeight: Int
    )
}
