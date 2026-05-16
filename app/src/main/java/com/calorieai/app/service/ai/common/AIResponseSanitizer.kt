package com.calorieai.app.service.ai.common

import com.calorieai.app.data.model.FoodAnalysisResult
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.ArrayDeque

/**
 * 统一处理 AI 非标准 JSON 输出，尽量在服务层兜底，提升解析成功率。
 */
object AIResponseSanitizer {
    private val wrapperKeys = listOf(
        "items", "foods", "foodList", "data", "result", "results", "payload", "response"
    )

    private val fieldAliases = linkedMapOf(
        "chinese name food" to "foodName",
        "food_name" to "foodName",
        "food name" to "foodName",
        "name" to "foodName",
        "食物名称" to "foodName",
        "名称" to "foodName",
        "estimated_weight" to "estimatedWeight",
        "estimated weight" to "estimatedWeight",
        "weight" to "estimatedWeight",
        "重量" to "estimatedWeight",
        "估计重量" to "estimatedWeight",
        "热量" to "calories",
        "卡路里" to "calories",
        "蛋白质" to "protein",
        "碳水" to "carbs",
        "碳水化合物" to "carbs",
        "carbohydrate" to "carbs",
        "脂肪" to "fat",
        "膳食纤维" to "fiber",
        "纤维" to "fiber",
        "糖分" to "sugar",
        "糖" to "sugar",
        "saturated_fat" to "saturatedFat",
        "饱和脂肪" to "saturatedFat",
        "胆固醇" to "cholesterol",
        "钠" to "sodium",
        "钾" to "potassium",
        "钙" to "calcium",
        "铁" to "iron",
        "维生素a" to "vitaminA",
        "维生素c" to "vitaminC",
        "描述" to "description"
    )

    private val numericFields = listOf(
        "estimatedWeight",
        "calories",
        "protein",
        "carbs",
        "fat",
        "fiber",
        "sugar",
        "saturatedFat",
        "cholesterol",
        "sodium",
        "potassium",
        "calcium",
        "iron",
        "vitaminA",
        "vitaminC"
    )

    private val foodNameStartAliases = listOf(
        "foodName", "food_name", "Chinese name food", "食物名称", "名称"
    )

    fun parseFoodItems(raw: String, gson: Gson): List<FoodAnalysisResult> {
        val root = parseJsonElement(raw)
        if (root != null) {
            val candidates = collectFoodObjects(root)
            if (candidates.isNotEmpty()) {
                val parsed = candidates.mapNotNull { parseFoodObject(it, gson) }
                if (parsed.isNotEmpty()) return parsed
            }
        }

        return parseFoodItemsByPattern(raw)
    }

    fun parseSingleFoodItem(raw: String, gson: Gson): FoodAnalysisResult? {
        return parseFoodItems(raw, gson).firstOrNull()
    }

    fun normalizeNumericLiteral(raw: String): String? {
        val normalized = toHalfWidth(raw)
            .lowercase()
            .replace("，", "")
            .replace(",", "")
            .replace(" ", "")
            .replace("千卡", "")
            .replace("大卡", "")
            .replace("卡路里", "")
            .replace("kcal", "")
            .replace("千焦", "")
            .replace("kj", "")
            .replace("毫克", "")
            .replace("mg", "")
            .replace("微克", "")
            .replace("μg", "")
            .replace("ug", "")
            .replace("克", "")
            .replace("g", "")
            .replace("毫升", "")
            .replace("ml", "")
            .replace("国际单位", "")
            .replace("iu", "")

        if (normalized in setOf("zero", "none", "nil", "null", "无", "没有")) {
            return "0"
        }

        val number = Regex("-?\\d+(?:\\.\\d+)?").find(normalized)?.value ?: return null
        val parsed = number.toFloatOrNull()?.coerceAtLeast(0f) ?: return null
        return parsed.toString()
    }

    private fun parseJsonElement(raw: String): JsonElement? {
        val candidates = buildCandidates(raw)
        candidates.forEach { candidate ->
            val sanitized = sanitizeJson(candidate)
            val parsed = runCatching { JsonParser.parseString(sanitized) }.getOrNull()
            if (parsed != null) {
                return parsed
            }
            val balanced = closeJsonStructures(sanitized)
            if (balanced != sanitized) {
                val reparsed = runCatching { JsonParser.parseString(balanced) }.getOrNull()
                if (reparsed != null) {
                    return reparsed
                }
            }
        }
        return null
    }

