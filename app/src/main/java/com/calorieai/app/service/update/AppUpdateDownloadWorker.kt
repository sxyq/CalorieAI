package com.calorieai.app.service.update

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class AppUpdateDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL).orEmpty()
        val versionCode = inputData.getInt(KEY_VERSION_CODE, -1)
        val expectedSize = inputData.getLong(KEY_EXPECTED_SIZE, -1L)
        val expectedSha256 = inputData.getString(KEY_EXPECTED_SHA256)
            .orEmpty()
            .lowercase(Locale.US)
        if (
            url.isBlank() ||
            !isAllowedDownloadUrl(url) ||
            versionCode <= 0 ||
            expectedSize <= 0L ||
            !SHA256_PATTERN.matches(expectedSha256)
        ) {
            return failure("更新元数据不完整，已忽略本次下载。")
        }

        val updatesRoot = File(applicationContext.filesDir, "updates")
        val stagingRoot = File(updatesRoot, "staging")
        if (!stagingRoot.exists() && !stagingRoot.mkdirs()) {
            return failure("无法创建更新临时目录。")
        }
        val partFile = File(stagingRoot, "$versionCode.apk.part")

        return try {
            download(url, partFile, expectedSize)
            if (isStopped) return failure("下载已取消。")
            if (partFile.length() != expectedSize) {
                partFile.delete()
                return failure("下载文件大小校验失败。")
            }
            if (!hasSha256(partFile, expectedSha256)) {
                partFile.delete()
                return failure("下载文件完整性校验失败。")
            }

            val finalDir = File(updatesRoot, versionCode.toString())
            if (!finalDir.exists() && !finalDir.mkdirs()) {
                partFile.delete()
                return failure("无法创建已校验文件目录。")
            }
            val finalFile = File(finalDir, "CalorieAI-$versionCode.apk")
            if (!partFile.renameTo(finalFile)) {
                partFile.inputStream().use { input ->
                    finalFile.outputStream().use { output -> input.copyTo(output) }
                }
                partFile.delete()
            }
            Result.success(workDataOf(KEY_APK_PATH to finalFile.absolutePath))
        } catch (error: Throwable) {
            // Keep a partial network download so the next attempt can send Range.
            failure(error.message ?: "下载失败，请重试。")
        }
    }

    private suspend fun download(url: String, partFile: File, expectedSize: Long) {
        var existingBytes = partFile.takeIf { it.isFile }?.length() ?: 0L
        if (existingBytes >= expectedSize) {
            partFile.delete()
            existingBytes = 0L
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .apply {
                if (existingBytes > 0L) {
                    header("Range", "bytes=$existingBytes-")
                }
            }
            .build()

        val client = okHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(2, TimeUnit.HOURS)
            .build()
        client.newCall(request).execute().use { response ->
            val append = existingBytes > 0L && response.code == 206
            if (!response.isSuccessful || (existingBytes > 0L && !append && response.code != 200)) {
                throw IOException("下载服务返回 HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("下载响应为空")
            val startingBytes = if (append) existingBytes else 0L
            body.byteStream().use { input ->
                FileOutputStream(partFile, append).use { output ->
                    copyWithProgress(input, output, startingBytes, expectedSize)
                }
            }
        }
    }

    private suspend fun copyWithProgress(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        startingBytes: Long,
        expectedSize: Long
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var downloaded = startingBytes
        var lastReport = startingBytes
        while (true) {
            if (isStopped) throw IOException("下载已取消")
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
            downloaded += count
            if (downloaded > expectedSize) {
                throw IOException("下载文件超过元数据声明大小")
            }
            if (downloaded - lastReport >= PROGRESS_STEP || downloaded == expectedSize) {
                val percent = ((downloaded * 100L) / expectedSize)
                    .coerceIn(0L, 100L)
                    .toInt()
                setProgress(
                    workDataOf(
                        KEY_PROGRESS to percent,
                        KEY_DOWNLOADED_BYTES to downloaded,
                        KEY_TOTAL_BYTES to expectedSize
                    )
                )
                lastReport = downloaded
            }
        }
    }

    private fun hasSha256(file: File, expected: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val actual = digest.digest().joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
        return actual.equals(expected, ignoreCase = true)
    }

    private fun isAllowedDownloadUrl(value: String): Boolean {
        val path = runCatching { Uri.parse(value).path.orEmpty() }.getOrDefault("")
        val versionName = DOWNLOAD_PATH_PATTERN.matchEntire(path)?.groupValues?.get(1)
            ?: return false
        return AppUpdateEndpoint.isValidDownloadUrl(value, versionName)
    }

    private fun failure(message: String): Result =
        Result.failure(workDataOf(KEY_ERROR to message))

    companion object {
        const val TAG = "app_update_download"
        const val KEY_URL = "url"
        const val KEY_VERSION_CODE = "version_code"
        const val KEY_EXPECTED_SIZE = "expected_size"
        const val KEY_EXPECTED_SHA256 = "expected_sha256"
        const val KEY_PROGRESS = "progress"
        const val KEY_DOWNLOADED_BYTES = "downloaded_bytes"
        const val KEY_TOTAL_BYTES = "total_bytes"
        const val KEY_APK_PATH = "apk_path"
        const val KEY_ERROR = "error"

        private const val PROGRESS_STEP = 256 * 1024L
        private val DOWNLOAD_PATH_PATTERN = Regex(
            "/releases/([0-9]+\\.[0-9]+\\.[0-9]+)/CalorieAI-v\\1\\.apk"
        )
        private val SHA256_PATTERN = Regex("[0-9a-f]{64}")

        fun uniqueWorkName(versionCode: Int): String = "$TAG-$versionCode"
    }
}
