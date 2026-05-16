package com.calorieai.app.service.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.room.Room
import com.calorieai.app.BuildConfig
import com.calorieai.app.data.local.AppDatabase
import com.calorieai.app.data.repository.AIConfigRepository
import com.calorieai.app.data.repository.AIFunctionConfigRepository
import com.calorieai.app.data.security.AIConfigSecretCipher
import com.calorieai.app.service.ai.common.AIApiClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NutritionRecognitionServiceInstrumentedTest {

    data class NutritionCase(
        val calories: Int,
        val protein: Float,
        val carbs: Float,
        val fat: Float
    )

    private val networkClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    @Test
    fun recognizeNutritionTable_returnsUsableNutritionValues() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageUri = createSyntheticNutritionImage(context.cacheDir)
        val service = createService()

        val result = service.recognizeNutritionTable(imageUri, context)
        assertTrue("OCR should succeed", result.isSuccess)

        val info = result.getOrThrow()
        Log.i(
            "OCR_TEST",
            "source=${info.source}, calories=${info.calories}, protein=${info.protein}, carbs=${info.carbs}, fat=${info.fat}, raw=${info.rawText.take(120)}"
        )
        assertTrue("calories should be parsed", info.calories in 150..400)
        assertTrue("protein should be parsed", info.protein in 5f..30f)
        assertTrue("carbs should be parsed", info.carbs in 10f..60f)
        assertTrue("fat should be parsed", info.fat in 1f..20f)
        assertTrue("raw text should not be blank", info.rawText.isNotBlank())
        assertTrue("source should be recognized", info.source == "mlkit" || info.source == "local_service")
    }

    @Test
    fun localOcrService_healthEndpointReachable_whenConfigured() {
        val endpoint = BuildConfig.LOCAL_OCR_SERVICE_URL.trim()
        assumeTrue("LOCAL_OCR_SERVICE_URL not configured", endpoint.isNotBlank())

        val healthUrl = if (endpoint.endsWith("/health")) endpoint else "$endpoint/health"
        val request = Request.Builder().url(healthUrl).get().build()
        networkClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            assertTrue("health endpoint should return 2xx", response.isSuccessful)
            assertTrue("health response should contain status=ok", body.contains("\"status\":\"ok\""))
        }
    }

    @Test
    fun localOcrService_ocrEndpointResponds_whenConfigured() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val endpoint = BuildConfig.LOCAL_OCR_SERVICE_URL.trim()
        assumeTrue("LOCAL_OCR_SERVICE_URL not configured", endpoint.isNotBlank())

        val ocrUrl = if (endpoint.endsWith("/ocr")) endpoint else "$endpoint/ocr"
        val imageUri = createSyntheticNutritionImage(
            cacheDir = context.cacheDir,
            target = NutritionCase(230, 11.3f, 28.2f, 7.4f),
            index = 999
        )
        val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
        assertTrue("test image bytes should not be empty", imageBytes != null && imageBytes.isNotEmpty())

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "ocr_instrumentation_sample_999.jpg",
                imageBytes!!.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder().url(ocrUrl).post(body).build()
        networkClient.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            assertTrue("ocr endpoint should return 2xx", response.isSuccessful)
            assertTrue("ocr payload should contain text field", payload.contains("\"text\""))
        }
    }

    @Test
    fun recognizeNutritionTable_batchSyntheticCases_keepsHighPassRate() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = createService()
        val cases = listOf(
            NutritionCase(250, 12.5f, 30.0f, 8.0f),
            NutritionCase(180, 8.2f, 22.0f, 6.0f),
            NutritionCase(310, 18.0f, 35.5f, 12.0f),
            NutritionCase(95, 3.5f, 10.2f, 2.8f),
            NutritionCase(420, 24.0f, 48.0f, 16.0f),
            NutritionCase(275, 11.0f, 33.3f, 9.5f),
            NutritionCase(160, 6.5f, 19.0f, 4.2f),
            NutritionCase(365, 20.0f, 41.0f, 13.5f),
            NutritionCase(220, 9.1f, 27.0f, 7.7f),
            NutritionCase(145, 5.0f, 16.4f, 3.6f)
        )

        var passCount = 0
        cases.forEachIndexed { index, target ->
            val imageUri = createSyntheticNutritionImage(context.cacheDir, target, index)
            val result = service.recognizeNutritionTable(imageUri, context)
            assertTrue("case $index should succeed", result.isSuccess)

            val info = result.getOrThrow()
            val casePass = isClose(info.calories.toFloat(), target.calories.toFloat(), 12f) &&
                isClose(info.protein, target.protein, 1.2f) &&
                isClose(info.carbs, target.carbs, 1.2f) &&
                isClose(info.fat, target.fat, 1.2f)

            if (casePass) passCount += 1
            Log.i(
                "OCR_BATCH_TEST",
                "case=$index source=${info.source} expected=${target.calories}/${target.protein}/${target.carbs}/${target.fat} " +
                    "actual=${info.calories}/${info.protein}/${info.carbs}/${info.fat} pass=$casePass"
            )
        }

        val passRate = passCount.toFloat() / cases.size.toFloat()
        assertTrue("batch pass rate too low: $passRate", passRate >= 0.8f)
    }

    private fun isClose(actual: Float, expected: Float, tolerance: Float): Boolean {
        return kotlin.math.abs(actual - expected) <= tolerance
    }

    private fun createService(): NutritionRecognitionService {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calorieai_database"
        ).build()
        val aiConfigRepository = AIConfigRepository(database.aiConfigDao(), AIConfigSecretCipher())
        val functionConfigRepository = AIFunctionConfigRepository(aiConfigRepository, context)
        return NutritionRecognitionService(
            aiApiClient = AIApiClient(OkHttpClient()),
            aiImportConfigResolver = AIImportConfigResolver(aiConfigRepository, functionConfigRepository)
        )
    }

    private fun createSyntheticNutritionImage(cacheDir: File): Uri {
        return createSyntheticNutritionImage(
            cacheDir = cacheDir,
            target = NutritionCase(250, 12.5f, 30.0f, 8.0f),
            index = 0
        )
    }

    private fun createSyntheticNutritionImage(cacheDir: File, target: NutritionCase, index: Int): Uri {
        val bitmap = Bitmap.createBitmap(1200, 1400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 78f
        }

        val lines = listOf(
            "Nutrition Facts",
            "Calories ${target.calories} kcal",
            "Protein ${String.format(Locale.US, "%.1f", target.protein)} g",
            "Carbs ${String.format(Locale.US, "%.1f", target.carbs)} g",
            "Fat ${String.format(Locale.US, "%.1f", target.fat)} g"
        )

        var y = 180f
        for (line in lines) {
            canvas.drawText(line, 60f, y, paint)
            y += 150f
        }

        val imageFile = File(cacheDir, "ocr_instrumentation_sample_$index.jpg")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        bitmap.recycle()
        return Uri.fromFile(imageFile)
    }
}