    private fun buildCandidates(raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return emptyList()

        val candidates = mutableListOf<String>()

        Regex("```json\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
            .findAll(raw)
            .forEach { match -> candidates += match.groupValues[1].trim() }

        Regex("```\\s*([\\s\\S]*?)```")
            .findAll(raw)
            .forEach { match -> candidates += match.groupValues[1].trim() }

        extractBalancedJson(raw)?.let { candidates += it }

        val firstStructured = listOf(raw.indexOf('{'), raw.indexOf('['))
            .filter { it >= 0 }
            .minOrNull()
        if (firstStructured != null) {
            candidates += raw.substring(firstStructured).trim()
        }

        candidates += trimmed
        return candidates.filter { it.isNotBlank() }.distinct()
    }

    private fun extractBalancedJson(raw: String): String? {
        var start = -1
        var inString = false
        var escaped = false
        val stack = ArrayDeque<Char>()

        raw.forEachIndexed { index, ch ->
            if (start < 0) {
                when (ch) {
                    '{' -> {
                        start = index
                        stack.addLast('}')
                    }
                    '[' -> {
                        start = index
                        stack.addLast(']')
                    }
                }
                return@forEachIndexed
            }

            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (ch == '\\') {
                    escaped = true
                } else if (ch == '"') {
                    inString = false
                }
                return@forEachIndexed
            }

            when (ch) {
                '"' -> inString = true
                '{' -> stack.addLast('}')
                '[' -> stack.addLast(']')
                '}', ']' -> {
                    if (stack.isEmpty() || stack.removeLast() != ch) {
                        return null
                    }
                    if (stack.isEmpty()) {
                        return raw.substring(start, index + 1).trim()
                    }
                }
            }
        }
        return if (start >= 0) raw.substring(start).trim() else null
    }

    private fun sanitizeJson(input: String): String {
        var result = toHalfWidth(input)
            .replace("\uFEFF", "")
            .replace("，", ",")
            .replace("：", ":")
            .replace("；", ",")
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‘", "'")
            .replace("’", "'")
            .trim()

        result = Regex("^\\s*json\\s*[:：]?", RegexOption.IGNORE_CASE).replace(result, "")
        result = Regex("\\[(?:\\s*)(\"[^\"]+\"|'[^']+'|[A-Za-z_\\u4e00-\\u9fa5\\s]+)(?:\\s*),(?:\\s*)([^\\]]+)\\]")
            .replace(result) { match ->
                val key = match.groupValues[1].trim().trim('"', '\'')
                val value = match.groupValues[2].trim()
                "\"$key\": $value"
            }
        result = Regex("\\[[^\\]\\n]*(?:同理|省略|填零|可省略|其他)[^\\]\\n]*\\]").replace(result, "")
        result = result
            .replace("{,", "{")
            .replace("[,", "[")
            .replace("(]", "]")
            .replace(")", "}")

        result = Regex("'([A-Za-z_\\u4e00-\\u9fa5][A-Za-z0-9_\\-\\u4e00-\\u9fa5\\s]*)'\\s*:")
            .replace(result) { match ->
                "\"${match.groupValues[1].trim()}\":"
            }

        result = Regex(":\\s*'([^']*)'").replace(result) { match ->
            ":\"${match.groupValues[1]}\""
        }

        result = Regex("([\\{,]\\s*)([A-Za-z_\\u4e00-\\u9fa5][A-Za-z0-9_\\-\\u4e00-\\u9fa5\\s]*)\\s*:")
            .replace(result) { match ->
                val prefix = match.groupValues[1]
                val key = match.groupValues[2].trim()
                "$prefix\"$key\":"
            }

        fieldAliases.forEach { (alias, canonical) ->
            val aliasRegex = Regex(
                "\"\\s*${Regex.escape(alias)}\\s*\"\\s*:",
                setOf(RegexOption.IGNORE_CASE)
            )
            result = aliasRegex.replace(result, "\"$canonical\":")
        }

        numericFields.forEach { field ->
            val valueRegex = Regex(
                "\"$field\"\\s*:\\s*(\"[^\"]*\"|'[^']*'|[^,}\\]\\n]+)",
                setOf(RegexOption.IGNORE_CASE)
            )
            result = valueRegex.replace(result) { match ->
                val rawValue = match.groupValues[1].trim().trim('"', '\'')
                val normalized = normalizeNumericLiteral(rawValue)
                "\"$field\":${normalized ?: "0"}"
            }
        }

        result = Regex("(?<=[0-9}\"\\]])\\s*(?=\"[A-Za-z_\\u4e00-\\u9fa5][^\"]*\"\\s*:)").replace(result, ", ")
        result = Regex(",\\s*,+").replace(result, ", ")
        result = Regex(",\\s*([}\\]])").replace(result, "$1")
        return result
    }

