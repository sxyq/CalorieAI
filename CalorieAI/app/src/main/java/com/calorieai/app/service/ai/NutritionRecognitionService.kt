package com.calorieai.app.service.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRecognitionService @Inject constructor() {

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend fun recognizeNutritionTable(imageUri: Uri, context: Context): Result<NutritionInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val visionText = recognizer.process(image).await()
                
                val text = visionText.text
                val nutritionInfo = parseNutritionText(text)
                
                Result.success(nutritionInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseNutritionText(text: String): NutritionInfo {
        val lines = text.lines()
        
        var calories: Int? = null
        var protein: Float? = null
        var carbs: Float? = null
        var fat: Float? = null
        
        // 正则表达式模式
        val caloriePattern = Pattern.compile("(能量|热量|卡路里|calories?)[^\\d]*(\\d+)[^\\d]*千?焦?", Pattern.CASE_INSENSITIVE)
        val proteinPattern = Pattern.compile("(蛋白质|protein)[^\\d]*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        val carbPattern = Pattern.compile("(碳水化合物|碳水|carbohydrates?|carbs?)[^\\d]*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        val fatPattern = Pattern.compile("(脂肪|fat)[^\\d]*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        
        for (line in lines) {
            // 解析热量
            if (calories == null) {
                val matcher = caloriePattern.matcher(line)
                if (matcher.find()) {
                    calories = matcher.group(2)?.toIntOrNull()
                }
            }
            
            // 解析蛋白质
            if (protein == null) {
                val matcher = proteinPattern.matcher(line)
                if (matcher.find()) {
                    protein = matcher.group(2)?.toFloatOrNull()
                }
            }
            
            // 解析碳水
            if (carbs == null) {
                val matcher = carbPattern.matcher(line)
                if (matcher.find()) {
                    carbs = matcher.group(2)?.toFloatOrNull()
                }
            }
            
            // 解析脂肪
            if (fat == null) {
                val matcher = fatPattern.matcher(line)
                if (matcher.find()) {
                    fat = matcher.group(2)?.toFloatOrNull()
                }
            }
        }
        
        return NutritionInfo(
            calories = calories ?: 0,
            protein = protein ?: 0f,
            carbs = carbs ?: 0f,
            fat = fat ?: 0f,
            rawText = text
        )
    }
}

data class NutritionInfo(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val rawText: String
)
