package com.calorieai.app.service.update

import com.calorieai.app.BuildConfig
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody

@Singleton
class AppUpdateService @Inject constructor(
    okHttpClient: OkHttpClient
) {
    // Update checks are bounded independently from long-running AI requests.
    private val updateClient = okHttpClient.newBuilder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchUpdateInfo(): AppUpdateInfo? {
        val endpoint = BuildConfig.UPDATE_CHECK_URL.trim()
        if (!isValidUpdateEndpoint(endpoint)) return null

        val request = Request.Builder()
            .url(endpoint)
            .header("Cache-Control", "no-cache")
            .get()
            .build()

        return runCatching {
            updateClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val responseBody = response.body ?: return null
                val body = responseBody.readBoundedUtf8(MAX_METADATA_BYTES) ?: return null
                if (body.isBlank()) return null

                val root = runCatching {
                    JsonParser.parseString(body).asJsonObject
                }.getOrNull() ?: return null

                if (!hasExpectedFields(root)) return null

                val latestCode = root.readInt("versionCode") ?: return null
                val latestName = root.readString("versionName") ?: return null
                val minSupportedCode = root.readInt("minSupportedVersionCode") ?: return null
                val downloadUrl = root.readString("apkUrl") ?: return null
                val apkSize = root.readLong("apkSize") ?: return null
                val apkSha256 = root.readString("apkSha256")
                    ?.lowercase(Locale.US)
                    ?: return null
                val releaseNotes = root.readString("releaseNotes") ?: return null
                val forceUpdate = root.readBoolean("forceUpdate") ?: return null

                if (
                    latestCode <= 0 ||
                    minSupportedCode < 0 ||
                    minSupportedCode > latestCode ||
                    !VERSION_NAME_PATTERN.matches(latestName) ||
                    apkSize <= 0L ||
                    releaseNotes.length > MAX_RELEASE_NOTES_LENGTH ||
                    !SHA256_PATTERN.matches(apkSha256) ||
                    !isValidDownloadUrl(downloadUrl, latestName)
                ) {
                    return null
                }

                AppUpdateInfo(
                    latestVersionCode = latestCode,
                    latestVersionName = latestName,
                    minSupportedVersionCode = minSupportedCode,
                    downloadUrl = downloadUrl,
                    apkSize = apkSize,
                    apkSha256 = apkSha256,
                    releaseNotes = releaseNotes,
                    forceUpdate = forceUpdate,
                    isMandatory = false
                )
            }
        }.getOrNull()
    }

    private fun JsonObject.readString(key: String): String? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        if (!primitive.isString) return null
        return runCatching { primitive.asString }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.readInt(key: String): Int? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        if (!primitive.isNumber) return null
        return runCatching {
            val number = primitive.asNumber.toDouble()
            if (!number.isFinite() || number % 1.0 != 0.0 || number > Int.MAX_VALUE) {
                null
            } else {
                number.toInt()
            }
        }.getOrNull()
    }

    private fun JsonObject.readLong(key: String): Long? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        if (!primitive.isNumber) return null
        return runCatching {
            val number = primitive.asNumber.toDouble()
            if (!number.isFinite() || number % 1.0 != 0.0 || number > Long.MAX_VALUE) {
                null
            } else {
                number.toLong()
            }
        }.getOrNull()
    }

    private fun JsonObject.readBoolean(key: String): Boolean? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        if (!primitive.isBoolean) return null
        return runCatching { primitive.asBoolean }.getOrNull()
    }

    private fun hasExpectedFields(root: JsonObject): Boolean {
        return root.entrySet().all { it.key in ALLOWED_FIELDS } &&
            REQUIRED_FIELDS.all(root::has)
    }

    private fun isValidUpdateEndpoint(value: String): Boolean {
        return AppUpdateEndpoint.isValidCheckUrl(value)
    }

    private fun isValidDownloadUrl(value: String, versionName: String): Boolean {
        return AppUpdateEndpoint.isValidDownloadUrl(value, versionName)
    }

    private fun ResponseBody.readBoundedUtf8(maxBytes: Long): String? {
        if (contentLength() > maxBytes) return null
        val output = ByteArrayOutputStream((maxBytes + 1L).toInt())
        val buffer = ByteArray(BODY_READ_BUFFER_SIZE)
        var total = 0L
        byteStream().use { input ->
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxBytes) return null
                output.write(buffer, 0, count)
            }
        }
        return output.toString(StandardCharsets.UTF_8.name())
    }

    companion object {
        private const val MAX_METADATA_BYTES = 64L * 1024L
        private const val MAX_RELEASE_NOTES_LENGTH = 16 * 1024
        private const val BODY_READ_BUFFER_SIZE = 8 * 1024
        private val VERSION_NAME_PATTERN = Regex("[0-9]+\\.[0-9]+\\.[0-9]+")
        private val SHA256_PATTERN = Regex("[0-9a-f]{64}")
        private val REQUIRED_FIELDS = setOf(
            "versionCode",
            "versionName",
            "minSupportedVersionCode",
            "forceUpdate",
            "apkUrl",
            "apkSize",
            "apkSha256",
            "releaseNotes"
        )
        private val ALLOWED_FIELDS = REQUIRED_FIELDS
    }
}
