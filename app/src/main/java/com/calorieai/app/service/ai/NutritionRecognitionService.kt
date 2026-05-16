package com.calorieai.app.service.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.calorieai.app.BuildConfig
import com.calorieai.app.service.ai.common.AIApiClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

@Singleton
class NutritionRecognitionService @Inject constructor(
    private val aiApiClient: AIApiClient,
    private val aiImportConfigResolver: AIImportConfigResolver
) {

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
    private val endpointCooldownUntil = ConcurrentHashMap<String, Long>()

    suspend fun recognizeNutritionTable(imageUri: Uri, context: Context): Result<NutritionInfo> {
        return withContext(Dispatchers.IO) {
            runCatching {
                coroutineScope {
                    val localDeferred = async { recognizeViaLocalService(imageUri, context) }
                    val mlKitDeferred = async { recognizeViaMlKit(imageUri, context) }

                    val localResult = localDeferred.await()
                    if (localResult != null && isUsableResult(localResult)) {
                        mlKitDeferred.cancel()
                        return@coroutineScope localResult
                    }

                    val mlKitResult = runCatching { mlKitDeferred.await() }.getOrNull()
                    val baseResult = chooseBestCandidate(localResult, mlKitResult)
                        ?: mlKitResult
                        ?: localResult
                        ?: NutritionInfo(
                            calories = 0,
                            protein = 0f,
                            carbs = 0f,
                            fat = 0f,
                            rawText = "",
                            source = "ocr_unavailable"
                        )

                    if (!shouldRunVisionValidation(baseResult)) {
                        return@coroutineScope baseResult
                    }

                    applyVisionValidation(imageUri, context, baseResult) ?: baseResult
                }
            }
        }
    }

    private fun chooseBestCandidate(vararg candidates: NutritionInfo?): NutritionInfo? {
        return candidates
            .filterNotNull()
            .maxByOrNull { infoQualityScore(it) }
    }

    private fun infoQualityScore(info: NutritionInfo): Int {
        var score = 0
        if (info.calories in 1..1200) score += 3
        if (info.protein in 0.1f..100f) score += 2
        if (info.carbs in 0.1f..100f) score += 2
        if (info.fat in 0.1f..100f) score += 2
        if (isUsableResult(info)) score += 4
        if (info.rawText.length >= 10) score += 1
        return score
    }

    private fun isUsableResult(info: NutritionInfo): Boolean {
        val macroCount = listOf(info.protein, info.carbs, info.fat).count { it > 0f }
        val hasCalories = info.calories > 0
        val hasEnoughSignals = macroCount >= 2 || (hasCalories && macroCount >= 1)

        val hasReasonableRange = info.calories in 1..1200 &&
            info.protein in 0f..100f &&
            info.carbs in 0f..100f &&
            info.fat in 0f..100f

        val loweredText = info.rawText.lowercase()
        val hasNutritionKeyword = NUTRITION_HINTS.any { loweredText.contains(it) }
        val hasTextEvidence = info.rawText.length >= 10 && hasNutritionKeyword
        val hasStructuredEvidence = hasCalories && macroCount >= 2

        return hasReasonableRange && hasEnoughSignals && (hasTextEvidence || hasStructuredEvidence)
    }

    private suspend fun recognizeViaMlKit(imageUri: Uri, context: Context): NutritionInfo {
        val image = InputImage.fromFilePath(context, imageUri)
        val visionText = recognizer.process(image).await()
        return parseNutritionText(visionText.text).copy(source = "mlkit")
    }

    private fun recognizeViaLocalService(imageUri: Uri, context: Context): NutritionInfo? {
        return runCatching {
            val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@runCatching null
            localOcrEndpoints().forEach { endpoint ->
                recognizeFromEndpoint(endpoint, imageBytes)?.let { return@runCatching it }
            }
            null
        }.getOrNull()
    }

    private fun localOcrEndpoints(): List<String> {
        val now = System.currentTimeMillis()
        val configured = BuildConfig.LOCAL_OCR_SERVICE_URL
            .split(',', ';', '\n')
            .map { it.trim().trimEnd('/') }
            .filter { it.isNotBlank() }
            .filter { endpointCooldownUntil[it]?.let { cooldownUntil -> cooldownUntil <= now } != false }
            .distinct()
        if (configured.isNotEmpty()) return configured

        if (!BuildConfig.DEBUG) return emptyList()

        return listOf(
            "http://10.0.2.2:8765",
            "http://127.0.0.1:8765",
            "http://localhost:8765"
        )
    }

    private fun recognizeFromEndpoint(endpoint: String, imageBytes: ByteArray): NutritionInfo? {
        val ocrUrl = if (endpoint.endsWith("/ocr")) endpoint else "$endpoint/ocr"
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "nutrition.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(ocrUrl)
            .post(body)
            .build()

        return runCatching {
            httpClient.newCall(request).execute().use {
                if (!it.isSuccessful) return null
                val payload = it.body?.string().orEmpty()
                if (payload.isBlank()) return null

                val obj = JSONObject(payload)
                val text = obj.optString("text", "").trim()
                val parsedByRegex = parseNutritionText(text)

                val fields = obj.optJSONObject("fields")
                val calories = fields?.optDouble("calories", Double.NaN)
                    ?.takeIf { !it.isNaN() && it > 0.0 }
                    ?.roundToInt()
                val protein = fields?.optDouble("protein", Double.NaN)
                    ?.takeIf { !it.isNaN() && it > 0.0 }
                    ?.toFloat()
                val carbs = fields?.optDouble("carbs", Double.NaN)
                    ?.takeIf { !it.isNaN() && it > 0.0 }
                    ?.toFloat()
                val fat = fields?.optDouble("fat", Double.NaN)
                    ?.takeIf { !it.isNaN() && it > 0.0 }
                    ?.toFloat()

                val engine = obj.optString("engine", "local_service").ifBlank { "local_service" }

                NutritionInfo(
                    calories = calories ?: parsedByRegex.calories,
                    protein = protein ?: parsedByRegex.protein,
                    carbs = carbs ?: parsedByRegex.carbs,
                    fat = fat ?: parsedByRegex.fat,
                    rawText = text,
                    source = engine
                )
            }
        }.onSuccess {
            endpointCooldownUntil.remove(endpoint)
        }.onFailure {
            endpointCooldownUntil[endpoint] = System.currentTimeMillis() + ENDPOINT_COOLDOWN_MS
        }.getOrNull()
    }

    private fun shouldRunVisionValidation(baseResult: NutritionInfo): Boolean {
        if (baseResult.rawText.isBlank()) return false
        if (baseResult.source.contains("ai_vision", ignoreCase = true)) return false
        if (isUsableResult(baseResult)) return false

        val missingFields = countMissingFields(baseResult)
        if (missingFields <= 1 && infoQualityScore(baseResult) >= 6) return false

        val loweredText = baseResult.rawText.lowercase()
        val hasNutritionKeyword = NUTRITION_HINTS.any { loweredText.contains(it) }
        return missingFields >= 2 && hasNutritionKeyword
    }

    private suspend fun applyVisionValidation(
        imageUri: Uri,
        context: Context,
        baseResult: NutritionInfo
    ): NutritionInfo? {
        val imageConfig = runCatching { aiImportConfigResolver.resolveImageConfig() }.getOrNull() ?: return null
        val apiKey = imageConfig.apiKey.trim()
        if (apiKey.isBlank()) return null

        val base64Image = imageUriToBase64(imageUri, context) ?: return null
        val responseText = runCatching {
            aiApiClient.vision(
                config = imageConfig,
                systemPrompt = VISION_VALIDATION_SYSTEM_PROMPT,
                userMessage = buildVisionValidationUserPrompt(baseResult),
                base64Image = base64Image,
                temperature = 0.1,
                maxTokens = 360
            )
        }.getOrNull() ?: return null

        val aiCandidate = parseVisionValidationResult(responseText) ?: return null
        val baseScore = infoQualityScore(baseResult)
        val candidateScore = aiCandidate.qualityScore()
        val shouldOverride = aiCandidate.confidence >= 0.88f && candidateScore > baseScore
        val fillsMissingValues = countMissingFields(baseResult) > countMissingFields(baseResult.mergeMissingOnly(aiCandidate))
        if (!fillsMissingValues && !shouldOverride) return null

        val merged = mergeVisionCandidate(baseResult, aiCandidate, allowOverride = shouldOverride)
        return if (merged == baseResult) null else merged
    }

    private fun countMissingFields(info: NutritionInfo): Int {
        var missing = 0
        if (info.calories <= 0) missing++
        if (info.protein <= 0f) missing++
        if (info.carbs <= 0f) missing++
        if (info.fat <= 0f) missing++
        return missing
    }

    private fun NutritionInfo.mergeMissingOnly(candidate: VisionNutritionCandidate): NutritionInfo {
        return mergeVisionCandidate(this, candidate, allowOverride = false)
    }

    private fun mergeVisionCandidate(
        baseResult: NutritionInfo,
        candidate: VisionNutritionCandidate,
        allowOverride: Boolean
    ): NutritionInfo {
        val mergedCalories = mergeIntValue(
            baseValue = baseResult.calories,
            aiValue = candidate.calories,
            minValue = 1,
            maxValue = 1200,
            allowOverride = allowOverride
        )
        val mergedProtein = mergeFloatValue(
            baseValue = baseResult.protein,
            aiValue = candidate.protein,
            minValue = 0f,
            maxValue = 100f,
            allowOverride = allowOverride
        )
        val mergedCarbs = mergeFloatValue(
            baseValue = baseResult.carbs,
            aiValue = candidate.carbs,
            minValue = 0f,
            maxValue = 100f,
            allowOverride = allowOverride
        )
        val mergedFat = mergeFloatValue(
            baseValue = baseResult.fat,
            aiValue = candidate.fat,
            minValue = 0f,
            maxValue = 100f,
            allowOverride = allowOverride
        )

        val changed = mergedCalories != baseResult.calories ||
            mergedProtein != baseResult.protein ||
            mergedCarbs != baseResult.carbs ||
            mergedFat != baseResult.fat
        if (!changed) return baseResult

        return baseResult.copy(
            calories = mergedCalories,
            protein = mergedProtein,
            carbs = mergedCarbs,
            fat = mergedFat,
            rawText = mergeRawText(baseResult.rawText, candidate.correctedText),
            source = "${baseResult.source}+ai_vision"
        )
    }

    private fun mergeIntValue(
        baseValue: Int,
        aiValue: Int?,
        minValue: Int,
        maxValue: Int,
        allowOverride: Boolean
    ): Int {
        val normalizedAi = aiValue?.takeIf { it in minValue..maxValue } ?: return baseValue
        if (baseValue !in minValue..maxValue) return normalizedAi
        return if (allowOverride) normalizedAi else baseValue
    }

    private fun mergeFloatValue(
        baseValue: Float,
        aiValue: Float?,
        minValue: Float,
        maxValue: Float,
        allowOverride: Boolean
    ): Float {
        val normalizedAi = aiValue?.takeIf { it in minValue..maxValue } ?: return baseValue
        if (baseValue !in minValue..maxValue || baseValue <= 0f) return normalizedAi
        return if (allowOverride) normalizedAi else baseValue
    }

    private fun mergeRawText(baseRawText: String, correctedText: String?): String {
        val normalizedCorrection = correctedText.orEmpty().trim()
        if (normalizedCorrection.isBlank()) return baseRawText
        if (baseRawText.contains(normalizedCorrection)) return baseRawText
        return buildString {
            if (baseRawText.isNotBlank()) {
                append(baseRawText.trim())
                append("\n\n")
            }
            append("[AI validated]\n")
            append(normalizedCorrection)
        }
    }

    private fun parseVisionValidationResult(responseText: String): VisionNutritionCandidate? {
        val jsonText = extractJsonObject(responseText) ?: return null
        return runCatching {
            val root = JSONObject(jsonText)
            VisionNutritionCandidate(
                calories = root.optFlexibleInt("calories"),
                protein = root.optFlexibleFloat("protein"),
                carbs = root.optFlexibleFloat("carbs"),
                fat = root.optFlexibleFloat("fat"),
                confidence = root.optFlexibleFloat("confidence")?.coerceIn(0f, 1f) ?: 0f,
                correctedText = root.optString("correctedText", "")
                    .ifBlank { root.optString("ocrText", "") }
                    .trim()
            )
        }.getOrNull()
    }

    private fun extractJsonObject(responseText: String): String? {
        val fenced = Regex("```(?:json)?\\s*(\\{.*})\\s*```", RegexOption.DOT_MATCHES_ALL)
            .find(responseText)
            ?.groupValues
            ?.getOrNull(1)
        if (!fenced.isNullOrBlank()) return fenced

        val start = responseText.indexOf('{')
        val end = responseText.lastIndexOf('}')
        if (start == -1 || end <= start) return null
        return responseText.substring(start, end + 1)
    }

    private fun buildVisionValidationUserPrompt(baseResult: NutritionInfo): String {
        return """
            OCR raw text:
            ${baseResult.rawText.ifBlank { "(empty)" }}

            Current extracted values (per 100g or per 100ml):
            calories=${baseResult.calories}
            protein=${baseResult.protein}
            carbs=${baseResult.carbs}
            fat=${baseResult.fat}

            Read the nutrition facts label in the image and correct the four fields.
            Keep units normalized as kcal and grams.
            Return JSON only.
        """.trimIndent()
    }

    private fun imageUriToBase64(imageUri: Uri, context: Context): String? {
        return runCatching {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use(BitmapFactory::decodeStream)
                ?: return@runCatching null
            val compressed = compressBitmap(bitmap, maxSizeKb = 1024)
            val outputStream = ByteArrayOutputStream()
            compressed.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }.getOrNull()
    }

    private fun compressBitmap(bitmap: Bitmap, maxSizeKb: Int): Bitmap {
        var quality = 95
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.size() / 1024 > maxSizeKb && quality > 55) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        if (outputStream.size() / 1024 <= maxSizeKb) return bitmap

        val scaleFactor = kotlin.math.sqrt((maxSizeKb * 1024).toDouble() / outputStream.size().toDouble())
        val newWidth = max(1, (bitmap.width * scaleFactor).toInt())
        val newHeight = max(1, (bitmap.height * scaleFactor).toInt())
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun parseNutritionText(text: String): NutritionInfo {
        val normalizedText = normalizeOcrText(text)
        val lines = normalizedText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val calories = extractCalories(lines, normalizedText)
        val protein = extractMacro(lines, normalizedText, PROTEIN_ALIASES)
        val carbs = extractMacro(lines, normalizedText, CARB_ALIASES)
        val fat = extractMacro(lines, normalizedText, FAT_ALIASES)

        return NutritionInfo(
            calories = calories ?: 0,
            protein = protein ?: 0f,
            carbs = carbs ?: 0f,
            fat = fat ?: 0f,
            rawText = text,
            source = "regex"
        )
    }

    private fun extractCalories(lines: List<String>, normalizedText: String): Int? {
        val kcalValue = extractValue(lines, normalizedText, CALORIE_ALIASES, preferredUnit = "kcal")
        if (kcalValue != null) return kcalValue.roundToInt()

        val kjValue = extractValue(lines, normalizedText, CALORIE_ALIASES, preferredUnit = "kj")
        return kjValue?.let { (it / 4.184f).roundToInt() }
    }

    private fun extractMacro(
        lines: List<String>,
        normalizedText: String,
        aliases: List<String>
    ): Float? {
        return extractValue(lines, normalizedText, aliases)
    }

    private fun extractValue(
        lines: List<String>,
        normalizedText: String,
        aliases: List<String>,
        preferredUnit: String? = null
    ): Float? {
        lines.forEachIndexed { index, line ->
            if (aliases.none { line.contains(it, ignoreCase = true) }) return@forEachIndexed

            findValueInLine(line, preferredUnit)?.let { return it }
            lines.getOrNull(index + 1)?.let { next ->
                findValueInLine(next, preferredUnit)?.let { return it }
            }
            lines.getOrNull(index + 2)?.let { next ->
                if (NUMBER_ONLY_PATTERN.matcher(next).matches()) {
                    next.toFloatOrNull()?.let { return it }
                }
            }
        }

        aliases.forEach { alias ->
            val directPattern = if (preferredUnit.isNullOrBlank()) {
                Pattern.compile("${Pattern.quote(alias)}[^\\d]{0,12}(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE)
            } else {
                Pattern.compile(
                    "${Pattern.quote(alias)}[^\\d]{0,16}(\\d+(?:[.,]\\d+)?)\\s*${Pattern.quote(preferredUnit)}",
                    Pattern.CASE_INSENSITIVE
                )
            }
            val matcher = directPattern.matcher(normalizedText)
            if (matcher.find()) {
                matcher.group(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
            }
        }

        return null
    }

    private fun findValueInLine(line: String, preferredUnit: String?): Float? {
        val pattern = if (preferredUnit.isNullOrBlank()) {
            NUMBER_WITH_OPTIONAL_UNIT_PATTERN
        } else {
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*${Pattern.quote(preferredUnit)}", Pattern.CASE_INSENSITIVE)
        }
        val matcher = pattern.matcher(line)
        return if (matcher.find()) matcher.group(1)?.replace(',', '.')?.toFloatOrNull() else null
    }

    private fun normalizeOcrText(text: String): String {
        return text
            .replace('\u3000', ' ')
            .replace(Regex("[ \t]+"), " ")
            .replace("\u5343\u7126", "kj")
            .replace("\u5343\u5361", "kcal")
            .replace("\u514b", "g")
    }

    private data class VisionNutritionCandidate(
        val calories: Int?,
        val protein: Float?,
        val carbs: Float?,
        val fat: Float?,
        val confidence: Float,
        val correctedText: String?
    ) {
        fun qualityScore(): Int {
            var score = 0
            if (calories in 1..1200) score += 3
            if (protein != null && protein in 0f..100f && protein > 0f) score += 2
            if (carbs != null && carbs in 0f..100f && carbs > 0f) score += 2
            if (fat != null && fat in 0f..100f && fat > 0f) score += 2
            score += (confidence * 3f).roundToInt()
            return score
        }
    }

    private fun JSONObject.optFlexibleInt(key: String): Int? {
        val raw = optFlexibleFloat(key) ?: return null
        return raw.roundToInt().takeIf { it > 0 }
    }

    private fun JSONObject.optFlexibleFloat(key: String): Float? {
        val raw = when (val value = opt(key)) {
            null -> return null
            is Number -> value.toString()
            else -> value.toString()
        }
        return raw
            .replace(',', '.')
            .replace(Regex("[^0-9.]"), "")
            .takeIf { it.isNotBlank() }
            ?.toFloatOrNull()
    }

    companion object {
        private const val ENDPOINT_COOLDOWN_MS = 2 * 60 * 1000L
        private val NUTRITION_HINTS = listOf(
            "\u8425\u517b",
            "\u80fd\u91cf",
            "\u70ed\u91cf",
            "\u86cb\u767d",
            "\u78b3\u6c34",
            "\u8102\u80aa",
            "nutrition",
            "calorie",
            "protein",
            "carb",
            "fat"
        )

        private val CALORIE_ALIASES = listOf(
            "\u80fd\u91cf",
            "\u70ed\u91cf",
            "energy",
            "calorie",
            "calories",
            "kcal"
        )
        private val PROTEIN_ALIASES = listOf("\u86cb\u767d\u8d28", "protein")
        private val CARB_ALIASES = listOf(
            "\u78b3\u6c34\u5316\u5408\u7269",
            "\u78b3\u6c34",
            "carbohydrate",
            "carbohydrates",
            "carbs"
        )
        private val FAT_ALIASES = listOf("\u8102\u80aa", "fat")
        private val NUMBER_WITH_OPTIONAL_UNIT_PATTERN =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:g|mg|kj|kcal)?", Pattern.CASE_INSENSITIVE)
        private val NUMBER_ONLY_PATTERN = Pattern.compile("^\\d+(?:[.,]\\d+)?$")

        private val VISION_VALIDATION_SYSTEM_PROMPT = """
            You verify OCR results for nutrition facts labels.
            Read the image and return JSON only.
            Required keys: calories, protein, carbs, fat, confidence, correctedText.
            Use per 100g or per 100ml values only.
            Normalize calories to kcal and macros to grams.
            confidence must be a number between 0 and 1.
            Do not add explanations or markdown.
        """.trimIndent()
    }
}

data class NutritionInfo(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val rawText: String,
    val source: String = "mlkit"
)