    private fun closeJsonStructures(input: String): String {
        if (input.isBlank()) return input

        val builder = StringBuilder(input.trim())
        var quoteCount = 0
        var escaped = false
        builder.forEach { ch ->
            if (escaped) {
                escaped = false
            } else if (ch == '\\') {
                escaped = true
            } else if (ch == '"') {
                quoteCount++
            }
        }
        if (quoteCount % 2 != 0) {
            builder.append('"')
        }

        val closers = ArrayDeque<Char>()
        var inString = false
        escaped = false
        builder.forEach { ch ->
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (ch == '\\') {
                    escaped = true
                } else if (ch == '"') {
                    inString = false
                }
                return@forEach
            }

            when (ch) {
                '"' -> inString = true
                '{' -> closers.addLast('}')
                '[' -> closers.addLast(']')
                '}', ']' -> if (closers.isNotEmpty() && closers.last() == ch) {
                    closers.removeLast()
                }
            }
        }

        while (closers.isNotEmpty()) {
            builder.append(closers.removeLast())
        }
        return builder.toString()
    }

    private fun collectFoodObjects(element: JsonElement, depth: Int = 0): List<JsonObject> {
        if (depth > 8 || element.isJsonNull) return emptyList()

        return when {
            element.isJsonObject -> {
                val obj = element.asJsonObject

                wrapperKeys.forEach { key ->
                    val child = obj.get(key)
                    if (child != null && !child.isJsonNull) {
                        val nested = collectFoodObjects(child, depth + 1)
                        if (nested.isNotEmpty()) return nested
                    }
                }

                val firstArray = obj.entrySet()
                    .firstOrNull { it.value.isJsonArray }
                    ?.value
                if (firstArray != null) {
                    val nested = collectFoodObjects(firstArray, depth + 1)
                    if (nested.isNotEmpty()) return nested
                }

                listOf(obj)
            }
            element.isJsonArray -> {
                val array = element.asJsonArray
                val directObjects = array.mapNotNull { item ->
                    item.takeIf { it.isJsonObject }?.asJsonObject
                }
                if (directObjects.isNotEmpty()) {
                    directObjects
                } else {
                    array.flatMap { item -> collectFoodObjects(item, depth + 1) }
                }
            }
            else -> emptyList()
        }
    }

    private fun parseFoodObject(obj: JsonObject, gson: Gson): FoodAnalysisResult? {
        if (obj.get("has_food")?.asBoolean == false) return null

        val direct = runCatching { gson.fromJson(obj, FoodAnalysisResult::class.java) }.getOrNull()
        if (direct != null && hasSignal(direct) && !isQuantityLike(direct.foodName)) {
            return direct
        }

        obj.get("food_info")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?.let { info ->
                val parsedInfo = parseFoodObject(info, gson)
                if (parsedInfo != null) {
                    val parentName = obj.get("foodName")?.asString?.trim()
                        ?: obj.get("name")?.asString?.trim()
                        ?: ""
                    val looksLikeQuantity = isQuantityLike(parentName) || Regex("\\d").containsMatchIn(parentName)
                    return if (parentName.isNotBlank() && !looksLikeQuantity) {
                        parsedInfo.copy(foodName = parentName)
                    } else {
                        parsedInfo
                    }
                }
            }

        fun stringOf(vararg keys: String): String {
            return keys.firstNotNullOfOrNull { key ->
                obj.get(key)?.takeIf { !it.isJsonNull }?.asString?.trim()
            }.orEmpty()
        }

        fun numberOf(vararg keys: String): Float {
            return keys.firstNotNullOfOrNull { key ->
                obj.get(key)?.takeIf { !it.isJsonNull }?.let { element ->
                    if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
                        element.asFloat
                    } else {
                        normalizeNumericLiteral(element.asString)?.toFloatOrNull()
                    }
                }
            } ?: 0f
        }

        val parsed = FoodAnalysisResult(
            foodName = stringOf("foodName", "name", "食物名称", "名称"),
            estimatedWeight = numberOf("estimatedWeight", "weight", "重量", "估计重量").toInt().coerceAtLeast(0),
            calories = numberOf("calories", "热量", "卡路里"),
            protein = numberOf("protein", "蛋白质"),
            carbs = numberOf("carbs", "carbohydrate", "碳水", "碳水化合物"),
            fat = numberOf("fat", "脂肪"),
            fiber = numberOf("fiber", "膳食纤维", "纤维"),
            sugar = numberOf("sugar", "糖分", "糖"),
            saturatedFat = numberOf("saturatedFat", "饱和脂肪"),
            cholesterol = numberOf("cholesterol", "胆固醇"),
            sodium = numberOf("sodium", "钠"),
            potassium = numberOf("potassium", "钾"),
            calcium = numberOf("calcium", "钙"),
            iron = numberOf("iron", "铁"),
            vitaminA = numberOf("vitaminA", "维生素A"),
            vitaminC = numberOf("vitaminC", "维生素C"),
            description = stringOf("description", "描述")
        )

        return if (hasSignal(parsed)) parsed else null
    }

    private fun hasSignal(result: FoodAnalysisResult): Boolean {
        return (result.foodName.isNotBlank() && !isQuantityLike(result.foodName)) ||
            result.calories > 0f ||
            result.protein > 0f ||
            result.carbs > 0f ||
            result.fat > 0f
    }

    private fun parseFoodItemsByPattern(raw: String): List<FoodAnalysisResult> {
        val normalized = normalizeLooseStructuredText(raw)
        val positions = findFoodNamePositions(normalized)
        if (positions.isEmpty()) return emptyList()

        return positions.mapIndexedNotNull { index, start ->
            val end = positions.getOrNull(index + 1) ?: normalized.length
            parseFoodFragment(normalized.substring(start, end))
        }
    }

    private fun normalizeLooseStructuredText(raw: String): String {
        return toHalfWidth(raw)
            .replace("\uFEFF", "")
            .replace("，", ",")
            .replace("：", ":")
            .replace("；", ",")
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‘", "'")
            .replace("’", "'")
            .replace(Regex("\\[(?:\\s*)(\"[^\"]+\"|'[^']+'|[A-Za-z_\\u4e00-\\u9fa5\\s]+)(?:\\s*),(?:\\s*)([^\\]]+)\\]")) { match ->
                val key = match.groupValues[1].trim().trim('"', '\'')
                val value = match.groupValues[2].trim()
                "\"$key\": $value"
            }
    }

    private fun findFoodNamePositions(normalized: String): List<Int> {
        return foodNameStartAliases.flatMap { alias ->
            val keyRegex = Regex(
                "(?:\"|')?${buildAliasKeyPattern(alias)}(?:\"|')?\\s*:",
                RegexOption.IGNORE_CASE
            )
            keyRegex.findAll(normalized).map { it.range.first }.toList()
        }.distinct().sorted()
    }

    private fun parseFoodFragment(fragment: String): FoodAnalysisResult? {
        val foodName = extractStringValue(
            fragment = fragment,
            aliases = aliasesFor("foodName") + "Chinese name food"
        )
        val estimatedWeight = extractNumberValue(fragment, aliasesFor("estimatedWeight")).toInt()
        val parsed = FoodAnalysisResult(
            foodName = foodName,
            estimatedWeight = estimatedWeight.coerceAtLeast(0),
            calories = extractNumberValue(fragment, aliasesFor("calories")),
            protein = extractNumberValue(fragment, aliasesFor("protein")),
            carbs = extractNumberValue(fragment, aliasesFor("carbs")),
            fat = extractNumberValue(fragment, aliasesFor("fat")),
            fiber = extractNumberValue(fragment, aliasesFor("fiber")),
            sugar = extractNumberValue(fragment, aliasesFor("sugar")),
            saturatedFat = extractNumberValue(fragment, aliasesFor("saturatedFat")),
            cholesterol = extractNumberValue(fragment, aliasesFor("cholesterol")),
            sodium = extractNumberValue(fragment, aliasesFor("sodium")),
            potassium = extractNumberValue(fragment, aliasesFor("potassium")),
            calcium = extractNumberValue(fragment, aliasesFor("calcium")),
            iron = extractNumberValue(fragment, aliasesFor("iron")),
            vitaminA = extractNumberValue(fragment, aliasesFor("vitaminA")),
            vitaminC = extractNumberValue(fragment, aliasesFor("vitaminC")),
            description = extractStringValue(fragment, aliasesFor("description"))
        )
        return parsed.takeIf(::hasSignal)
    }

    private fun aliasesFor(canonical: String): List<String> {
        return buildList {
            add(canonical)
            fieldAliases.filterValues { it == canonical }.keys.forEach(::add)
        }.distinct()
    }

    private fun extractStringValue(fragment: String, aliases: List<String>): String {
        aliases.forEach { alias ->
            val quoted = Regex(
                "(?:\"|')?${buildAliasKeyPattern(alias)}(?:\"|')?\\s*:\\s*(?:\"|')([^\"'\\n\\r]+)",
                RegexOption.IGNORE_CASE
            ).find(fragment)
            if (quoted != null) {
                return quoted.groupValues[1].trim()
            }

            val plain = Regex(
                "(?:\"|')?${buildAliasKeyPattern(alias)}(?:\"|')?\\s*:\\s*([^,}\\]\\n\\r]+)",
                RegexOption.IGNORE_CASE
            ).find(fragment)
            if (plain != null) {
                return plain.groupValues[1].trim().trim('"', '\'')
            }
        }
        return ""
    }

    private fun extractNumberValue(fragment: String, aliases: List<String>): Float {
        aliases.forEach { alias ->
            val pair = Regex(
                "\\[(?:\"|')?${Regex.escape(alias)}(?:\"|')?\\s*,\\s*([^\\]]+)\\]",
                RegexOption.IGNORE_CASE
            ).find(fragment)
            val pairValue = pair?.groupValues?.getOrNull(1)?.let(::normalizeNumericLiteral)?.toFloatOrNull()
            if (pairValue != null) {
                return pairValue
            }

            val standard = Regex(
                "(?:\"|')?${buildAliasKeyPattern(alias)}(?:\"|')?\\s*:\\s*([^,}\\]\\n\\r]+)",
                RegexOption.IGNORE_CASE
            ).find(fragment)
            val standardValue = standard?.groupValues?.getOrNull(1)?.let(::normalizeNumericLiteral)?.toFloatOrNull()
            if (standardValue != null) {
                return standardValue
            }
        }
        return 0f
    }

    private fun buildAliasKeyPattern(alias: String): String {
        return if (alias.all { it.isLetter() || it == '_' || it == ' ' }) {
            "(?<![A-Za-z])${Regex.escape(alias)}(?![A-Za-z])"
        } else {
            Regex.escape(alias)
        }
    }

    private fun isQuantityLike(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        return Regex(
            "^\\d+(?:\\.\\d+)?\\s*(g|kg|ml|l|克|千克|毫升|升|个|份|片|勺|杯|碗)?$",
            RegexOption.IGNORE_CASE
        ).matches(trimmed)
    }

    private fun toHalfWidth(input: String): String {
        val result = StringBuilder(input.length)
        input.forEach { ch ->
            when (ch) {
                in '０'..'９' -> result.append(('0'.code + (ch.code - '０'.code)).toChar())
                '．' -> result.append('.')
                '－' -> result.append('-')
                '＋' -> result.append('+')
                else -> result.append(ch)
            }
        }
        return result.toString()
    }
}
